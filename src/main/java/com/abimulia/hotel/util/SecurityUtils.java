/**
 * 
 */
package com.abimulia.hotel.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.abimulia.hotel.model.Authority;
import com.abimulia.hotel.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
public class SecurityUtils {
	
	public static final String ROLE_TENANT = "ROLE_TENANT";
    public static final String ROLE_LANDLORD = "ROLE_LANDLORD";

    public static final String CLAIMS_NAMESPACE = "https://www.abimulia.com/roles";
    
    public static User mapOauth2AttributesToUser(Map<String, Object> attributes) {
    	log.debug("mapOauth2AttributesToUser()");
        User user = new User();
        String sub = String.valueOf(attributes.get("sub"));
        log.debug("sub: " + sub);

        String username = null;

        if (attributes.get("preferred_username") != null) {
            username = ((String) attributes.get("preferred_username")).toLowerCase();
            log.debug("username: " + username);
        }

        if (attributes.get("given_name") != null) {
            user.setFirstName(((String) attributes.get("given_name")));
            log.debug("given_name");
        } else if ((attributes.get("nickname") != null)) {
            user.setFirstName(((String) attributes.get("nickname")));
            log.debug("nickname");
        }

        if (attributes.get("family_name") != null) {
            user.setLastName(((String) attributes.get("family_name")));
        }

        if (attributes.get("email") != null) {
            user.setEmail(((String) attributes.get("email")));
        } else if (sub.contains("|") && (username != null && username.contains("@"))) {
            user.setEmail(username);
        } else {
            user.setEmail(sub);
        }

        if (attributes.get("picture") != null) {
            user.setImageUrl(((String) attributes.get("picture")));
        }

        if(attributes.get(CLAIMS_NAMESPACE) != null) {
            List<String> authoritiesRaw = (List<String>) attributes.get(CLAIMS_NAMESPACE);
            Set<Authority> authorities = authoritiesRaw.stream()
                    .map(authority -> {
                        Authority auth = new Authority();
                        auth.setName(authority);
                        return auth;
                    }).collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        return user;
    }

    public static List<SimpleGrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
    	log.debug("extractAuthorityFromClaims() claims: " + claims);
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
    	log.debug("getRolesFromClaims() claims: " + claims);
    	log.debug("get CLAIMS_NAMESPACE: " +CLAIMS_NAMESPACE + " value: "+claims.get(CLAIMS_NAMESPACE));
        return (List<String>) claims.get(CLAIMS_NAMESPACE);
    }

    private static List<SimpleGrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
    	log.debug("mapRolesToGrantedAuthorities() roles: " + roles);
        return roles.stream().filter(role -> role.startsWith("ROLE_")).map(SimpleGrantedAuthority::new).toList();
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String ...authorities) {
    	log.debug("hasCurrentUserAnyOfAuthorities() authorities: " + authorities);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && getAuthorities(authentication)
                .anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
    	log.debug("getAuthorities() authentication: " + authentication);
        Collection<? extends GrantedAuthority> authorities = authentication
                instanceof JwtAuthenticationToken jwtAuthenticationToken ?
                extractAuthorityFromClaims(jwtAuthenticationToken.getToken().getClaims()) : authentication.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority);
    }

}
