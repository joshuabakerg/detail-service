package za.co.joshuabakerg.detail.detailservice.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;

public class JWTAuthentication extends UsernamePasswordAuthenticationToken {

    public JWTAuthentication(String jwtToken) {
        super(null, jwtToken);
    }

    public String getJwtToken(){
        return (String) getCredentials();
    }
}
