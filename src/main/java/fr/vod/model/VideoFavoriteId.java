package fr.vod.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class VideoFavoriteId implements Serializable {
    @Column(name = "VideoFK_VIDEO")
    private Integer videoId;
    @Column(name = "UserFK_USER")
    private Integer userId;
}
