package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.model.Review;
import do_an_java.quan_ly_my_pham.model.ReviewStatus;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.OrderRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.repository.ReviewRepository;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Review> findApprovedByProduct(Integer productId) {
        return reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }

    public List<Review> findAllForAdmin() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> findByStatusForAdmin(ReviewStatus status) {
        if (status == null) {
            return findAllForAdmin();
        }

        return reviewRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Review> findPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING);
    }

    public Set<Integer> findReviewedProductIds(Integer userId, Integer orderId) {
        return reviewRepository.findByUserIdAndOrderId(userId, orderId).stream()
            .map(review -> review.getProduct().getId())
            .collect(Collectors.toSet());
    }

    @Transactional
    public Review createReview(Integer userId, Integer orderId, Integer productId, Integer stars, String comment) {
        validateReviewInput(stars, comment);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay nguoi dung"));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Ban khong co quyen danh gia don hang nay");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Chi co the danh gia khi don hang da hoan thanh");
        }

        boolean productInOrder = order.getOrderDetails().stream()
            .anyMatch(detail -> detail.getProduct().getId().equals(productId));
        if (!productInOrder) {
            throw new BusinessException("San pham khong thuoc don hang nay");
        }

        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, orderId, productId)) {
            throw new BusinessException("Ban da danh gia san pham nay trong don hang nay");
        }

        Review review = new Review();
        review.setUser(user);
        review.setOrder(order);
        review.setProduct(product);
        review.setStars(stars);
        review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
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

    private void validateReviewInput(Integer stars, String comment) {
        if (stars == null || stars < 1 || stars > 5) {
            throw new BusinessException("So sao phai tu 1 den 5");
        }
        if (comment != null && comment.length() > 500) {
            throw new BusinessException("Binh luan toi da 500 ky tu");
        }
    }
}
