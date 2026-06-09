package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Integer productId);
}
