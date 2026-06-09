package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.CartItem;
import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderDetail;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import do_an_java.quan_ly_my_pham.model.OrderStatusHistory;
import do_an_java.quan_ly_my_pham.model.Payment;
import do_an_java.quan_ly_my_pham.model.PaymentStatus;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.OrderRepository;
import do_an_java.quan_ly_my_pham.repository.OrderStatusHistoryRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import do_an_java.quan_ly_my_pham.service.dto.CheckoutRequest;
import do_an_java.quan_ly_my_pham.service.dto.PromotionDiscount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_CONFIRMATION, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPING, Set.of(OrderStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, Set.of());
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
        ALLOWED_TRANSITIONS.put(OrderStatus.PAYMENT_IN_PROGRESS, Set.of(OrderStatus.PENDING_CONFIRMATION, OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
    }

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final PromotionService promotionService;

    public List<Order> findOrdersByUser(Integer userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order findById(Integer orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay don hang"));
    }

    @Transactional
    public Order checkout(CheckoutRequest request) {
        validateCheckoutRequest(request);

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new NotFoundException("Khong tim thay nguoi dung"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException("Tai khoan dang bi khoa");
        }

        List<CartItem> items = cartService.getItems(request.userId());
        if (items.isEmpty()) {
            throw new BusinessException("Gio hang dang trong");
        }

        List<Product> productsToUpdate = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = item.getProduct();
            cartService.ensureStockAvailable(product, item.getQuantity());
            subtotal = subtotal.add(productService.currentPrice(product).multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        PromotionDiscount discount = promotionService.calculateDiscount(request.promotionCode(), subtotal);
        BigDecimal shippingFee = request.shippingFee() == null ? BigDecimal.ZERO : request.shippingFee();
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Phi giao hang khong duoc am");
        }

        BigDecimal total = subtotal.subtract(discount.amount()).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        Order order = new Order();
        order.setUser(user);
        order.setPromotion(discount.promotion());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setReceiverName(request.receiverName().trim());
        order.setReceiverPhone(request.receiverPhone().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod());
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount.amount());
        order.setShippingFee(shippingFee);
        order.setTotalAmount(total);
        order.setNote(request.note());

        for (CartItem item : items) {
            Product product = item.getProduct();
            BigDecimal unitPrice = productService.currentPrice(product);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setProductName(product.getName());
            detail.setUnitPrice(unitPrice);
            detail.setQuantity(item.getQuantity());
            detail.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            order.getOrderDetails().add(detail);

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productsToUpdate.add(product);
        }

        productRepository.saveAll(productsToUpdate);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.UNPAID);
        payment.setAmount(total);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        saveStatusHistory(savedOrder, null, OrderStatus.PENDING_CONFIRMATION, user, "Tao don hang - cho admin xac nhan");
        promotionService.increaseUsedCount(savedOrder.getPromotion());
        cartService.clearCart(user.getId());
        return savedOrder;
    }

    @Transactional
    public Order updateStatus(Integer orderId, OrderStatus newStatus, Integer changedByUserId, String note) {
        Order order = findById(orderId);
        OrderStatus oldStatus = order.getStatus();

        if (!ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of()).contains(newStatus)) {
            throw new BusinessException("Khong the chuyen trang thai don hang tu " + oldStatus + " sang " + newStatus);
        }

        User changedBy = null;
        if (changedByUserId != null) {
            changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay nguoi cap nhat"));
        }

        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            restoreStock(order);
            promotionService.decreaseUsedCount(order.getPromotion());
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        saveStatusHistory(savedOrder, oldStatus, newStatus, changedBy, note);
        return savedOrder;
    }

    @Transactional
    public Order cancelByCustomer(Integer orderId, Integer userId) {
        Order order = findById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Ban khong co quyen huy don hang nay");
        }

        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION && order.getStatus() != OrderStatus.PAYMENT_IN_PROGRESS) {
            throw new BusinessException("Chi co the huy don khi don hang dang cho xac nhan");
        }

        return updateStatus(orderId, OrderStatus.CANCELLED, userId, "Khach hang huy don");
    }

    private void restoreStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
    }

    private void saveStatusHistory(
        Order order,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        User changedBy,
        String note
    ) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);
        orderStatusHistoryRepository.save(history);
    }

    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request.userId() == null) {
            throw new BusinessException("Nguoi dung khong duoc de trong");
        }
        if (request.receiverName() == null || request.receiverName().isBlank()) {
            throw new BusinessException("Ten nguoi nhan khong duoc de trong");
        }
        if (request.receiverPhone() == null || !request.receiverPhone().trim().matches("^0[0-9]{9}$")) {
            throw new BusinessException("So dien thoai nguoi nhan phai gom 10 chu so va bat dau bang 0");
        }
        if (request.shippingAddress() == null || request.shippingAddress().isBlank()) {
            throw new BusinessException("Dia chi giao hang khong duoc de trong");
        }
        if (request.paymentMethod() == null) {
            throw new BusinessException("Phuong thuc thanh toan khong duoc de trong");
        }
    }
}
