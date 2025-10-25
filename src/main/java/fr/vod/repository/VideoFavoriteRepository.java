package fr.vod.repository;

import fr.vod.model.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoFavoriteRepository extends CrudRepository<VideoFavorite, VideoFavoriteId> {
    boolean existsByVideoAndUser(Video v, User u);
    
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByVideoAndUser(Video v, User u);
    List<VideoFavorite> findAllByUser(User u);
}
