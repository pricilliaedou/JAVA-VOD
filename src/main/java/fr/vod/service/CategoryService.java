package fr.vod.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.vod.model.Category;
import fr.vod.repository.CategoryRepository;

@Service
public class CategoryService {

	@Autowired
	CategoryRepository categoryRepository;

	public List<Category> list() {
		return (List<Category>) categoryRepository.findAll();
	}

	public boolean deleteCategory(String idCategory) 
	{
		try {
			categoryRepository.delete(categoryRepository.findCategoryById(Integer.valueOf(idCategory)));
		return true;	
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean exists(String name) {
		return categoryRepository.findByName(name) != null;
	}

	public Category createCategory(String name) {
		Category category = new Category();
		category.setName(name);
		categoryRepository.save(category); // permet à enregistrer en base de données
		return category;
	}
}
