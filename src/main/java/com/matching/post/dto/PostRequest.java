package com.matching.post.dto;

import com.matching.member.domain.Member;
import com.matching.post.domain.Category;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    /**
     * 글쓰기
     */
    private String title;
    private String content;
    private Member member;
    private Long categoryId;

    /**
     * 목표
     */
    private String detail;
    private LocalDate startedAt;
    private LocalDate endedAt;

}
