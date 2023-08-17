package oauth2.common.oauth2;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
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

        }
        else if (userRequest.getClientRegistration().getClientName().equals("github")) {

        }
        else if (userRequest.getClientRegistration().getClientName().equals("amazon")) {

        }
        else if (userRequest.getClientRegistration().getClientName().equals("linkedin")) {

        }
        else if (userRequest.getClientRegistration().getClientName().equals("twitter")) {

        }


        userCustom.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        userCustom.setAccessToken(userRequest.getAccessToken().getTokenValue());
        userCustom.setExpiresAt(userRequest.getAccessToken().getExpiresAt());
        /*
           we can handle save user to database at her
         */
        return oAuth2User;
    }

    private OAuth2UserCustom getUserPropertiesForGoogle(OAuth2User oAuth2User) {
        return null;
    }

    private OAuth2UserCustom getUserPropertiesForKakao(OAuth2User oAuth2User) {
        Assert.notNull(oAuth2User.getAttribute("id"), "kakal's id");
        Assert.notNull(((Map<String, String>) oAuth2User.getAttribute("properties")).get("nickname"), "kakal's nickname");
        Assert.notNull(((Map<String, String>) oAuth2User.getAttribute("kakao_account")).get("email"), "kakal's email");
        return OAuth2UserCustom
                .builder()
                .id(oAuth2User.getAttribute("id").toString())
                .name(((Map<String, String>) oAuth2User.getAttribute("properties")).get("nickname"))
                .email(((Map<String, String>) oAuth2User.getAttributes().get("kakao_account")).get("email"))
                .build();
    }

    private OAuth2UserCustom getUserPropertiesForNaver(OAuth2User oAuth2User) {
        Map<String, String> naverRes = oAuth2User.getAttribute("response");
        Assert.notNull(naverRes.get("id"), "naver's id");
        Assert.notNull(naverRes.get("nickname"), "naver's nickname");
        Assert.notNull(naverRes.get("email"), "naver's email");
        return OAuth2UserCustom
                .builder()
                .id(naverRes.get("id"))
                .name(naverRes.get("nickname"))
                .email(naverRes.get("email"))
                .build();
    }
}


