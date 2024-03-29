package be.busstop.global.security.jwt;

import be.busstop.domain.post.entity.Category;
import be.busstop.domain.user.entity.UserRoleEnum;
import be.busstop.domain.user.repository.UserRepository;
import be.busstop.global.redis.RedisService;
import be.busstop.global.utils.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESHTOKEN_HEADER = "RefreshToken";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 1주일

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final EncryptionUtils encryptionUtils;

    // @Value("${jwt.secret.key}")
    @Value(value = "${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private ObjectMapper customObjectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }


    public String substringHeaderToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(7);
        }
        throw new NullPointerException("유효한 토큰이 아닙니다.");
    }


    public void addJwtHeader(String token, HttpServletResponse response) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20");
            response.setHeader(AUTHORIZATION_HEADER, token);
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
        }
    }

    public void addJwtHeaders(String accessToken, String refreshToken, HttpServletResponse response) {
        try {
            accessToken = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
            refreshToken = URLEncoder.encode(refreshToken, "utf-8").replaceAll("\\+", "%20");

            response.setHeader(AUTHORIZATION_HEADER, accessToken);
            response.setHeader(REFRESHTOKEN_HEADER, refreshToken);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
    }

    public String getUserCodeFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("sub", String.class);
    }
    public String getNickNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("nickname", String.class);
    }
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("userId", String.class);
    }
    public String getProfileImageUrlFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("profileImageUrl", String.class);
    }

    public String getTokenFromHeader(HttpServletRequest req) {
        String token = req.getHeader(AUTHORIZATION_HEADER);
        if (token != null) {
            try {
                return URLDecoder.decode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public String getRefreshTokenFromHeader(HttpServletRequest req) {
        String refreshToken = req.getHeader(REFRESHTOKEN_HEADER);
        if (refreshToken != null) {
            try {
                return URLDecoder.decode(refreshToken, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public String createToken(String userId, String userCode, String nickname, String age, String gender, UserRoleEnum role, String profileImageUrl, Category interest) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(userCode)
                        .claim("nickname", nickname)
                        .claim("userId", userId)
                        .claim("age", age)
                        .claim("gender", gender)
                        .claim(AUTHORIZATION_KEY, role)
                        .claim("interest", interest)
                        .claim("profileImageUrl", profileImageUrl)
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRE_TIME)) // 만료시간
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    public String createRefreshToken(String userId, String userCode,String nickname, String age, String gender,  UserRoleEnum role, Category interest, String profileImageUrl) {
        Date date = new Date();
        String refreshToken = Jwts.builder()
                .setSubject(userCode)
                .claim("nickname", nickname)
                .claim("userId", userId)
                .claim("age", age)
                .claim("gender", gender)
                .claim(AUTHORIZATION_KEY, role)
                .claim("interest", interest)
                .claim("profileImageUrl", profileImageUrl)
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRE_TIME)) // 만료시간
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();

        try {
            return encryptionUtils.encrypt(refreshToken); // encryptionUtils 인스턴스를 통해 encrypt 메서드 호출
        } catch (Exception e) {
            log.error("리프레시 토큰 암호화 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰 암호화 실패");
        }
    }
    // JwtProvider 클래스에 추가 메소드
    public String decryptRefreshToken(String encryptedRefreshToken) {
        try {
            return encryptionUtils.decrypt(encryptedRefreshToken);
        } catch (Exception e) {
            log.error("리프레시 토큰 복호화 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰 복호화 실패");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }


    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }


    public String createAccessTokenFromRefreshToken(String refreshToken) {
        try {
            refreshToken = refreshToken.replace("Bearer ", ""); // Bearer 접두사 제거

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .deserializeJsonWith(new JacksonDeserializer<>(customObjectMapper))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String id = claims.getSubject();
            UserRoleEnum role = UserRoleEnum.valueOf(claims.get(AUTHORIZATION_KEY, String.class));
            Date date = new Date();

            String newAccessToken = BEARER_PREFIX +
                    Jwts.builder()
                            .setSubject(id) // 사용자 식별
                            .claim("nickname", claims.get("nickname",String.class))
                            .claim("userId", claims.get("userId", String.class)) // userId 추가
                            .claim("age", claims.get("age",String.class))
                            .claim("gender",claims.get("gender",String.class))
                            .claim(AUTHORIZATION_KEY, role)
                            .claim("interest", claims.get("interest",String.class))
                            .claim("profileImageUrl", claims.get("profileImageUrl", String.class))
                            .setExpiration(new Date(date.getTime() + 5 * 60 * 1000L)) // 만료 시간
                            .setIssuedAt(date)
                            .signWith(key, signatureAlgorithm)
                            .compact();

            log.info("리프레시 토큰으로부터 새로운 액세스 토큰 생성");
            return newAccessToken;
        } catch (Exception e) {
            log.error("리프레시 토큰으로부터 액세스 토큰 생성 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰으로부터 액세스 토큰 생성 실패");
        }
    }

    public void refreshAccessToken(String refreshTokenValue, HttpServletResponse response) {
        if (StringUtils.hasText(refreshTokenValue)) {
            String decryptedRefreshToken = decryptRefreshToken(refreshTokenValue);
            log.info("리프레시 토큰 넘어왔나?={}", decryptedRefreshToken);

            if (StringUtils.hasText(decryptedRefreshToken) && validateToken(decryptedRefreshToken)) {
                String redisStoredRefreshToken = getRefreshTokenFromRedis(decryptedRefreshToken);

                if (redisStoredRefreshToken != null && validateToken(redisStoredRefreshToken)) {
                    String RefreshTokenByUserId = getUserIdFromToken(decryptedRefreshToken);
                    String RefreshTokenByUserCode = getUserCodeFromToken(decryptedRefreshToken);

                    String redisRfTokenByUserId = getUserIdFromToken(redisStoredRefreshToken);
                    String redisRfTokenByUserCode = getUserCodeFromToken(redisStoredRefreshToken);

                    log.info("레디스에서 리프레시 토큰 넘어왔나?={}", redisStoredRefreshToken);

                    if (RefreshTokenByUserId.equals(redisRfTokenByUserId)
                            && RefreshTokenByUserCode.equals(redisRfTokenByUserCode)) {
                        String newAccessToken = createAccessTokenFromRefreshToken(decryptedRefreshToken);
                        addJwtHeader(newAccessToken, response);
                        log.info("리프레시 토큰으로 새로운 액세스 토큰 발급");
                    } else {
                        log.error("레디스에 저장된 리프레시 토큰과 정보가 일치하지 않습니다.");
                    }
                } else {
                    log.error("레디스에 저장된 리프레시 토큰이 없습니다.");
                }
            } else {
                log.error("리프레시 토큰이 유효하지 않습니다.");
            }
        } else {
            log.error("리프레시 토큰이 없습니다.");
        }
    }



    public String getRefreshTokenFromRedis(String decryptedRefreshToken) {
        String refreshToken = null;
        try {
            Claims claims = getUserInfoFromToken(decryptedRefreshToken);

            if (claims != null) {
                String refreshTokenKey = claims.getSubject(); // 리프레시 토큰 키 생성
                log.info("레디스에서 키 값으로 값 조회를 시도 중: " + refreshTokenKey);
                refreshToken = refreshTokenRedisRepository.findById(refreshTokenKey)
                        .map(RefreshToken::getRefreshToken)
                        .orElse(null);
                if (refreshToken != null) {
                    log.info("레디스에서 리프레시 토큰 조회 성공");
                    // 리프레시 토큰 복호화를 위한 부분 추가
                    refreshToken = decryptRefreshToken(refreshToken);
                } else {
                    log.error("레디스에서 리프레시 토큰을 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            log.error("레디스에서 리프레시 토큰 조회 실패 : {}", e.getMessage());
        }
        log.info("리프레시 토큰 조회 성공");
        return refreshToken;
    }


    public void expireAccessToken(String token, HttpServletResponse response) {
        try {
            // 디코딩된 토큰 추출
            String decodedToken = URLDecoder.decode(token, "UTF-8");
            // "Bearer " 제거
            String cleanToken = decodedToken.replace("Bearer ", "");
            Claims claims = getUserInfoFromToken(cleanToken);
            if (claims != null) {
                // 기존 토큰의 만료 시간을 현재 시간으로 설정하여 즉시 만료
                Date expiration = new Date();

                // 기존 토큰을 업데이트하여 만료시킴
                String expireAccessToken =
                        BEARER_PREFIX +
                                Jwts.builder()
                                        .setClaims(claims)
                                        .setExpiration(expiration)
                                        .signWith(key, signatureAlgorithm)
                                        .compact();

                // 새로 생성된 액세스 토큰으로 헤더 업데이트
                addJwtHeader(expireAccessToken, response);

                log.info("액세스 토큰을 강제로 만료시킴");
            }
        } catch (Exception e) {
            log.error("액세스 토큰 만료 실패: {}", e.getMessage());
            throw new RuntimeException("액세스 토큰 만료 실패");
        }
    }
}

