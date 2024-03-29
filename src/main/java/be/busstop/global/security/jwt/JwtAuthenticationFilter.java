package be.busstop.global.security.jwt;

import be.busstop.domain.post.entity.Category;
import be.busstop.domain.statistics.service.LoginStaticService;
import be.busstop.domain.user.dto.LoginRequestDto;
import be.busstop.domain.user.entity.User;
import be.busstop.domain.user.entity.UserRoleEnum;
import be.busstop.domain.user.repository.UserRepository;
import be.busstop.global.redis.RedisService;
import be.busstop.global.responseDto.ApiResponse;
import be.busstop.global.responseDto.ErrorResponse;
import be.busstop.global.security.UserDetailsImpl;
import be.busstop.global.stringCode.ErrorCodeEnum;
import be.busstop.global.stringCode.SuccessCodeEnum;
import be.busstop.global.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static be.busstop.global.utils.ResponseUtils.customError;
import static be.busstop.global.utils.ResponseUtils.okWithMessage;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisRepository redisRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginStaticService loginStaticService;



    public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenRedisRepository redisRepository, UserRepository userRepository,PasswordEncoder passwordEncoder, LoginStaticService loginStaticService) {
        this.jwtUtil = jwtUtil;
        this.redisRepository = redisRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginStaticService = loginStaticService;
        setFilterProcessesUrl("/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");

        try {
            LoginRequestDto loginRequestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            UserDetailsImpl userDetails = loadUserByUsername(loginRequestDto.getNickname());

            if (userDetails == null) {
                sendErrorResponse(response, "유저 고유번호가 올바르지 않습니다.", HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }

            if (userDetails.getUser().getRole() == UserRoleEnum.BLACK) {
                ApiResponse<?> errorResponse = ApiResponse.customErrorWithNickname(ErrorCodeEnum.USER_BLACKLISTED, userDetails.getNickname());
                sendJsonResponse(response, errorResponse); // 로그인 실패 시 응답 전송
                return null; // 인증 실패이므로 null 반환
            }

            // 정상적인 경우, Spring Security의 인증 매니저에게 인증 처리를 위임
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getNickname(),
                            loginRequestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error("로그인 시도 중 예외 발생: {}", e.getMessage());
            throw new AuthenticationException("로그인 중 오류가 발생했습니다.") {
            };
        }
    }


    private void sendErrorResponse(HttpServletResponse response, String errorMessage, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .error(new ErrorResponse(errorMessage, statusCode))
                .build();

        String jsonResponse;
        try {
            jsonResponse = new ObjectMapper().writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        } catch (IOException ex) {
            log.error("에러 메시지 JSON 변환 중 예외 발생: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }


    private boolean isPasswordValid(String password, String encodedPassword) {

        return passwordEncoder.matches(password, encodedPassword);
    }

    private UserDetailsImpl loadUserByUsername(String nickname) {

        return userRepository.findByNickname(nickname)
                .map(UserDetailsImpl::new)
                .orElse(null);
    }


    /**
     * 로그인 성공 시 JWT를 생성하여 응답에 추가합니다.
     *
     * @param request    HttpServletRequest 객체
     * @param response   HttpServletResponse 객체
     * @param chain      FilterChain 객체
     * @param authResult 인증 결과 Authentication 객체
     * @throws IOException      입출력 예외가 발생한 경우
     * @throws ServletException Servlet 예외가 발생한 경우
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        ObjectMapper objectMapper = new ObjectMapper();
        User user = ((UserDetailsImpl) authResult.getPrincipal()).getUser();

        // 사용자 정보 가져오기
        String userCode = user.getUserCode();
        String nickname = user.getNickname();
        Long userId = user.getId();
        String age = user.getAge();
        String gender = user.getGender();
        UserRoleEnum role = user.getRole();
        String profileImageUrl = user.getProfileImageUrl();
        String interest = String.valueOf(user.getInterest());

        String token = jwtUtil.createToken(String.valueOf(userId),userCode, nickname, age, gender, role, profileImageUrl, Category.valueOf(interest));
        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(userId),userCode, nickname, age, gender, role,Category.valueOf(interest), profileImageUrl);
        jwtUtil.addJwtHeaders(token,refreshToken, response);


        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(userCode)
                .refreshToken(refreshToken)
                .build();
        redisRepository.save(refresh);

        loginStaticService.updateLoginStatic();

        user.updateLastAccessed();
        userRepository.save(user);

        ApiResponse<?> apiResponse = okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        response.setStatus(HttpServletResponse.SC_OK);

    }


    /**
     * 로그인 실패 시 실패 응답을 반환합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param failed   AuthenticationException 객체
     * @throws IOException      입출력 예외가 발생한 경우
     * @throws ServletException Servlet 예외가 발생한 경우
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");
        ObjectMapper objectMapper = new ObjectMapper();

        // 로그인 실패 응답 반환
        ApiResponse<?> apiResponse = customError(ErrorCodeEnum.LOGIN_FAIL);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private void sendJsonResponse(HttpServletResponse response, ApiResponse<?> apiResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
        response.getWriter().flush();
    }

}
