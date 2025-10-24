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


    List<VideoDTO> vids = new ArrayList<>();
    for (Video v : videoService.top3()) {
      vids.add(new VideoDTO(
        v.getId(),
        v.getTitle(),
        v.getDescription(),
        v.getFileName(), 
        v.getAgeRange()            
      ));
    }
    homeDTO.setVideoListDTO(vids);
    return homeDTO;
  }
}
