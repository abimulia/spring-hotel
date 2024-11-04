/**
 * 
 */
package com.abimulia.hotel.mapper;

import org.mapstruct.Mapper;

import com.abimulia.hotel.dto.ReadUserDTO;
import com.abimulia.hotel.model.Authority;
import com.abimulia.hotel.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
	
	ReadUserDTO readUserDTOToUser(User user);
	
	default String mapAuthoritiesToString(Authority authority) {
		
		return authority.getName();
	}

}
