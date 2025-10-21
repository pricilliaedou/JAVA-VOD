
package fr.vod.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@Entity @Table(name="video")
public class Video{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="Id")
	private Integer id;
	
	@Column(name = "Title")
	private String title;

	@Column(name = "Description", columnDefinition="TEXT")
	private String description;
	
	@Column(name="Filename", columnDefinition="TEXT")
	private String fileName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CategoryFK_CATEGORY")
	private Category category;
	
	
	@ManyToMany(mappedBy = "videoLikes", fetch = FetchType.LAZY)
	private Set<User> likedByUsers = new HashSet<>();
	
	@OneToMany(mappedBy = "video", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<VideoComment> comments = new HashSet<>();
	
	
	
}






//package fr.vod.model;
//
//import java.util.Set;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.JoinTable;
//import jakarta.persistence.ManyToMany;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "video")
//public class Video {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
//	int id;
//	
//	@Column(name = "title")
//	String title;
//	
//	@Column(name = "description")
//	String description;
//	
//	@Column(name = "filename")
//	String fileName;
//	
//	@ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "CategoryFK_CATEGORY")
//	Category category;
//	
//	@ManyToMany(fetch = FetchType.LAZY)
//	@JoinTable(
//		name = "USER_VIDEO_LIKE", 
//		joinColumns = {
//			@JoinColumn(name = "VideoFK_VIDEO", nullable = false, updatable = false, insertable = false) }, 
//			inverseJoinColumns = {
//				@JoinColumn(name = "UserFK_USER", nullable = false, updatable = false, insertable = false) })
//	Set<User> userLikes;
//}



