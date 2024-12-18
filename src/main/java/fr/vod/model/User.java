package fr.vod.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	
	@Column(name="email")
	@NotNull(message = "L'email est obligatoire")
	String email;
	
	@Column(name="password")
	@Size(min = 8, max = 20)
	String password;
	
	@Column(name="firstname")
	@NotNull(message = "Le nom est obligatoire")
	String firstName;
	
	@Column(name="lastname")
	String lastName;
	
	@Column(name="phone")
	String phone;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "USER_VIDEO_LIKE", 
		joinColumns = {
			@JoinColumn(name = "UserFK_USER", nullable = false, updatable = false, insertable = false) }, 
			inverseJoinColumns = {
				@JoinColumn(name = "VideoFK_VIDEO", nullable = false, updatable = false, insertable = false) })
	Set<Video> videoLikes;
}
