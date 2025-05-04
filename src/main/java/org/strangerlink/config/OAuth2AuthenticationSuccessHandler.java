package org.strangerlink.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.strangerlink.model.User;
import org.strangerlink.security.JwtService;
import org.strangerlink.service.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            logger.info("OAuth authentication success");

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            Map<String, Object> attributes = oAuth2User.getAttributes();
            logger.info("Got OAuth user attributes");

            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");

            if (email == null) {
                logger.error("Email is null in OAuth user attributes");
                throw new RuntimeException("Email not available from OAuth provider");
            }

            // Processa l'utente OAuth
            User user = userService.processOAuthUser(email, name, "google");
            logger.info("Processed OAuth user: {}", user.getUsername());

            // Genera token JWT
            String token = jwtService.generateToken(
                    new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            "",
                            Collections.emptyList()
                    )
            );
            logger.info("Generated JWT token");

            // Due opzioni:
            // 1. Reindirizza all'app mobile o web con il token
            // String redirectUrl = "http://YOUR_FRONTEND_URL?token=" + token;

            // 2. Restituisci il token come JSON (utile per app mobili)
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"token\":\"" + token + "\",\"userId\":\"" + user.getId() +
                            "\",\"username\":\"" + user.getUsername() + "\"}"
            );

        } catch (Exception e) {
            logger.error("Error during OAuth authentication success handling", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication error: " + e.getMessage() + "\"}");
        }
    }
}