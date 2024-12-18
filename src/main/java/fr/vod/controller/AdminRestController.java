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
import fr.vod.service.CategoryService;

@RestController
public class AdminRestController {

	@Autowired
	CategoryService categoryService;
	
	@DeleteMapping("/api/category/delete/{idCategory}")
	public Object deleteCategory(@PathVariable String idCategory) throws Exception{
		boolean ok = categoryService.deleteCategory(idCategory);
		if (ok)
			return ResponseEntity.ok(new RestAPIResponse(200,"thematique supprimee"));
		else throw new Exception("Pb a la suppression");
	}

	@PostMapping("/api/category/add")
	public Object addCategory(@RequestBody CategoryDTO categoryDTO) throws Exception // autorise la methode à lever des
																						// exception
	{
		System.out.println(categoryDTO.getName());
		if (!categoryService.exists(categoryDTO.getName())) {
			categoryService.createCategory(categoryDTO.getName());
			return ResponseEntity.ok(categoryDTO);
		} else
			throw new Exception("Thématique exixte deja");// lever une exception pour dire que la thematique existe deja pour qu'elle n'y soit pas deux fois 
	}
}
