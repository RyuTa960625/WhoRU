package com.ssafy.whoru.global.common.filter;

import com.ssafy.whoru.domain.member.dto.CustomOAuth2User;
import com.ssafy.whoru.domain.member.dto.MemberDTO;
import com.ssafy.whoru.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // request에서 토큰 추출 ex) Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
        String token = getAccessToken(request.getHeader(
                HEADER_AUTHORIZATION));

        // 토큰이 없다면 다음 필터로 넘김
        if (token == null) {
            log.info("token is null");
            filterChain.doFilter(request, response);
            log.info("token is null to after");
            return;
        }

        //토큰 검증 ㄱㄱ access 토큰
        try {
            log.info("validate token");
            jwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token 만료됨");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String category = jwtUtil.getCategory(token);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("유효하지 않은 access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //토큰에서 userId 와 MemberIdentifier 획득
        Long userId = jwtUtil.getUserId(token);
        String memberIdentifier = jwtUtil.getMemberIdentifier(token);

        //Member DTO를 생성하여 값 set
        MemberDTO memberDTO = MemberDTO
                .builder()
                .id(userId)
                .memberIdentifier(memberIdentifier)
                .build();

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(memberDTO);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null);

        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("success");
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {
        // jwt 토큰 해더가 있다면 토큰 반환
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        // 없으면 null
        return null;
    }
}
