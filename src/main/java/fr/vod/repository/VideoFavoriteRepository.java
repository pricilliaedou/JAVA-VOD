package fr.vod.repository;

import fr.vod.model.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoFavoriteRepository extends CrudRepository<VideoFavorite, VideoFavoriteId> {
    boolean existsByVideoAndUser(Video v, User u);
    void deleteByVideoAndUser(Video v, User u);
    List<VideoFavorite> findAllByUser(User u);
}
