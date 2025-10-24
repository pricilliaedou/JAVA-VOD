
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
	
	@Column(name = "Filename", length = 512, unique = true)
	private String fileName;
	
	@Column(name = "age_range")
	private String ageRange;
	
	@Column(name = "is_testimony")
	private boolean isTestimony;
	
	@Column(name = "is_home_featured")
	private boolean isHomeFeatured;
	
	@Column(name = "home_order")
    private Integer homeOrder;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CategoryFK_CATEGORY")
	private Category category;
	
	
	@ManyToMany(mappedBy = "videoLikes", fetch = FetchType.LAZY)
	private Set<User> likedByUsers = new HashSet<>();
	
	@OneToMany(mappedBy = "video", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<VideoComment> comments = new HashSet<>();
	
	
	
}






