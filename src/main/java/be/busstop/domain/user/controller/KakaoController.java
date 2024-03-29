package be.busstop.domain.user.controller;

import be.busstop.domain.statistics.service.LoginStaticService;
import be.busstop.domain.user.entity.User;
import be.busstop.domain.user.entity.UserRoleEnum;
import be.busstop.domain.user.service.KakaoService;
import be.busstop.global.responseDto.ApiResponse;
import be.busstop.global.stringCode.ErrorCodeEnum;
import be.busstop.global.stringCode.SuccessCodeEnum;
import be.busstop.global.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "카카오 API", description = "로그인")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final LoginStaticService loginStaticService;

    @Operation(summary = "인가코드 전달 받은 후 카카오로부터 사용자 정보 발급 -> 최종 토큰 반환")
    @Transactional
    @PostMapping("/kakao")
    public ApiResponse<?> kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("카카오 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 카카오 로그인에 성공한 후, 사용자 정보 가져오기
        User user = kakaoService.kakaoSignUpOrLinkUser(code);

        if (user.getRole() == UserRoleEnum.BLACK) {
            // 차단된 유저의 닉네임을 함께 반환
            String blockedUserNickname = user.getNickname();
            return ApiResponse.customErrorWithNickname(ErrorCodeEnum.USER_BLACKLISTED, blockedUserNickname);
        }

        log.info("카카오 로그인 성공. 유저 ID: {}", user.getId());
        kakaoService.addToken(user, response);
        loginStaticService.updateLoginStatic();
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }
}