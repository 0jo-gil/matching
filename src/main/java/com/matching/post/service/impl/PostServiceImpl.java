package com.matching.post.service.impl;

import com.matching.common.config.JwtTokenProvider;
import com.matching.member.domain.Member;
import com.matching.member.repository.MemberRepository;
import com.matching.plan.domain.Plan;
import com.matching.plan.repository.PlanRepository;
import com.matching.post.domain.Post;
import com.matching.post.dto.PostRequest;
import com.matching.post.dto.PostResponse;
import com.matching.post.repository.PostRepository;
import com.matching.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;


    @Transactional
    @Override
    public Long writePost(PostRequest parameter, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String email = jwtTokenProvider.getMemberEmailByToken(token);

        Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        parameter.setMember(member);

        Post post = postRepository.save(Post.from(parameter));

        return post.getId();
    }

    @Override
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        return PostResponse.from(post);
    }


    @Transactional
    @Override
    public Long participate(String email, Long postId) {
        Member participant = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        return planRepository.save(Plan.from(participant, post)).getId();
    }

    @Transactional
    @Override
    public void completePlan(String email, Long planId) {
        Member participant = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 플랜이 존재하지 않습니다."));

        if(plan.getParticipant().equals(participant)) {
            throw new IllegalArgumentException("해당 참가신청은 존재하지 않습니다.");
        }

        plan.setCompleted(true);
        planRepository.save(plan);
    }
}
