package fr.vod.controller;

import fr.vod.dto.CommentDTO;
import fr.vod.dto.VideoDTO;
import fr.vod.model.*;
import fr.vod.repository.*;
import fr.vod.service.VideoService;
import fr.vod.service.VideoCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class VideoController {

  @Autowired private VideoService videoService;
  @Autowired private VideoRepository videoRepository;
  @Autowired private VideoCommentRepository commentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CategoryRepository categoryRepository; // <- ajout pour éviter "cannot be resolved"
  @Autowired private VideoCommentService videoCommentService;
  // ====================== Endpoints existants ======================

  // Liste paginée publique
  @GetMapping("/public/videos")
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String ageRange
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    Page<Video> p = videoService.list(query, pageable);

    Page<VideoDTO> out = p.map(v -> new VideoDTO(
        v.getId(),
        v.getTitle(),
        v.getDescription(),
        v.getFileName(),
        v.getAgeRange()
    ));
    return ResponseEntity.ok(out);
  }

  // Détail d’une vidéo
  @GetMapping("/public/videos/{id}")
  public ResponseEntity<VideoDTO> detail(@PathVariable Integer id) {
    Video v = videoRepository.findById(id).orElseThrow();
    return ResponseEntity.ok(new VideoDTO(
        v.getId(), v.getTitle(), v.getDescription(), v.getFileName(), v.getAgeRange()
    ));
  }
  
  @GetMapping("/public/videos/by-url")
  public ResponseEntity<VideoDTO> byUrl(@RequestParam("u") String fileName) {
      return videoService.findByUrl(fileName)
              .map(ResponseEntity::ok)
              .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
  
  @GetMapping("/public/videos/{id}/comments")
  public ResponseEntity<List<CommentDTO>> publicComments(@PathVariable Integer id) {
    // même payload que /api pour simplifier le front ; côté suppression reste admin-only
    return ResponseEntity.ok(videoCommentService.listByVideo(id));
  }
  
  @GetMapping("/api/videos/{id}/meta")
  public Map<String, Object> meta(@PathVariable Integer id) {
    return videoService.meta(id);
  }
  
  private User currentUser(HttpServletRequest req) {
    // TODO: brancher ton mécanisme (cookie -> user) si besoin
    return null;
  }

  
  @PutMapping("/api/videos/{id}/like")
  public Map<String, Object> like(@PathVariable Integer id) {
    return videoService.like(id);
  }
  @DeleteMapping("/api/videos/{id}/like")
  public Map<String, Object> unlike(@PathVariable Integer id) {
    return videoService.unlike(id);
  }

  @DeleteMapping("/videos/{id}/like")
  public ResponseEntity<?> unlike(@PathVariable Integer id, HttpServletRequest req) {
    User u = currentUser(req);
    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    Video v = videoRepository.findById(id).orElseThrow();
    u.getVideoLikes().remove(v);
    userRepository.save(u);
    return ResponseEntity.noContent().build();
  }
  
  @PutMapping("/api/videos/{id}/favorite")
  public Map<String, Object> favorite(@PathVariable Integer id) {
    return videoService.favorite(id);
  }
  @DeleteMapping("/api/videos/{id}/favorite")
  public Map<String, Object> unfavorite(@PathVariable Integer id) {
    return videoService.unfavorite(id);
  }

  // Mes favoris
  @GetMapping("/api/me/favorites")
  public List<VideoDTO> myFavorites() {
    return videoService.myFavorites();
  }
  
  @GetMapping("/api/videos/{id}/comments")
  public List<CommentDTO> comments(@PathVariable Integer id) {
    return videoCommentService.listByVideo(id);
  }

  @PostMapping("/api/videos/{id}/comments")
  public CommentDTO addComment(@PathVariable Integer id, @RequestBody Map<String, String> body) {
    String comment = Optional.ofNullable(body.get("comment")).orElse("").trim();
    return videoCommentService.add(id, comment);
  }
  

  

  // ====================== Nouveaux endpoints "par catégorie" ======================

  // Règle d’inclusion des tranches d’âges (les supérieures voient les inférieures)
  private List<String> allowedAges(String age) {
    if (age == null || age.isBlank())
      return List.of("2-6","7-10","11-13","14-17","18+");
    return switch (age) {
      case "2-6"   -> List.of("2-6");
      case "7-10"  -> List.of("2-6","7-10");
      case "11-13" -> List.of("2-6","7-10","11-13");
      case "14-17" -> List.of("2-6","7-10","11-13","14-17");
      default      -> List.of("2-6","7-10","11-13","14-17","18+"); // 18+ voit tout
    };
  }

  /**
   * Contenus "classiques" regroupés par catégorie (pour page /videos)
   * GET /public/videos/by-category?ageRange=11-13
   */
  @GetMapping("/public/videos/by-category")
  public List<Map<String, Object>> videosByCategory(
      @RequestParam(required = false) String ageRange
  ) {
    return groupedByCategory(false, ageRange);
  }

  /**
   * Témoignages regroupés par catégorie (pour page /temoignages)
   * GET /public/testimonials/by-category?ageRange=14-17
   */
  @GetMapping("/public/testimonials/by-category")
  public List<Map<String, Object>> testimoniesByCategory(
      @RequestParam(required = false) String ageRange
  ) {
    return groupedByCategory(true, ageRange);
  }

  // ----------- Implémentation commune ------------

  private List<Map<String, Object>> groupedByCategory(boolean isTestimony, String ageRange) {
    List<String> ages = allowedAges(ageRange);

    // 1) charge les vidéos selon témoignage + âge
    List<Video> vids = (ageRange == null || ageRange.isBlank())
        ? videoRepository.findByIsTestimony(isTestimony)
        : videoRepository.findByIsTestimonyAndAgeRangeIn(isTestimony, ages);

    if (vids.isEmpty()) return List.of();

    // 2) groupe par nom de catégorie
    Map<String, List<VideoDTO>> grouped = vids.stream().collect(
        Collectors.groupingBy(
            v -> v.getCategory() != null ? v.getCategory().getName() : "Autres",
            Collectors.mapping(
                v -> new VideoDTO(
                    v.getId(),
                    v.getTitle(),
                    v.getDescription(),
                    v.getFileName(),
                    v.getAgeRange()
                ),
                Collectors.toList()
            )
        )
    );

    // 3) ordonne par nom de catégorie et transforme en blocs {category, videos}
    return grouped.entrySet().stream()
        // retire les catégories vides au cas où
        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
        .map(e -> {
          Map<String, Object> block = new LinkedHashMap<>();
          block.put("category", e.getKey());
          block.put("videos", e.getValue());
          return block;
        })
        .collect(Collectors.toList());
  }
}



