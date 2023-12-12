package be.busstop.domain.user.dto.mypage;

import be.busstop.domain.post.dto.PostResponseDto;
import be.busstop.domain.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class MypageResponseDto {

    private Long userId;
    private String nickname;
    private String age;
    private String gender;
    private String profileImageUrl;
    private List<PostResponseDto> userPosts;


    public MypageResponseDto(Long userId, String nickname, String age, String gender, String profileImageUrl, List<Post> userPosts) {
        this.userId = userId;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        // 사용자의 게시물 리스트를 PostResponseDto 리스트로 변환하여 설정합니다.
        this.userPosts = userPosts.stream()
                .map(post -> new PostResponseDto(
                        post.getId(), post.getUser().getId(),
                        post.getCategory(), post.getStatus().name(),
                        post.getTitle(), post.getUser().getNickname(),
                        post.getUser().getAge(), post.getUser().getGender(),post.getLocation(),
                        Collections.singletonList(Collections.singletonList(post.getImageUrlList().get(0)).toString()),post.getUser().getProfileImageUrl(),
                        post.getViews(), post.getCreatedAt()))
                .collect(Collectors.toList());

    }
}
