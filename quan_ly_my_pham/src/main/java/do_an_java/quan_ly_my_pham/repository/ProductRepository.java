package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Integer categoryId);

    List<Product> findByBrandId(Integer brandId);

    @Query("""
        select p
        from Product p
        where p.active = true
          and p.category.active = true
          and (p.brand is null or p.brand.active = true)
        """)
    List<Product> findVisibleProducts();

    @Query("""
        select p
        from Product p
        where p.featured = true
          and p.active = true
          and p.category.active = true
          and (p.brand is null or p.brand.active = true)
        """)
    List<Product> findVisibleFeaturedProducts();

    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("""
        select p
        from Product p
        where p.active = true
          and p.category.active = true
          and (p.brand is null or p.brand.active = true)
        order by p.createdAt desc
        """)
    List<Product> findVisibleOrderByCreatedAtDesc();

    @Query("""
        select p
        from Product p
        where p.id = :productId
          and p.active = true
          and p.category.active = true
          and (p.brand is null or p.brand.active = true)
        """)
    Optional<Product> findVisibleById(@Param("productId") Integer productId);

    long countByCategoryId(Integer categoryId);

    long countByBrandId(Integer brandId);
}
