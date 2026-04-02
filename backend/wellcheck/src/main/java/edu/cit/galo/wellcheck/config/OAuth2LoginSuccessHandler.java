package edu.cit.galo.wellcheck.config;

import edu.cit.galo.wellcheck.repository.UserRepository;
import edu.cit.galo.wellcheck.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof OAuth2User oauth2User)) {
            response.sendRedirect(frontendUrl + "/auth/callback?error=" + urlEncode("Invalid OAuth2 principal"));
            return;
        }

        try {
            String rawEmail = oauth2User.getAttribute("email");
            String normalizedEmail = rawEmail.trim().toLowerCase();
            boolean isNewUser = !userRepository.existsByEmail(normalizedEmail);

            String token = authService.authenticateWithGoogleOAuth2User(oauth2User);
            String email = urlEncode(rawEmail);
            String firstName = urlEncode(oauth2User.getAttribute("given_name"));
            String lastName = urlEncode(oauth2User.getAttribute("family_name"));

            response.sendRedirect(frontendUrl
                    + "/auth/callback"
                    + "?token=" + urlEncode(token)
                    + "&email=" + email
                    + "&firstName=" + firstName
                    + "&lastName=" + lastName
                    + "&isNewUser=" + isNewUser);

        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/auth/callback?error=" + urlEncode(e.getMessage()));
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}