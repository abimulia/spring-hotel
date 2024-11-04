/**
 * 
 */
package com.abimulia.hotel.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.abimulia.hotel.util.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfiguration {
	
	@Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		log.debug("configure()");
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "api/tenant-listing/get-all-by-category").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/tenant-listing/get-one").permitAll()
                        .requestMatchers(HttpMethod.POST, "api/tenant-listing/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/booking/check-availability").permitAll()
                        .requestMatchers(HttpMethod.GET, "assets/*").permitAll()
                        .anyRequest()
                        .authenticated())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler))
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults());
        log.debug("return http: " + http );

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
    	log.debug("userAuthoritiesMapper()");
        return authorities -> {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

            authorities.forEach(grantedAuthority -> {
                if (grantedAuthority instanceof OidcUserAuthority oidcUserAuthority) {
                	log.debug("grantedAuthority: " + grantedAuthority + " instanceof " + oidcUserAuthority);
                    grantedAuthorities
                            .addAll(SecurityUtils.extractAuthorityFromClaims(oidcUserAuthority.getUserInfo().getClaims()));
                }
            });
            log.debug("return grantedAuthorities: " + grantedAuthorities);
            return grantedAuthorities;
        };
    }

}
