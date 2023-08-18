package oauth2.common.oauth2;

import org.springframework.http.*;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
    To see common oauth provider, visit this class {@link CommonOAuth2Provider}
 **/
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final RestOperations restOperations = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Role generate
        OAuth2UserCustom userCustom = OAuth2UserCustom.builder().build();
        if (userRequest.getClientRegistration().getClientName().equals("kakao"))
            userCustom = getUserPropertiesForKakao(oAuth2User);
        else if (userRequest.getClientRegistration().getClientName().equals("naver"))
            userCustom = getUserPropertiesForNaver(oAuth2User);
        else if (userRequest.getClientRegistration().getClientName().equals("facebook")) {

        } else if (userRequest.getClientRegistration().getClientName().equals("google")) {
            userCustom = getUserPropertiesForGoogle(oAuth2User);
        } else if (userRequest.getClientRegistration().getClientName().equals("apple")) {

        } else if (userRequest.getClientRegistration().getClientName().equals("github")) {
            userCustom = getUserPropertiesForGithub(userRequest, oAuth2User);
        } else if (userRequest.getClientRegistration().getClientName().equals("amazon")) {

        } else if (userRequest.getClientRegistration().getClientName().equals("linkedin")) {

        } else if (userRequest.getClientRegistration().getClientName().equals("twitter")) {

        }


        Objects.requireNonNull(userCustom, "Oauth2 userCustom");
        userCustom.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        userCustom.setAccessToken(userRequest.getAccessToken().getTokenValue());
        userCustom.setExpiresAt(userRequest.getAccessToken().getExpiresAt());
        /*
           we can handle save user to database at her
         */
        return oAuth2User;
    }


    public AuthenticationSuccessHandler onLoginSuccess() {
        return ((request, response, authentication) -> {

            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

            String id = defaultOAuth2User.getAttributes().get("id").toString();
            String body = """
                    {"id":"%s"}
                    """.formatted(id);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            PrintWriter writer = response.getWriter();
            writer.println(body);
        });
    }


    private OAuth2UserCustom getUserPropertiesForKakao(OAuth2User oAuth2User) {
        Map<String, ?> kakaoProperties = oAuth2User.getAttribute("properties");
        Map<String, ?> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        Objects.requireNonNull(kakaoProperties, "Kakao properties");
        Objects.requireNonNull(kakaoAccount, "Kakao account");
        return OAuth2UserCustom
                .builder()
                .id(Objects.requireNonNull(oAuth2User.getAttribute("id"), "Kakao's id"))
                .name(Objects.requireNonNull(kakaoProperties.get("nickname").toString(), "Kakao's nickname"))
                .email(Objects.requireNonNull(kakaoAccount.get("email").toString(), "Kakao's email"))
                .build();
    }

    private OAuth2UserCustom getUserPropertiesForNaver(OAuth2User oAuth2User) {
        Map<String, String> naverRes = oAuth2User.getAttribute("response");
        Objects.requireNonNull(naverRes, "Naver's attributes");
        return OAuth2UserCustom
                .builder()
                .id(Objects.requireNonNull(naverRes.get("id"), "Naver's id"))
                .name(Objects.requireNonNull(naverRes.get("nickname"), "Naver's  nickName"))
                .email(Objects.requireNonNull(naverRes.get("email"), "Naver's email"))
                .build();
    }

    private OAuth2UserCustom getUserPropertiesForGoogle(OAuth2User oAuth2User) {
        return null;
    }

    private OAuth2UserCustom getUserPropertiesForGithub(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // because some GitHub user doesn't want to show their email, we have to call another API to get that email
        String email = null;
        try {
            ClientRegistration clientRegistration = userRequest.getClientRegistration();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            URI uri = UriComponentsBuilder
                    .fromUriString(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri() + "/emails").build().toUri();
            RequestEntity<?> request;
            headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
            request = new RequestEntity<>(headers, HttpMethod.GET, uri);
            List<?> emailRes = Objects.requireNonNull(this.restOperations.exchange(request, List.class).getBody(), "Github email");
            if (emailRes.size() > 0)
                email = ((Map<String, ?>) emailRes.get(0)).get("email").toString();
        } catch (Exception ex) {
            throw new RuntimeException("Github user's email");
        }

        return OAuth2UserCustom
                .builder()
                .id(Objects.requireNonNull(oAuth2User.getAttribute("id"), "Github's id").toString())
                .name(Objects.requireNonNull(oAuth2User.getAttribute("login"), "Github's login name"))
                .email(Objects.requireNonNull(email, "Github's email"))
                .build();
    }
}


