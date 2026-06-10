package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    List<Category> findByActiveTrueOrderByNameAsc();
}
