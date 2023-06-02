package com.matching.post.controller;

import com.matching.common.dto.ResponseDto;
import com.matching.common.utils.ResponseUtil;
import com.matching.post.dto.*;
import com.matching.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/write")
    public ResponseDto savePost(
            @RequestPart(value = "file", required = false) List<MultipartFile> multipartFile,
            @RequestPart("request") PostRequest parameter,
            @AuthenticationPrincipal User user
    ) {
        String id = user.getUsername();
        Long postId = postService.writePost(parameter, id, multipartFile);

        return ResponseUtil.SUCCESS("글쓰기 성공", postId);
    }

    @GetMapping("/{postId}")
    public ResponseDto getPost(
            @PathVariable Long postId
    ) {
        PostResponse postResponse = postService.getPost(postId);
        return ResponseUtil.SUCCESS("글 조회 성공", postResponse);
    }

    @PatchMapping("/{postId}")
    public ResponseDto updatePost(
        @PathVariable Long postId,
        @RequestBody PostUpdateRequest parameter,
        @AuthenticationPrincipal User user
    ) {
        Long id = postService.updatePost(postId, Long.parseLong(user.getUsername()), parameter);

        return ResponseUtil.SUCCESS("글 수정 성공", id);
    }

    @GetMapping("/categoryList")
    public ResponseDto getPostByCategoryId(
        @RequestBody PostCategoryRequest request
    ) {
        return ResponseUtil.SUCCESS("조회성공", postService.getPostByCategoryDesc(request.getCategoryId()));
    }

    @DeleteMapping("/{postId}")
    public ResponseDto deletePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal User user
    ) {
        postService.deletePost(postId, Long.parseLong(user.getUsername()));
        return ResponseUtil.SUCCESS("삭제성공", true);
    }


    @GetMapping("/search")
    public ResponseDto getPostSearchList(
            @RequestBody @Valid PostSearchRequest parameter
    ) {
        return ResponseUtil.SUCCESS("조회성공", postService.getPostSearchList(parameter));
    }

    // 참가 중인 post 조회
    @GetMapping("/myPage/participant")
    public ResponseDto getPostByParticipant(
            @AuthenticationPrincipal User user
    ) {
        Long memberId = Long.parseLong(user.getUsername());
        return ResponseUtil.SUCCESS("목록 조회 성공", postService.getPostByParticipant(memberId));
    }

    // 작성한 post 조회
    @GetMapping("/myPage/write")
    public ResponseDto getPostByWrite(
            @AuthenticationPrincipal User user
    ) {
        Long memberId = Long.parseLong(user.getUsername());
        return ResponseUtil.SUCCESS("목록 조회 성공", postService.getPostByWrite(memberId));
    }
}
