package be.busstop.domain.user.dto;

import be.busstop.domain.user.entity.User;
import be.busstop.domain.user.entity.UserRoleEnum;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private Long id;
    private String nickname;
    private String age;
    private String gender;
    private String profileImageUrl;
    private UserRoleEnum role;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.age = user.getAge();
        this.gender = user.getGender();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
    }
}
