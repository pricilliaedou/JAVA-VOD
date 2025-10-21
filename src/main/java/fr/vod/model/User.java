package fr.vod.model;

import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name="user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="Id") 
	private int id; 
	
	@Column(name="email")
	@NotNull(message = "L'email est obligatoire")
	private String email; 
	
	@Column(name="password")
	@Size(min = 8, max = 20)
	private String password;
	
	@Column(name="Firstname")
	@NotNull(message = "Le nom est obligatoire")
	private String firstName;
	
	@Column(name="Lastname")
	private String lastName;
	
	@Column (name="Gender")
	private String gender;
	
	@Column(name="Phone")
	private String phone;
	
//    @Column(name = "profileCode")
//    private String profileCode;
//    
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "user_video_like", 
		joinColumns = {
			@JoinColumn(name = "UserFK_USER") }, 
			inverseJoinColumns = {
				@JoinColumn(name = "VideoFK_VIDEO") })
	private Set<Video> videoLikes = new HashSet<>();
	
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<VideoComment> videoComments = new HashSet<>();
}
