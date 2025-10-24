package fr.vod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.vod.dto.CategoryDTO;
import fr.vod.dto.RestAPIResponse;
import fr.vod.dto.VideoDTO;
import fr.vod.model.Video;
import fr.vod.service.CategoryService;
import fr.vod.service.VideoService;

@RestController
public class AdminRestController {

	@Autowired
	CategoryService categoryService;
	
    @Autowired
    VideoService videoService;
	
	@DeleteMapping("/api/category/delete/{idCategory}")
	public Object deleteCategory(@PathVariable String idCategory) throws Exception{
		boolean ok = categoryService.deleteCategory(idCategory);
		if (ok)
			return ResponseEntity.ok(new RestAPIResponse(200,"Catégorie supprimee"));
		else throw new Exception("Erreur lors de la suppression de la catégorie");
	}

	@PostMapping("/api/category/add")
	public Object addCategory(@RequestBody CategoryDTO categoryDTO) throws Exception 
																						
	{
		System.out.println(categoryDTO.getName());
		if (!categoryService.exists(categoryDTO.getName())) {
			categoryService.createCategory(categoryDTO.getName());
			return ResponseEntity.ok(categoryDTO);
		} else {
			throw new Exception("Cette catégorie exixte deja"); 
	}
}

    @PostMapping("/api/videos/add")
    public ResponseEntity<?> addVideo(@RequestBody VideoDTO dto) {
        Video added = videoService.addIfAbsent(dto.getTitle(), dto.getDescription(), dto.getUrl(), dto.getCategoryName(), dto.getAgeRange());
        VideoDTO out = new VideoDTO(added.getId(), added.getTitle(), added.getDescription(), added.getFileName(), added.getAgeRange());
        return ResponseEntity.ok(out);
    }
}
