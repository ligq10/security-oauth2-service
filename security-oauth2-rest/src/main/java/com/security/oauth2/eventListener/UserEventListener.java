package com.security.oauth2.eventListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import com.security.oauth2.entity.User;
import com.security.oauth2.repository.UserRepository;

@RepositoryEventHandler(User.class)
@Component
public class UserEventListener {
	
	@Autowired
	private UserRepository userRepository;
	
	@HandleBeforeDelete
	public void onBeforeDelete(User user) {
		//user.getRoles().clear();
		
	}
}
