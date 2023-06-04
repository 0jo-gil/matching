package com.matching.post.dto;

import com.matching.member.domain.Member;
import com.matching.post.domain.QnaHashtag;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaPostRequest {
    @NotBlank(message = "제목은 필수입니다.")
//    @Min(value = 10)
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String body;

    private Member member;
    private List<String> hashtagList;
}
