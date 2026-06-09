package do_an_java.quan_ly_my_pham.repository;

import do_an_java.quan_ly_my_pham.model.Review;
import do_an_java.quan_ly_my_pham.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductIdAndStatus(Integer productId, ReviewStatus status);

    List<Review> findByStatus(ReviewStatus status);

    long countByProductId(Integer productId);
}
