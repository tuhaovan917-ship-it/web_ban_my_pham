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
            .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền đánh giá đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Chỉ có thể đánh giá khi đơn hàng đã hoàn thành");
        }

        boolean productInOrder = order.getOrderDetails().stream()
            .anyMatch(detail -> detail.getProduct().getId().equals(productId));
        if (!productInOrder) {
            throw new BusinessException("Sản phẩm không thuộc đơn hàng này");
        }

        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, orderId, productId)) {
            throw new BusinessException("Bạn đã đánh giá sản phẩm này trong đơn hàng này");
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
            .orElseThrow(() -> new NotFoundException("Không tìm thấy đánh giá"));
        review.setStatus(status);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new NotFoundException("Không tìm thấy đánh giá");
        }
        reviewRepository.deleteById(reviewId);
    }

    private void validateReviewInput(Integer stars, String comment) {
        if (stars == null || stars < 1 || stars > 5) {
            throw new BusinessException("Số sao phải từ 1 đến 5");
        }
        if (comment != null && comment.length() > 500) {
            throw new BusinessException("Bình luận tối đa 500 ký tự");
        }
    }
}
