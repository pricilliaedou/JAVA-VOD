package fr.vod.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.vod.dto.CategoryDTO;
import fr.vod.dto.HomeDTO;
import fr.vod.model.Category;
import fr.vod.service.CategoryService;
import fr.vod.service.VideoService;

@RestController
public class HomeRestController {

	@Autowired
	CategoryService categoryService;
	@Autowired
	VideoService videoService;
	
	@GetMapping("/public/home")
	public HomeDTO getHome()
	{
		//creation de l'objet de retour
		HomeDTO homeDTO = new HomeDTO();
		//recuperation des categories en base 
		List<Category> categoryList = categoryService.list();
		List<CategoryDTO> listCategoryDTO = new ArrayList<CategoryDTO>();
		//transtypage des category => categoryDTO
		for (Category category : categoryList)
			listCategoryDTO.add(new CategoryDTO(category));
		//affectation des categoryDTO à l'objet de retour
		homeDTO.setCategoryListDTO(listCategoryDTO);
		homeDTO.setVideoListDTO(null);
		//retour API REST
		return homeDTO;
	}
}
