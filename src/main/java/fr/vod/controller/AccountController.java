package fr.vod.controller;

import fr.vod.dto.CommentDTO;

import fr.vod.dto.SimpleUserDTO;
import fr.vod.dto.VideoDTO;
import fr.vod.model.User;
import fr.vod.model.Video;
import fr.vod.model.VideoComment;
import fr.vod.repository.UserRepository;
import fr.vod.repository.VideoCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
public class AccountController {

    @Autowired private UserRepository userRepository;
    @Autowired private VideoCommentRepository commentRepository;

    private User currentUser(HttpServletRequest req) {
        // TODO: retrouver l'utilisateur depuis le cookie "auth-token-vod"
        return null;
    }

   
    @GetMapping("/me/likes")
    public ResponseEntity<List<VideoDTO>> myLikes(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).build();

        List<VideoDTO> out = u.getVideoLikes().stream().map(v ->
                new VideoDTO(v.getId(), v.getTitle(), v.getDescription(), v.getFileName(), null)
        ).toList();

        return ResponseEntity.ok(out);
    }


    @GetMapping("/me/comments")
    public ResponseEntity<List<CommentDTO>> myComments(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) return ResponseEntity.status(401).build();

        List<VideoComment> list = commentRepository.findByUserOrderByIdDesc(u);
        List<CommentDTO> out = list.stream().map(c ->
                new CommentDTO(
                        c.getComment(),
                        new SimpleUserDTO(
                                c.getUser() != null ? c.getUser().getFirstName() : null,
                                c.getUser() != null ? c.getUser().getLastName()  : null
                        )
                )
        ).toList();

        return ResponseEntity.ok(out);
    }
}