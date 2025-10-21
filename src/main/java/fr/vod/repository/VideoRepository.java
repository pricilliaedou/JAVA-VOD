
package fr.vod.repository;

import fr.vod.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {

  @Query(value = "SELECT * FROM video ORDER BY Id DESC LIMIT 3", nativeQuery = true)
  List<Video> findTop3();

  Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
