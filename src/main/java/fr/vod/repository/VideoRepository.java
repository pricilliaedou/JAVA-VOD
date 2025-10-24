
package fr.vod.repository;

import fr.vod.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {

  @Query(value = "SELECT * FROM video ORDER BY Id DESC LIMIT 3", nativeQuery = true)
  List<Video> findTop3();

  Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);
  List<Video> findByIsTestimony(boolean isTestimony);
  List<Video> findByIsTestimonyAndAgeRangeIn(boolean isTestimony, List<String> ages);
  List<Video> findByIsTestimonyAndCategory_Name(boolean isTestimony, String categoryName);
  
  List<Video> findByIsTestimonyAndCategory_NameAndAgeRangeIn(
	      boolean isTestimony, String categoryName, List<String> ages);
  
  List<Video> findTop3ByIsHomeFeaturedTrueOrderByHomeOrderAscIdAsc();
  
  boolean existsByFileName(String fileName);
  Optional<Video> findByFileName(String fileName);
}
