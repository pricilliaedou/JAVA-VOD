package fr.vod.service;

import fr.vod.dto.CommentDTO;
import fr.vod.dto.SimpleUserDTO;
import fr.vod.model.User;
import fr.vod.model.Video;
import fr.vod.model.VideoComment;
import fr.vod.repository.UserRepository;
import fr.vod.repository.VideoCommentRepository;
import fr.vod.repository.VideoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoCommentService {

	@Autowired
    private VideoCommentRepository videoCommentRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;
    
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
    
    public List<CommentDTO> listByVideo(Integer videoId) {
        Video v = videoRepository.findById(videoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
        );
        return videoCommentRepository.findByVideoOrderByIdDesc(v).stream()
                .map(c -> new CommentDTO(
                        c.getComment(),
                        new SimpleUserDTO(
                                c.getUser() != null ? c.getUser().getFirstName() : null,
                                c.getUser() != null ? c.getUser().getLastName()  : null
                        )
                ))
                .toList();
    }

    public CommentDTO add(Integer videoId, String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commentaire vide");
        }
        User me = getCurrentUserOrThrow();
        Video v = videoRepository.findById(videoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Vidéo introuvable")
        );
        VideoComment entity = new VideoComment();
        entity.setUser(me);
        entity.setVideo(v);
        entity.setComment(comment.trim());

        VideoComment saved = videoCommentRepository.save(entity);
        return new CommentDTO(
                saved.getComment(),
                new SimpleUserDTO(
                        saved.getUser() != null ? saved.getUser().getFirstName() : null,
                        saved.getUser() != null ? saved.getUser().getLastName()  : null
                )
        );
    }

    /* suppression admin : à placer dans un controller Admin avec contrôle de rôle */
    public void adminDelete(Integer commentId) {
        videoCommentRepository.deleteById(commentId);
    }
}
