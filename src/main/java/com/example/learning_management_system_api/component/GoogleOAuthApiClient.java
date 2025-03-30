package com.example.learning_management_system_api.component;

import com.example.learning_management_system_api.dto.response.GoogleUserInfo;
import com.example.learning_management_system_api.exception.AppException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthApiClient {
  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String GOOGLE_CLIENT_ID;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String CLIENT_SECRET;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String REDIRECT_URI;

  @Autowired private RestTemplate restTemplate;

  public String exchangeCodeForAccessToken(String code) {
    String tokenUri = "https://oauth2.googleapis.com/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    String decodedCode = "";
    try {
      decodedCode = java.net.URLDecoder.decode(code, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new AppException(400, "invalid code");
    }

    params.add("code", decodedCode);
    params.add("client_id", GOOGLE_CLIENT_ID);
    params.add("client_secret", CLIENT_SECRET);
    params.add("redirect_uri", REDIRECT_URI);
    params.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

    if (response.getStatusCode() == HttpStatus.OK) {
      return (String) response.getBody().get("access_token");
    } else {
      throw new AppException(400, "Cannot exchange code for access token");
    }
  }

  public GoogleUserInfo fetchGoogleUserInfo(String accessToken) {
    String userInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> request = new HttpEntity<>(headers);
    ResponseEntity<GoogleUserInfo> response =
        restTemplate.exchange(userInfoUri, HttpMethod.GET, request, GoogleUserInfo.class);

    if (response.getStatusCode() == HttpStatus.OK) {
      return response.getBody();
    } else {
      throw new AppException(400, "Cannot fetch user info from Google");
    }
  }
}
