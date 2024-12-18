package fr.vod.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="user")
public class VideoComment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserFK_CATEGORY")
	User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VideoFK_VIDEO")
	Video video;
	
	@Column(name="comment")
	String comment;
	
}
