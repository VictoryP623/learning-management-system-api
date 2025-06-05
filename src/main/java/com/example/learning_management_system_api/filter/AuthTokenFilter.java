package com.example.learning_management_system_api.filter;

import com.example.learning_management_system_api.component.JwtUtils;
import com.example.learning_management_system_api.config.CustomUserDetailService;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthTokenFilter extends OncePerRequestFilter {

  @Autowired private JwtUtils jwtUtils;

  @Autowired private CustomUserDetailService customUserDetailService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);

      if (isBypassUrl(request)) {
        filterChain.doFilter(request, response);
        return;
      }

      if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
        String username = jwtUtils.getUsernameFromToken(jwt);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (JwtException e) {
      ResponseVO<?> errorResponse =
          ResponseVO.error(
              HttpServletResponse.SC_UNAUTHORIZED,
              "Unauthorized: " + e.getMessage() + " (Invalid or expired token)");

      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      ObjectMapper objectMapper = new ObjectMapper();
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

      return;
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Internal Server Error: " + e.getMessage());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isBypassUrl(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    if (requestURI.contains("/update-password")) return false;
    return requestURI.startsWith("/public")
        || requestURI.contains("/api/auth")
        || requestURI.contains("/api/purchases/callback");
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
