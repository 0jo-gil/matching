package com.matching.member.service;

import com.matching.member.dto.MemberResponse;
import com.matching.member.dto.SignInRequest;
import com.matching.member.dto.SignUpRequest;
import com.matching.member.dto.MemberUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface MemberService {
    boolean signup(SignUpRequest parameter, List<MultipartFile> multipartFile);
    MemberResponse signIn(SignInRequest parameter);
    MemberResponse updateMember(MemberUpdateRequest parameter, Long id, List<MultipartFile> multipartFile);
    void logout(Long id);
    boolean withdraw(HttpServletRequest request, Long memberId);
}
