package fr.vod.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.vod.model.Category;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer> {

	public Category findByName(String name);
	public Category findCategoryById(Integer id);
}
