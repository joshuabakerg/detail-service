package za.co.joshuabakerg.detail.detailservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {

    private Map<String, Map> tokenCache = new HashMap<>();

    private OkHttpClient client;
    private ObjectMapper objectMapper;
    private String authServer;

    public AuthenticationProviderImpl(final OkHttpClient client,
                                      final ObjectMapper detailServiceObjectMapper,
                                      final String authServer) {
        this.client = client;
        this.objectMapper = detailServiceObjectMapper;
        this.authServer = authServer;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String userToken = getUserToken(authentication);

        if(userToken == null){
            return null;
        }

        Map mapUser = tokenCache.get(userToken);
        if(mapUser == null) {
            mapUser = getUserFromToken(userToken);
            tokenCache.put(userToken, mapUser);
        }
        mapUser.put("token", userToken);
       Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) ((Collection) mapUser.get("roles")).stream()
                .map(o -> ((Map<String, String>)o).get("authority"))
                .map(o -> new SimpleGrantedAuthority("ROLE_"+o))
                .collect(Collectors.toSet());

        UserDetails userDetails = User.withUsername((String) mapUser.get("username"))
                .password(userToken)
                .roles()
                .authorities(authorities)
                .build();
        UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken(userDetails, userToken, userDetails.getAuthorities());
        newToken.setDetails(mapUser);
        return newToken;
    }

    private String getUserToken(Authentication authentication) {
        if(authentication.getClass().equals(UsernamePasswordAuthenticationToken.class)) {
            final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
            String username = (String) authentication.getPrincipal();
            String credentials = (String) token.getCredentials();
            return verifyUser(username, credentials);
        }else if(authentication.getClass().equals(JWTAuthentication.class)){
            return ((JWTAuthentication)authentication).getJwtToken();
        }
        return null;
    }

    private LinkedHashMap getUserFromToken(String userToken) {
        try {
            Request request = new Request.Builder()
                    .url(authServer+"/user")
                    .header("Authorization", userToken)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return objectMapper.readValue(response.body().string(), LinkedHashMap.class);
            }
            throw new AuthenticationServiceException("Token verification failed");
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to contact auth server", e);
        }
    }

    private String verifyUser(String username, String credentials) {
        RequestBody body = new MultipartBuilder()
                .addFormDataPart("username", username)
                .addFormDataPart("password", credentials)
                .build();

        Request request = new Request.Builder()
                .url(authServer+"/token")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
            throw new BadCredentialsException("Credentials are incorrect");
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to contact auth server", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

}
