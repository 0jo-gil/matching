package com.matching.member.service;

import com.matching.member.dto.MemberResponse;
import com.matching.member.dto.SignInRequest;
import com.matching.member.dto.SignUpRequest;
import com.matching.member.dto.MemberUpdateRequest;

public interface MemberService {
    boolean signup(SignUpRequest parameter);
    MemberResponse signIn(SignInRequest parameter);
    MemberResponse updateMember(MemberUpdateRequest parameter, Long id);
}
