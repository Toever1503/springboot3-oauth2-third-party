package oauth2.common.security;

import lombok.RequiredArgsConstructor;
import oauth2.common.oauth2.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/admin/**").authenticated()
                                .anyRequest().permitAll()
                );
        http.formLogin(login -> login.loginPage("/"));

        http.oauth2Login(
                oauth2Configurer ->
                        oauth2Configurer
                                .loginPage("/login")
//                                .successHandler(oAuth2UserService.onLoginSuccess())
                                .userInfoEndpoint((t) ->  t.userService(oAuth2UserService))
//                .userService(oAuth2UserService)
        );

        return http.build();
    }

}