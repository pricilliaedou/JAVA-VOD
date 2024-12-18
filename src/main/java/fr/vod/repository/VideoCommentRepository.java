package fr.vod.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.vod.model.VideoComment;

@Repository
public interface VideoCommentRepository extends CrudRepository<VideoComment, Integer> {

}
