package org.strangerlink.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.strangerlink.userservice.model.User;
import org.strangerlink.userservice.security.JwtService;
import org.strangerlink.userservice.service.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Crea o aggiorna l'utente con le informazioni di Google
        User user = userService.processOAuthUser(email, name, "google");

        // Genera token JWT
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        "",
                        Collections.emptyList()
                )
        );

        // Redirect alla homepage con il token come parametro o cookie
        String redirectUrl = "http://localhost:8080?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}