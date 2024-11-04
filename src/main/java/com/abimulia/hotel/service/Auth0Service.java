/**
 * 
 */
package com.abimulia.hotel.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abimulia.hotel.dto.ReadUserDTO;
import com.abimulia.hotel.exception.UserException;
import com.abimulia.hotel.util.SecurityUtils;
import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.FieldsFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Service
@Slf4j
public class Auth0Service {
	@Value("${okta.oauth2.client-id}")
    private String clientId;

    @Value("${okta.oauth2.client-secret}")
    private String clientSecret;

    @Value("${okta.oauth2.issuer}")
    private String domain;

    @Value("${application.auth0.role-landlord-id}")
    private String roleLandlordId;
    
    private String getAccessToken() throws Auth0Exception {
    	log.debug("getAccessToken()");
        AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
        log.debug("authAPI: "+ authAPI) ;
        TokenRequest tokenRequest = authAPI.requestToken(domain + "api/v2/");
        log.debug("tokenRequest " + tokenRequest);
        TokenHolder holder = tokenRequest.execute().getBody();
        log.debug("holder: " + holder);
        return holder.getAccessToken();
    }
    
    private void assignRoleById(String accessToken, String email, UUID publicId, String roleIdToAdd) throws Auth0Exception {
    	log.debug("assignRoleById()");
        ManagementAPI mgmt = ManagementAPI.newBuilder(domain, accessToken).build();
        log.debug("mgmt: " + mgmt);
        Response<List<User>> auth0userByEmail = mgmt.users().listByEmail(email, new FieldsFilter()).execute();
        log.debug("auth0userByEmail: " + auth0userByEmail);
        User user = auth0userByEmail.getBody()
                .stream().findFirst()
                .orElseThrow(() -> new UserException(String.format("Cannot find user with public id %s", publicId)));
        log.debug("user: " + user);
        mgmt.roles().assignUsers(roleIdToAdd, List.of(user.getId())).execute();
    }
    
    public void addLandlordRoleToUser(ReadUserDTO readUserDTO) {
    	log.debug("addLandlordRoleToUser: " + readUserDTO);
        if (readUserDTO.authorities().stream().noneMatch(role -> role.equals(SecurityUtils.ROLE_LANDLORD))) {
        	log.debug("set role landlord");
            try {
                String accessToken = this.getAccessToken();
                assignRoleById(accessToken, readUserDTO.email(), readUserDTO.publicId(), roleLandlordId);
            } catch (Auth0Exception a) {
                throw new UserException(String.format("not possible to assign %s to %s", roleLandlordId, readUserDTO.publicId()));
            }
        }
    }

}
