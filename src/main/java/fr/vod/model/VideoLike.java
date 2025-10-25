package fr.vod.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_video_like")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VideoLike {
    @EmbeddedId
    private VideoLikeId id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("videoId")
    @JoinColumn(name = "VideoFK_VIDEO")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "UserFK_USER")
    private User user;
}
