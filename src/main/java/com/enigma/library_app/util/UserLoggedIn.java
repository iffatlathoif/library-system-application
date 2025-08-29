package com.enigma.library_app.util;

import com.enigma.library_app.model.User;
import com.enigma.library_app.service.UserService;
import com.enigma.library_app.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLoggedIn {

	private final UserService userService;

	public User getUserLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String usernameLoggedIn = UserUtil.getUsernameLoggedIn(authentication);
		User user = userService.getEntityByUsername(usernameLoggedIn);
		if (null == user.getLocation() || null == user.getLocation().getName()) {
			throw new ApiException("The staff user does not yet have a library location assigned.");
		}
		return user;
	}
}
