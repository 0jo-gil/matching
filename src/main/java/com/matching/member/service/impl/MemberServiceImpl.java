package com.matching.member.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.MatchingApplication;
import com.matching.aws.service.AwsS3Service;
import com.matching.common.config.JwtTokenProvider;
import com.matching.member.domain.Member;
import com.matching.member.domain.MemberStatus;
import com.matching.member.domain.RefreshToken;
import com.matching.member.dto.MemberResponse;
import com.matching.member.dto.SignInRequest;
import com.matching.member.dto.SignUpRequest;
import com.matching.member.dto.MemberUpdateRequest;
import com.matching.member.repository.MemberRepository;
import com.matching.member.repository.RefreshTokenRepository;
import com.matching.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AwsS3Service awsS3Service;

//    private final Map memberResponseMap = new HashMap<>();
    @Value("${jwt.refresh-token-expire-time}")
    int expiredTime;
    private final Logger logger = LoggerFactory.getLogger(MatchingApplication.class);

    /**
     * 회원 가입
     * @param Member
     * @return boolean
     */
    @Transactional
    @Override
    public boolean signup(SignUpRequest parameter, List<MultipartFile> multipartFile) {
        logger.info("sign up: {}" );
        boolean existsByEmail = memberRepository.existsByEmail(parameter.getEmail());

        if(existsByEmail) {
            throw new RuntimeException("회원 이메일이 존재합니다.");
        }

        if(!multipartFile.isEmpty()) {
            List<String> uploadFile = awsS3Service.upload(multipartFile);
            parameter.setProfileImageUrl(uploadFile.get(0));
        }

        parameter.setPassword(passwordEncoder.encode(parameter.getPassword()));

        Member member = memberRepository.save(Member.from(parameter));

        if(ObjectUtils.isEmpty(member)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 로그인
     * @param SignInRequest (email, password)
     * @return MemberResponse
     */
    @Override
    public MemberResponse signIn(SignInRequest parameter) {
        Member member = memberRepository.findByEmail(parameter.getEmail())
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));

        if(!passwordEncoder.matches(parameter.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRoles());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(String.valueOf(member.getId()))
                        .refreshToken(refreshToken)
                        .build()
        );

        return MemberResponse.of(member, accessToken);
    }

    @Override
    public MemberResponse reissue(HttpServletRequest request, Long id) {
        try{
            String accessToken = jwtTokenProvider.resolveToken(request);

            RefreshToken refreshToken = refreshTokenRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("리프레쉬 토큰이 없습니다."));

            if(jwtTokenProvider.validateToken(accessToken) == 2) {
                Member member = memberRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

                String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRoles());
                String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRoles());

                refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(String.valueOf(member.getId()))
                                .refreshToken(newRefreshToken)
                                .build()
                );

                return MemberResponse.of(member, newAccessToken);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("토큰 만료");
        }
        return null;
    }


    /**
     * 회정정보 수정
     * @param UpdateMemberRequest
     * @return MemberResponse
     */
    @Transactional
    @Override
    public MemberResponse updateMember(MemberUpdateRequest parameter, Long id, List<MultipartFile> multipartFile) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRoles());

        List<String> uploadFile = awsS3Service.upload(multipartFile);

        if(multipartFile != null) {
            parameter.setProfileImageUrl(uploadFile.get(0));
        }

        member.update(parameter);

        return MemberResponse.of(member, accessToken);
    }

    @Override
    public boolean logout(HttpServletRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));
        String token = jwtTokenProvider.resolveToken(request);

        updateUserByTokenBlackList(token, member.getId());

        return true;
    }


    @Override
    public boolean withdraw(HttpServletRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 회원이 없습니다."));
        String token = jwtTokenProvider.resolveToken(request);

        updateUserByTokenBlackList(token, member.getId());
        member.setStatus(MemberStatus.WITHDRAW);

        return true;
    }

    private void updateUserByTokenBlackList(String accessToken, Long memberId) {
        RefreshToken refreshToken = refreshTokenRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 유저 토큰 정보가 없습니다."));

        Long expiration = jwtTokenProvider.getExpiration(accessToken);

        refreshTokenRepository.setBlackList(accessToken, expiration);
        refreshTokenRepository.delete(refreshToken);
    }
}
