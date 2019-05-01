package za.co.joshuabakerg.detail.detailservice.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class AuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

    public AuthenticationTokenFilter() {
        super("/**");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        AntPathRequestMatcher loginPageMatcher = new AntPathRequestMatcher("/login", "POST");

        Authentication token = getAuthentication(loginPageMatcher, httpServletRequest);

        Authentication authenticate = token;

        AuthenticationException lastException = null;

        try {
            authenticate = getAuthenticationManager().authenticate(token);
        } catch (AuthenticationException e) {
            lastException = e;
        }
        if (loginPageMatcher.matches(httpServletRequest)) {
            Object details = authenticate.getDetails();
            if (details instanceof Map) {
                String jwtToken = (String) ((Map) details).get("token");
                httpServletResponse.setHeader("Authorization", jwtToken);
            } else {
                throw lastException;
            }
        }
        return authenticate;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }

    private Authentication getAuthentication(RequestMatcher requestMatcher, HttpServletRequest httpServletRequest) {
        if (requestMatcher.matches(httpServletRequest)) {
            String username = httpServletRequest.getParameter("username");
            String password = httpServletRequest.getParameter("password");
            return new UsernamePasswordAuthenticationToken(username, password);
        } else {
            return new JWTAuthentication(httpServletRequest.getHeader("Authorization"));
        }
    }

}
