package fr.vod.service;

import fr.vod.model.Video;
import fr.vod.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VideoService {

  @Autowired
  private VideoRepository videoRepository;

  public List<Video> top3() {
    return videoRepository.findTop3();
  }

  public Page<Video> list(String query, Pageable pageable) {
    if (query != null && !query.isBlank()) {
      return videoRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);
    }
    return videoRepository.findAll(pageable);
  }
}
