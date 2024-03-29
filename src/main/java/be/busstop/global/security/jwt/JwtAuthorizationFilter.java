package be.busstop.global.security.jwt;

import be.busstop.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증, 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRedisRepository redisRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String tokenValue = jwtUtil.getTokenFromHeader(req);
        String refreshTokenValue = jwtUtil.getRefreshTokenFromHeader(req); // 헤더에서 리프레시 토큰을 가져옴

        if (StringUtils.hasText(tokenValue)) {
            tokenValue = jwtUtil.substringHeaderToken(tokenValue);
            if (!jwtUtil.validateToken(tokenValue)) {
                // 액세스 토큰이 만료된 경우 새로운 액세스 토큰 발급
                if (StringUtils.hasText(refreshTokenValue)) {
                    jwtUtil.refreshAccessToken(refreshTokenValue, res);
                    // 리프레시 토큰을 통한 새로운 액세스 토큰 발급
                }
            }
            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);
            // 액세스 토큰이 유효한 경우 인증진행
            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error("사용자 인증 설정 중 에러 발생: " + e.getMessage());
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /**
     * 주어진 사용자 이름을 기반으로 인증을 설정합니다.
     *
     * @param username 사용자 이름
     */
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    /**
     * 인증 객체를 생성합니다.
     *
     * @param userCode 사용자 고유식별번호
     * @return 인증 객체
     */
    private Authentication createAuthentication(String userCode) {
        UserDetails userDetails = userDetailsService.loadUserByUserCode(userCode);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}