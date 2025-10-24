package fr.vod.controller;

import fr.vod.dto.CommentDTO;
import fr.vod.dto.VideoDTO;
import fr.vod.model.*;
import fr.vod.repository.*;
import fr.vod.service.VideoService;
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

  // Commentaires d’une vidéo (public/lecture)
  @GetMapping("/public/videos/{id}/comments")
  public ResponseEntity<List<CommentDTO>> comments(@PathVariable Integer id) {
    Video v = videoRepository.findById(id).orElseThrow();
    List<CommentDTO> out = commentRepository.findByVideoOrderByIdDesc(v).stream().map(c ->
        new CommentDTO(
            c.getComment(),
            // petit DTO inline {firstName,lastName}
            new fr.vod.dto.SimpleUserDTO(
                c.getUser() != null ? c.getUser().getFirstName() : null,
                c.getUser() != null ? c.getUser().getLastName()  : null
            )
        )
    ).collect(Collectors.toList());
    return ResponseEntity.ok(out);
  }

  // ==== Auth requise (ton mécanisme à brancher) ====

  private User currentUser(HttpServletRequest req) {
    // TODO: brancher ton mécanisme (cookie -> user) si besoin
    return null;
  }

  @PostMapping("/videos/{id}/like")
  public ResponseEntity<?> like(@PathVariable Integer id, HttpServletRequest req) {
    User u = currentUser(req);
    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    Video v = videoRepository.findById(id).orElseThrow();
    u.getVideoLikes().add(v);
    userRepository.save(u);
    return ResponseEntity.ok().build();
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

  @PostMapping("/videos/{id}/comments")
  public ResponseEntity<?> comment(@PathVariable Integer id, @RequestBody Map<String,String> body, HttpServletRequest req) {
    User u = currentUser(req);
    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    Video v = videoRepository.findById(id).orElseThrow();
    String text = body.getOrDefault("comment", "").trim();
    if (text.isEmpty()) return ResponseEntity.badRequest().body("Commentaire vide");

    VideoComment c = new VideoComment();
    c.setUser(u);
    c.setVideo(v);
    c.setComment(text);
    commentRepository.save(c);

    return ResponseEntity.ok().build();
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



//package fr.vod.controller;
//
//import fr.vod.dto.CommentDTO;
//import fr.vod.dto.VideoDTO;
//import fr.vod.model.Category;
//import fr.vod.model.User;
//import fr.vod.model.Video;
//import fr.vod.model.VideoComment;
//import fr.vod.repository.CategoryRepository;
//import fr.vod.repository.UserRepository;
//import fr.vod.repository.VideoCommentRepository;
//import fr.vod.repository.VideoRepository;
//import fr.vod.service.VideoService;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.*;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//
//@RestController
//public class VideoController {
//
//  @Autowired private VideoService videoService;
//  @Autowired private VideoRepository videoRepository;
//  @Autowired private VideoCommentRepository commentRepository;
//  @Autowired private UserRepository userRepository;
//  @Autowired private CategoryRepository categoryRepository; // ✅ nécessaire
//
//  // ---------- Helpers ----------
//  /** Classement des tranches pour autoriser "≤" (une tranche supérieure voit aussi les inférieures). */
//  private int ageRank(String range) {
//    if (range == null) return 0;
//    return switch (range) {
//      case "2-6"   -> 1;
//      case "7-10"  -> 2;
//      case "11-13" -> 3;
//      case "14-17" -> 4;
//      case "18+"   -> 5;
//      default      -> 0;
//    };
//  }
//
//  // ---------- PUBLIC LIST (paginated) ----------
//  // + filtre ageRange optionnel en "≤"
//  @GetMapping("/public/videos")
//  public ResponseEntity<?> list(
//      @RequestParam(defaultValue = "0") int page,
//      @RequestParam(defaultValue = "12") int size,
//      @RequestParam(required = false) String query,
//      @RequestParam(required = false) String ageRange
//  ) {
//    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
//    Page<Video> p = videoService.list(query, pageable);
//
//    Page<VideoDTO> mapped = p.map(v -> new VideoDTO(
//        v.getId(),
//        v.getTitle(),
//        v.getDescription(),
//        v.getFileName(), // url
//        v.getAgeRange()
//    ));
//
//    if (ageRange == null || ageRange.isBlank()) {
//      return ResponseEntity.ok(mapped);
//    }
//
//    final int selectedRank = ageRank(ageRange);
//    List<VideoDTO> filtered = mapped.getContent().stream()
//        .filter(v -> ageRank(v.getAgeRange()) <= selectedRank)
//        .toList();
//
//    Page<VideoDTO> filteredPage = new PageImpl<>(filtered, pageable, filtered.size());
//    return ResponseEntity.ok(filteredPage);
//  }
//
//  // ---------- PUBLIC GROUPED BY CATEGORY ----------
//  // Retour: [ { category: "Nom", videos: [VideoDTO...] }, ... ]
//  @GetMapping("/public/videos/by-category")
//  public ResponseEntity<List<Map<String, Object>>> byCategory(
//      @RequestParam(required = false) String ageRange
//  ) {
//    final int selectedRank = ageRank(ageRange);
//
//    List<Category> cats = StreamSupport
//        .stream(categoryRepository.findAll().spliterator(), false)
//        .toList();
//
//    List<Map<String, Object>> out = new ArrayList<>();
//
//    for (Category c : cats) {
//      List<VideoDTO> items = c.getVideos().stream()
//          .filter(v -> ageRange == null || ageRange.isBlank() || ageRank(v.getAgeRange()) <= selectedRank)
//          .map(v -> new VideoDTO(
//              v.getId(),
//              v.getTitle(),
//              v.getDescription(),
//              v.getFileName(),
//              v.getAgeRange()
//          ))
//          .toList();
//
//      Map<String, Object> block = new LinkedHashMap<>();
//      block.put("category", c.getName());
//      block.put("videos", items);
//      out.add(block);
//    }
//
//    return ResponseEntity.ok(out);
//  }
//
//  // ---------- PUBLIC "ALL" GROUPED: category -> byAge ----------
//  @GetMapping("/public/videos/all")
//  public ResponseEntity<List<Map<String, Object>>> allVideosGrouped() {
//
//    List<String> ageOrder = List.of("2-6", "7-10", "11-13", "14-17", "18+");
//
//    List<Category> cats = StreamSupport
//        .stream(categoryRepository.findAll().spliterator(), false)
//        .toList();
//
//    List<Map<String, Object>> out = new ArrayList<>();
//
//    for (Category c : cats) {
//      Map<String, List<VideoDTO>> byAge = new LinkedHashMap<>();
//      ageOrder.forEach(a -> byAge.put(a, new ArrayList<>()));
//
//      c.getVideos().forEach(v -> {
//        String a = v.getAgeRange();
//        if (a == null || a.isBlank() || !byAge.containsKey(a)) {
//          a = "18+";
//        }
//        byAge.get(a).add(new VideoDTO(
//            v.getId(),
//            v.getTitle(),
//            v.getDescription(),
//            v.getFileName(),
//            v.getAgeRange()
//        ));
//      });
//
//      byAge.entrySet().removeIf(e -> e.getValue().isEmpty());
//
//      Map<String, Object> block = new LinkedHashMap<>();
//      block.put("category", c.getName());
//      block.put("byAge", byAge);
//      block.put("total", byAge.values().stream().mapToInt(List::size).sum());
//      out.add(block);
//    }
//
//    return ResponseEntity.ok(out);
//  }
//
//  // ---------- PUBLIC DETAIL ----------
//  @GetMapping("/public/videos/{id}")
//  public ResponseEntity<VideoDTO> detail(@PathVariable Integer id) {
//    Video v = videoRepository.findById(id).orElseThrow();
//    return ResponseEntity.ok(new VideoDTO(
//        v.getId(),
//        v.getTitle(),
//        v.getDescription(),
//        v.getFileName(),
//        v.getAgeRange()
//    ));
//  }
//
//  // ---------- PUBLIC COMMENTS (read) ----------
//  @GetMapping("/public/videos/{id}/comments")
//  public ResponseEntity<List<CommentDTO>> comments(@PathVariable Integer id) {
//    Video v = videoRepository.findById(id).orElseThrow();
//    List<CommentDTO> out = commentRepository.findByVideoOrderByIdDesc(v).stream().map(c ->
//        new CommentDTO(
//            c.getComment(),
//            new fr.vod.dto.SimpleUserDTO(
//                c.getUser() != null ? c.getUser().getFirstName() : null,
//                c.getUser() != null ? c.getUser().getLastName()  : null
//            )
//        )
//    ).collect(Collectors.toList());
//    return ResponseEntity.ok(out);
//  }
//
//  // ---------- AUTH REQUIRED (like/comment) ----------
//  private User currentUser(HttpServletRequest req) {
//    // TODO: retrouver l'utilisateur depuis le cookie/token "auth-token-vod"
//    return null;
//  }
//
//  @PostMapping("/videos/{id}/like")
//  public ResponseEntity<?> like(@PathVariable Integer id, HttpServletRequest req) {
//    User u = currentUser(req);
//    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    Video v = videoRepository.findById(id).orElseThrow();
//    u.getVideoLikes().add(v);
//    userRepository.save(u);
//    return ResponseEntity.ok().build();
//  }
//
//  @DeleteMapping("/videos/{id}/like")
//  public ResponseEntity<?> unlike(@PathVariable Integer id, HttpServletRequest req) {
//    User u = currentUser(req);
//    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    Video v = videoRepository.findById(id).orElseThrow();
//    u.getVideoLikes().remove(v);
//    userRepository.save(u);
//    return ResponseEntity.noContent().build();
//  }
//
//  @PostMapping("/videos/{id}/comments")
//  public ResponseEntity<?> comment(@PathVariable Integer id, @RequestBody Map<String,String> body, HttpServletRequest req) {
//    User u = currentUser(req);
//    if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//
//    Video v = videoRepository.findById(id).orElseThrow();
//    String text = body.getOrDefault("comment", "").trim();
//    if (text.isEmpty()) return ResponseEntity.badRequest().body("Commentaire vide");
//
//    VideoComment c = new VideoComment();
//    c.setUser(u); c.setVideo(v); c.setComment(text);
//    commentRepository.save(c);
//
//    return ResponseEntity.ok().build();
//  }
//}
//
//
//
//
//
