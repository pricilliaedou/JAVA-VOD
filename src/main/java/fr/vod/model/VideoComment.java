package fr.vod.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity @Table(name="user_video_comment")
public class VideoComment{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="Id")
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UserFK_USER")
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	  @JoinColumn(name = "VideoFK_VIDEO")
	  private Video video;
	
	@Column(name="Comment", columnDefinition="TEXT")
	private String comment;
}


