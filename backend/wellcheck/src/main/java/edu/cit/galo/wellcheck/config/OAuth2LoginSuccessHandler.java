package edu.cit.galo.wellcheck.config;

import edu.cit.galo.wellcheck.entity.User;
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
import java.util.Optional;

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

        // Parse state: format is <original_state>:<role>:<redirectUri or empty>
        String state = request.getParameter("state");
        String role = "STUDENT";
        String baseRedirect = frontendUrl + "/auth/callback";

        if (state != null && state.contains(":")) {
            String[] parts = state.split(":", 3);
            // parts[0] = original state
            // parts[1] = role
            // parts[2] = redirectUri (may be empty)
            if (parts.length >= 2) {
                role = parts[1];
            }
            if (parts.length >= 3 && !parts[2].isBlank()) {
                baseRedirect = parts[2]; // use mobile redirect_uri
            }
        }

        if (!(principal instanceof OAuth2User oauth2User)) {
            response.sendRedirect(baseRedirect + "?error=" + urlEncode("Invalid OAuth2 principal"));
            return;
        }

        try {
            String rawEmail = oauth2User.getAttribute("email");
            String normalizedEmail = rawEmail.trim().toLowerCase();
            boolean isNewUser = !userRepository.existsByEmail(normalizedEmail);

            String status = "ACTIVE";

            // For existing users, override role and status from DB
            if (!isNewUser) {
                Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
                role = existingUser.map(u -> u.getRole().name()).orElse("STUDENT");
                status = existingUser.map(u -> u.getStatus().name()).orElse("ACTIVE");
            }

            String token = authService.authenticateWithGoogleOAuth2User(oauth2User);
            String email = urlEncode(rawEmail);
            String firstName = urlEncode(oauth2User.getAttribute("given_name"));
            String lastName = urlEncode(oauth2User.getAttribute("family_name"));

            response.sendRedirect(baseRedirect
                    + "?token=" + urlEncode(token)
                    + "&email=" + email
                    + "&firstName=" + firstName
                    + "&lastName=" + lastName
                    + "&isNewUser=" + isNewUser
                    + "&role=" + role
                    + "&status=" + status);

        } catch (Exception e) {
            response.sendRedirect(baseRedirect + "?error=" + urlEncode(e.getMessage()));
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}