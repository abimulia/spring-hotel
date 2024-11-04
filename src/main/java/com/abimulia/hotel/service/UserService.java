/**
 * 
 */
package com.abimulia.hotel.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abimulia.hotel.dto.ReadUserDTO;
import com.abimulia.hotel.mapper.UserMapper;
import com.abimulia.hotel.model.User;
import com.abimulia.hotel.repository.UserRepository;
import com.abimulia.hotel.util.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Service
@Slf4j
public class UserService {
	private static final String UPDATED_AT_KEY = "updated_at";
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    
	/**
	 * @param userRepository
	 * @param userMapper
	 */
	public UserService(UserRepository userRepository, UserMapper userMapper) {
		log.debug("UserService() userRepository: " + userRepository +" userMapper: " +userMapper);
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	@Transactional(readOnly = true)
    public ReadUserDTO getAuthenticatedUserFromSecurityContext() {
		log.debug("getAuthenticatedUserFromSecurityContext()");
        OAuth2User principal = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = SecurityUtils.mapOauth2AttributesToUser(principal.getAttributes());
        return getByEmail(user.getEmail()).orElseThrow();
    }
	
	@Transactional(readOnly = true)
    public Optional<ReadUserDTO> getByEmail(String email) {
		log.debug("getByEmail() email: " + email);
        Optional<User> oneByEmail = userRepository.findOneByEmail(email);
        return oneByEmail.map(userMapper::readUserDTOToUser);
    }
	
	public void syncWithIdp(OAuth2User oAuth2User, boolean forceResync) {
		log.debug("syncWithIdp()");
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.debug("attributes: " + attributes);
        User user = SecurityUtils.mapOauth2AttributesToUser(attributes);
        log.debug("user: " + user);
        Optional<User> existingUser = userRepository.findOneByEmail(user.getEmail());
        log.debug("existingUser: " + existingUser);
        if (existingUser.isPresent()) {
        	log.debug("isPresent()");
            if (attributes.get(UPDATED_AT_KEY) != null) {
            	log.debug("UPDATED_AT_KEY: " + UPDATED_AT_KEY);
                Instant lastModifiedDate = existingUser.orElseThrow().getLastModifiedDate();
                Instant idpModifiedDate;
                if (attributes.get(UPDATED_AT_KEY) instanceof Instant instant) {
                    idpModifiedDate = instant;
                } else {
                    idpModifiedDate = Instant.ofEpochSecond((Integer) attributes.get(UPDATED_AT_KEY));
                }
                if (idpModifiedDate.isAfter(lastModifiedDate) || forceResync) {
                    updateUser(user);
                }
            }
        } else {
        	log.debug("save and flush");
            userRepository.saveAndFlush(user);
        }
    }

    private void updateUser(User user) {
    	log.debug("updateUser() " + user);
        Optional<User> userToUpdateOpt = userRepository.findOneByEmail(user.getEmail());
        if (userToUpdateOpt.isPresent()) {
            User userToUpdate = userToUpdateOpt.get();
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setFirstName(user.getFirstName());
            userToUpdate.setLastName(user.getLastName());
            userToUpdate.setAuthorities(user.getAuthorities());
            userToUpdate.setImageUrl(user.getImageUrl());
            userRepository.saveAndFlush(userToUpdate);
        }
    }

    public Optional<ReadUserDTO> getByPublicId(UUID publicId) {
    	log.debug("getByPublicId() " + publicId);
        Optional<User> oneByPublicId = userRepository.findOneByPublicId(publicId);
        return oneByPublicId.map(userMapper::readUserDTOToUser);
    }

}
