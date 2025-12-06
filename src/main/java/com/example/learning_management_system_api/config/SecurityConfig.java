package com.example.learning_management_system_api.config;

import com.example.learning_management_system_api.component.JwtAuthEntryPoint;
import com.example.learning_management_system_api.filter.AuthTokenFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Autowired CustomUserDetailService customUserDetailService;
  @Autowired JwtAuthEntryPoint authEntryPoint;

  // FE origin (override bằng app.CLIENT_URL)
  @Value("${app.CLIENT_URL:http://localhost:3000}")
  private String clientUrl;

  // Chế độ CSP: dev | prod (đặt trong application-*.properties)
  @Value("${app.security.csp.mode:dev}")
  private String cspMode;

  private static final List<String> SWAGGER_WHITELIST =
      List.of("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");

  private static final List<String> ACTUATOR_WHITELIST = List.of("/actuator/health", "/api/health");

  private final List<String> PUBLIC_URLS =
      List.of(
          "/api/auth/**",
          "/api/purchases/callback",
          "/api/test/firebase/**",
          "/api/admin/register",
          "/api/courses",
          "/api/categories/**");

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public AuthTokenFilter authTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    var authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Preflight
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // WebSocket handshake endpoint (SockJS/STOMP)
                    .requestMatchers("/ws/**")
                    .permitAll()
                    // Swagger / health
                    .requestMatchers(SWAGGER_WHITELIST.toArray(String[]::new))
                    .permitAll()
                    .requestMatchers(ACTUATOR_WHITELIST.toArray(String[]::new))
                    .permitAll()
                    // Public APIs khác
                    .requestMatchers(PUBLIC_URLS.toArray(String[]::new))
                    .permitAll()
                    // Tuỳ bạn: tạm thời permit để test Notification REST, hoặc để authenticated
                    // .requestMatchers("/api/notifications/**").permitAll()
                    .anyRequest()
                    .authenticated());

    // === Security Headers: dev vs prod ===
    http.headers(
        headers -> {
          if ("prod".equalsIgnoreCase(cspMode)) {
            // -------- PROD: siết chặt, cho phép kết nối realtime wss/ws --------
            headers
                .contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            String.join(
                                "; ",
                                "default-src 'self'",
                                "img-src 'self' data:",
                                "media-src 'self'",
                                "font-src 'self'",
                                "style-src 'self'",
                                "script-src 'self'",
                                // Cho phép kết nối từ FE + realtime ws/wss (cùng origin hoặc khác)
                                "connect-src 'self' "
                                    + clientUrl.replace("http://", "https://")
                                    + " ws: wss:",
                                "frame-ancestors 'self'",
                                "object-src 'none'",
                                "base-uri 'self'",
                                "form-action 'self'")))
                .referrerPolicy(
                    rp ->
                        rp.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(cto -> {})
                .addHeaderWriter(
                    new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
                .addHeaderWriter(
                    new StaticHeadersWriter("Cross-Origin-Embedder-Policy", "require-corp"))
                .addHeaderWriter(
                    new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
                .httpStrictTransportSecurity(
                    hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000));
          } else {
            // -------- DEV: nới cho HMR/Swagger, thuận phát triển + ws realtime --------
            headers
                .contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            String.join(
                                "; ",
                                "default-src 'self'",
                                "img-src 'self' data: blob:",
                                "media-src 'self' blob:",
                                "font-src 'self' data:",
                                "style-src 'self' 'unsafe-inline'",
                                "script-src 'self' 'unsafe-inline'",
                                // Cho REST & HMR & realtime ws tới FE và BE
                                "connect-src 'self' "
                                    + "http://localhost:5173 http://localhost:3000 "
                                    + "ws://localhost:5173 ws://localhost:3000 "
                                    + "ws: wss:",
                                "frame-ancestors 'self'",
                                "object-src 'none'",
                                "base-uri 'self'",
                                "form-action 'self'")))
                .referrerPolicy(
                    rp ->
                        rp.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(cto -> {})
                // COOP/COEP tắt ở dev để tránh chặn resource bên thứ ba
                .addHeaderWriter(
                    new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"));
          }
        });

    http.authenticationProvider(daoAuthenticationProvider());
    http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var origins = Arrays.asList(clientUrl, "http://localhost:3000", "http://localhost:5173");

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
