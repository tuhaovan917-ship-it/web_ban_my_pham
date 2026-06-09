package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Review;
import do_an_java.quan_ly_my_pham.model.ReviewStatus;
import do_an_java.quan_ly_my_pham.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<Review> findApprovedByProduct(Integer productId) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }

    public List<Review> findPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING);
    }

    @Transactional
    public Review updateStatus(Integer reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay danh gia"));
        review.setStatus(status);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new NotFoundException("Khong tim thay danh gia");
        }
        reviewRepository.deleteById(reviewId);
    }
}
