package fr.vod.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.vod.model.User;
import fr.vod.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	public User get(String userName, String password) {
		return userRepository.findByEmailAndPassword(userName, password);
	}

	public User createUser(String email, String password, String lastName, String firstName, Character gender,
			String phone) {
		User user = new User();
		user.setEmail(email);
		user.setPassword(password);
		user.setLastName(lastName);
		user.setFirstName(firstName);
		user.setPhone(phone);
		userRepository.save(user);
		return user;

	}

	public boolean exists(String email) {

		return (userRepository.findByEmail(email) != null);
	}

}
