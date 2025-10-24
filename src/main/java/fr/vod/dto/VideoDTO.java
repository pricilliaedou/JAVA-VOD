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
	private String categoryName;
	
	public VideoDTO(Integer id, String title, String description, String url, String ageRange) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.url = url;
		this.ageRange = ageRange;
	}
}

