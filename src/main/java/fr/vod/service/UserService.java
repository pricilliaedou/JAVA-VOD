package fr.vod.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fr.vod.model.User;
import fr.vod.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public User get(String userName, String password) {
		User user = userRepository.findByEmail(userName);
		if (user == null)
			return null;
		if (passwordEncoder.matches(password, user.getPassword())) {
			return user;
		}
		return null;
	}

	public User createUser(String email, String password, String lastName, String firstName, Character gender,
			String phone) {
		User user = new User();
		user.setEmail(email);
		// encode password before saving
		user.setPassword(passwordEncoder.encode(password));
		user.setLastName(lastName);
		user.setFirstName(firstName);
		user.setPhone(phone);
		userRepository.save(user);
		return user;

	}

	public boolean exists(String email) {

		return (userRepository.findByEmail(email) != null);
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

}