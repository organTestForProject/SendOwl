package com.example.sendowl.domain.user.util.oauth;

import com.example.sendowl.config.KakaoApiConfig;
import com.example.sendowl.domain.user.dto.Oauth2User;
import com.example.sendowl.domain.user.exception.Oauth2Exception;
import com.example.sendowl.domain.user.exception.enums.Oauth2ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.Response;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KakaoOauth {

    private final KakaoApiConfig kakaoApiConfig;

    public Oauth2User getOauth2User(String code){
        final String accessToken = this.getAccessToken(code);
        final String URL = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);
        headers.setContentType(mediaType);
        headers.add("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();

        ResponseEntity<Map> response = null;
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameter, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.exchange(URL, HttpMethod.POST, entity, Map.class);
        }catch (HttpStatusCodeException e) {
            // 카카오 인증 실패 에러
            throw new Oauth2Exception.TokenNotValid(Oauth2ErrorCode.UNAUTHORIZED);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        LinkedHashMap kakaoAccount = objectMapper.convertValue(Objects.requireNonNull(response.getBody()).get("kakao_account"), LinkedHashMap.class);


        return Oauth2User.builder()
                .email(kakaoAccount.get("email").toString()).transactionId("kakao").build();
    };
    private String getAccessToken(String code){
        final String URL = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8);
        headers.setContentType(mediaType);

        MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("grant_type", this.kakaoApiConfig.getAuthorizationGrantType());
        parameter.add("client_id", this.kakaoApiConfig.getClientId());
        parameter.add("redirect_uri", this.kakaoApiConfig.getRedirectUri());
        parameter.add("code", code);
        parameter.add("client_secret", this.kakaoApiConfig.getClientSecret());

        ResponseEntity<Map> response = null;
        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(parameter, headers);

        try{
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.exchange(URL, HttpMethod.POST,entity, Map.class);
        }catch(HttpStatusCodeException e){
            throw new Oauth2Exception.TokenNotValid(Oauth2ErrorCode.UNAUTHORIZED);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = null;

        return Objects.requireNonNull(response.getBody()).get("access_token").toString();
    }
}