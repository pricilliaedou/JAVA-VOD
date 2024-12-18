package fr.vod.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.vod.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
	public User findByEmailAndPassword(String email, String password);

	public User findByEmail(String email);

}
