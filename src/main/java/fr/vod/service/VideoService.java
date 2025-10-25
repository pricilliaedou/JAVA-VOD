package fr.vod.service;

import fr.vod.dto.VideoDTO;
import fr.vod.model.Category;
import fr.vod.model.User;
import fr.vod.model.Video;
import fr.vod.model.VideoFavorite;
import fr.vod.model.VideoFavoriteId;
import fr.vod.model.VideoLike;
import fr.vod.model.VideoLikeId;
import fr.vod.repository.CategoryRepository;
import fr.vod.repository.UserRepository;
import fr.vod.repository.VideoFavoriteRepository;
import fr.vod.repository.VideoLikeRepository;
import fr.vod.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

  @Autowired
  private VideoRepository videoRepository;
  
  @Autowired
  private CategoryRepository categoryRepository;
  
  @Autowired
  private VideoLikeRepository likeRepository;

  @Autowired
  private VideoFavoriteRepository favoriteRepository;

  @Autowired
  private UserRepository userRepository;
  

  public List<Video> top3() {
    return videoRepository.findTop3();
  }
  
  private User getCurrentUserOrThrow() {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
      }
      String email = auth.getName();
      User u = userRepository.findByEmail(email);
      if (u == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable: " + email);
      }
      return u;
  }

  public Map<String, Object> meta(Integer videoId) {
      User me = getCurrentUserOrThrow();
      Video v = videoRepository.findById(videoId).orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
      );
      long likes = likeRepository.countByVideo(v);
      boolean liked = likeRepository.existsByVideoAndUser(v, me);
      boolean favorite = favoriteRepository.existsByVideoAndUser(v, me);

      Map<String, Object> map = new HashMap<>();
      map.put("videoId", v.getId());
      map.put("likes", likes);
      map.put("liked", liked);
      map.put("favorite", favorite);
      return map;
  }

  /* =========================================================
     LIKE / UNLIKE
     ========================================================= */
  public Map<String, Object> like(Integer videoId) {
      User me = getCurrentUserOrThrow();
      Video v = videoRepository.findById(videoId).orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
      );
      if (!likeRepository.existsByVideoAndUser(v, me)) {
          likeRepository.save(new VideoLike(new VideoLikeId(videoId, me.getId()), v, me));
      }
      return meta(videoId);
  }

  public Map<String, Object> unlike(Integer videoId) {
      User me = getCurrentUserOrThrow();
      Video v = videoRepository.findById(videoId).orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
      );
      likeRepository.deleteByVideoAndUser(v, me);
      return meta(videoId);
  }

  /* =========================================================
     FAVORI / UNFAVORI
     ========================================================= */
  public Map<String, Object> favorite(Integer videoId) {
      User me = getCurrentUserOrThrow();
      Video v = videoRepository.findById(videoId).orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
      );
      if (!favoriteRepository.existsByVideoAndUser(v, me)) {
          favoriteRepository.save(new VideoFavorite(new VideoFavoriteId(videoId, me.getId()), v, me));
      }
      return meta(videoId);
  }

  public Map<String, Object> unfavorite(Integer videoId) {
      User me = getCurrentUserOrThrow();
      Video v = videoRepository.findById(videoId).orElseThrow(() ->
              new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
      );
      favoriteRepository.deleteByVideoAndUser(v, me);
      return meta(videoId);
  }

  public List<VideoDTO> myFavorites() {
      User me = getCurrentUserOrThrow();
      return favoriteRepository.findAllByUser(me).stream()
              .map(VideoFavorite::getVideo)
              .map(v -> new VideoDTO(
                      v.getId(),
                      v.getTitle(),
                      v.getDescription(),
                      v.getFileName(),      // ✅ ton DTO attend url=fileName
                      v.getAgeRange()
              ))
              .collect(Collectors.toList());
  }

  /* =========================================================
     Trouver une vidéo par son fileName (utilisé côté front)
     ========================================================= */
  public Optional<VideoDTO> findByUrl(String fileName) {
      return videoRepository.findByFileName(fileName)
              .map(v -> new VideoDTO(
                      v.getId(),
                      v.getTitle(),
                      v.getDescription(),
                      v.getFileName(),      // ✅ correspond à url dans ton DTO
                      v.getAgeRange()
              ));
  }
  
  public Page<Video> list(String query, Pageable pageable) {
    if (query != null && !query.isBlank()) {
      return videoRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);
    }
    return videoRepository.findAll(pageable);
  }
  
  public List<Video> top3Featured() {
	    return videoRepository.findTop3ByIsHomeFeaturedTrueOrderByHomeOrderAscIdAsc();
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
  
  
  public Video createVideo(String title, String description, String url, String ageRange, String categoryName, CategoryRepository categoryRepository) {
      var category = categoryRepository.findByName(categoryName);
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
