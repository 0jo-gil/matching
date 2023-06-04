package com.matching.post.domain;

import com.matching.common.domain.BaseEntity;
import com.matching.member.domain.Member;
import com.matching.post.dto.QnaPostRequest;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@AuditOverride(forClass = BaseEntity.class)
public class QnaPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String body;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member author;

    @OneToMany(mappedBy = "qnaPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaHashtag> hashtags = new ArrayList<>();



    public static QnaPost from(QnaPostRequest parameter) {
        return QnaPost.builder()
                .title(parameter.getTitle())
                .body(parameter.getBody())
                .author(parameter.getMember())
                .build();
    }
}
