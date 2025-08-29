package com.enigma.library_app.util;

import com.enigma.library_app.model.UserInfoDetails;
import org.springframework.security.core.Authentication;


public class UserUtil {

	public static String getUsernameLoggedIn(Authentication authentication) {
		return ((UserInfoDetails) authentication.getPrincipal()).getUsername();
	}
}
