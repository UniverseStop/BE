package be.busstop.domain.post.dto;

import be.busstop.domain.post.entity.Category;
import be.busstop.domain.post.entity.Post;
import be.busstop.domain.post.entity.PostApplicant;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto {

    private Long id;
    private Long userId;
    private Category category;
    private String title;
    private String nickname;
    private String age;
    private String gender;
    private String content;
    private LocalDateTime createdAt;
    private List<String> imageUrlList;
    private String thumbnailImageUrl;
    private String endDate;
    private String endTime;
    private String status;
    private Boolean isComplete;
    private Boolean isAlreadyApplicant;
    private Boolean isParticipants;
    private int views;
    private String location;
    private String profileImageUrl;
    private String chatroomId;
    private List<Map<String, String>> chatParticipants;
    private List<PostApplicant> applicants;
    private float lng;
    private float lat;
    private String placeName;

    // 전체 조회
    @QueryProjection
    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.category = post.getCategory();
        this.views = post.getViews();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.age = post.getUser().getAge();
        this.gender = post.getUser().getGender();
        this.thumbnailImageUrl = post.getThumbnailImageUrl();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.endDate = post.getEndDate();
        this.endTime = post.getEndTime();
        this.location = post.getLocation();
        this.profileImageUrl = post.getUser().getProfileImageUrl();
        this.lat = post.getLat();
        this.lng = post.getLng();
        this.placeName = post.getPlaceName();
    }

    // 상세 조회
    public PostResponseDto(Post post, Boolean isComplete, Boolean isAlreadyApplicant,Boolean isParticipants, List<Map<String, String>> chatParticipants, List<PostApplicant> applicants){
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.category = post.getCategory();
        this.views = post.getViews();
        this.title = post.getTitle();
        this.nickname = post.getUser().getNickname();
        this.age = post.getUser().getAge();
        this.gender = post.getUser().getGender();
        this.content = post.getContent();
        this.endDate = post.getEndDate();
        this.endTime = post.getEndTime();
        this.createdAt = post.getCreatedAt();
        this.imageUrlList = post.getImageUrlList().stream()
                .map(String::new)
                .collect(Collectors.toList());
        this.location = post.getLocation();
        this.profileImageUrl = post.getUser().getProfileImageUrl();
        this.isComplete = isComplete;
        if (post.getStatus() != null) {
            this.status = post.getStatus().name();
        }
        this.isAlreadyApplicant = isAlreadyApplicant;
        this.isParticipants = isParticipants;
        this.chatroomId = post.getChatroomId();
        this.chatParticipants = chatParticipants;
        this.applicants = applicants;
        this.lng = post.getLng();
        this.lat = post.getLat();
        this.placeName = post.getPlaceName();
    }

    // 랜덤 조회
    public PostResponseDto(Long id, Long userId, Category category, String title,
                               String nickname, String age, String gender,
                               LocalDateTime createdAt, String thumbnailImageUrl,
                               String endDate,String endTime, String location, String profileImageUrl) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.title = title;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.createdAt = createdAt;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.endDate = endDate;
        this.endTime = endTime;
        this.location = location;
        this.profileImageUrl = profileImageUrl;
    }
    // 마이페이지
    public PostResponseDto(Long id, Long userId, Category category,String status, String title,
                           String nickname, String age, String gender,
                       String location,String endDate, String thumbnailImageUrl, String profileImageUrl,int views, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.status = status;
        this.title = title;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.location = location;
        this.endDate = endDate;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.profileImageUrl = profileImageUrl;
        this.views = views;
        this.createdAt = createdAt;
    }
}

