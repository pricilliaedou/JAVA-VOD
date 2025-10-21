package fr.vod.controller;

import fr.vod.dto.*;
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
      null
    ));

    return ResponseEntity.ok(out);
  }


  @GetMapping("/public/videos/{id}")
  public ResponseEntity<VideoDTO> detail(@PathVariable Integer id) {
    Video v = videoRepository.findById(id).orElseThrow();
    return ResponseEntity.ok(new VideoDTO(
      v.getId(), v.getTitle(), v.getDescription(), v.getFileName(), null
    ));
    }


  @GetMapping("/public/videos/{id}/comments")
  public ResponseEntity<List<CommentDTO>> comments(@PathVariable Integer id) {
    Video v = videoRepository.findById(id).orElseThrow();
    List<CommentDTO> out = commentRepository.findByVideoOrderByIdDesc(v).stream().map(c ->
      new CommentDTO(
        c.getComment(),
        new SimpleUserDTO(
          c.getUser() != null ? c.getUser().getFirstName() : null,
          c.getUser() != null ? c.getUser().getLastName()  : null
        )
      )
    ).collect(Collectors.toList());
    return ResponseEntity.ok(out);
  }


  private User currentUser(HttpServletRequest req) {
    // TODO: brancher avec ton mécanisme (cookie -> user)
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
    c.setUser(u); c.setVideo(v); c.setComment(text);
    commentRepository.save(c);

    return ResponseEntity.ok().build();
  }
}
