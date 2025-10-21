package fr.vod.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.vod.model.VideoComment;
import fr.vod.model.User;
import fr.vod.model.Video;

import java.util.List;


@Repository
public interface VideoCommentRepository extends CrudRepository<VideoComment, Integer> {
	List<VideoComment> findByVideoOrderByIdDesc(Video video);
	List<VideoComment> findByUserOrderByIdDesc(User user);

}
