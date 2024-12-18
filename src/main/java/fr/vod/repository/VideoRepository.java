package fr.vod.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.vod.model.Video;

@Repository
public interface VideoRepository extends CrudRepository<Video, Integer> {

}
