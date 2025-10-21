package fr.vod.dto;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CommentDTO {
    private String comment;
    private SimpleUserDTO user;
}

