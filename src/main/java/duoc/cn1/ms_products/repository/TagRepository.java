package duoc.cn1.ms_products.repository;

import duoc.cn1.ms_products.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findByName(String name);

	boolean existsByName(String name);
}
