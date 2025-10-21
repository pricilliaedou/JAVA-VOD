package fr.vod.dto;
import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class VideoDTO{
	private Integer id; 
	private String title;
	private String description;
	private String url;
	private String ageRange;
}

