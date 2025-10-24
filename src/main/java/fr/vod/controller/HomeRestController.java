package fr.vod.controller;

import fr.vod.dto.*;
import fr.vod.model.Category;
import fr.vod.model.Video;
import fr.vod.service.CategoryService;
import fr.vod.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class HomeRestController {

  @Autowired CategoryService categoryService;
  @Autowired VideoService videoService;

  @GetMapping("/public/home")
  public HomeDTO getHome() {
    HomeDTO homeDTO = new HomeDTO();

    // catégories
    List<CategoryDTO> listCategoryDTO = new ArrayList<>();
    for (Category c : categoryService.list()) {
      listCategoryDTO.add(new CategoryDTO(c));
    }
    homeDTO.setCategoryListDTO(listCategoryDTO);


    var featured = videoService.top3Featured();
    List<VideoDTO> vids = featured.stream()
        .map(v -> new VideoDTO(
            v.getId(),
            v.getTitle(),
            v.getDescription(),
            v.getFileName(),   // <- url
            v.getAgeRange()
        ))
        .toList();

    homeDTO.setVideoListDTO(vids);
    return homeDTO;
  }
}
