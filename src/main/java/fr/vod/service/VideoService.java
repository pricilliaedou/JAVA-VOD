package fr.vod.service;

import fr.vod.model.Category;
import fr.vod.model.Video;
import fr.vod.repository.CategoryRepository;
import fr.vod.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

  @Autowired
  private VideoRepository videoRepository;
  
  @Autowired
  private CategoryRepository categoryRepository;

  public List<Video> top3() {
    return videoRepository.findTop3();
  }

  public Page<Video> list(String query, Pageable pageable) {
    if (query != null && !query.isBlank()) {
      return videoRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);
    }
    return videoRepository.findAll(pageable);
  }
  
  public Video addIfAbsent(String title, String description, String url, String categoryName, String ageRange) {
      Optional<Video> existing = videoRepository.findByFileName(url);
      if (existing.isPresent()) {
          return existing.get(); 
      }

      Category category = categoryRepository.findByName(categoryName);
      if (category == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie inconnue : " + categoryName);
      }

      Video video = new Video();
      video.setTitle(title);
      video.setDescription(description);
      video.setFileName(url);
      video.setCategory(category);
      video.setAgeRange(ageRange);

      try {
          return videoRepository.save(video);
      } catch (DataIntegrityViolationException e) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "Vidéo déjà existante avec cette URL.");
      }
  }
}
