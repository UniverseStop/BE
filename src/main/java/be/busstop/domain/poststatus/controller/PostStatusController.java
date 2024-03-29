package be.busstop.domain.poststatus.controller;


import be.busstop.domain.poststatus.service.PostStatusService;
import be.busstop.domain.user.entity.User;
import be.busstop.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "게시글 상태", description = "IN_PROGRESS or COMPLETED로 상태 변화")
@Controller
@RequestMapping("/post/{postId}")
@RequiredArgsConstructor
public class PostStatusController {

    private final PostStatusService postStatusService;
    @Operation(summary = "게시글 마감")
    @PostMapping
    public ResponseEntity<?> updatePostStatus(@PathVariable Long postId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        User user = userDetailsImpl.getUser();
        return ResponseEntity.ok(postStatusService.updatePostStatus(postId, user));
    }
}
