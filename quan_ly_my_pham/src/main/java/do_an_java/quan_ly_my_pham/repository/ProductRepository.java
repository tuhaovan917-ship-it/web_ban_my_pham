package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByBrandId(Integer brandId);

    List<Product> findByActiveTrue();

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();
}
