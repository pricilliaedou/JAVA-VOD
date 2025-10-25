package fr.vod.repository;

import fr.vod.model.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoLikeRepository extends CrudRepository<VideoLike, VideoLikeId> {
    boolean existsByVideoAndUser(Video v, User u);
    long countByVideo(Video v);
    void deleteByVideoAndUser(Video v, User u);
    List<VideoLike> findAllByUser(User u);
}
