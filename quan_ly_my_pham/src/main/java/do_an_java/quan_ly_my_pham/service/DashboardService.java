package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import do_an_java.quan_ly_my_pham.model.UserRole;
import do_an_java.quan_ly_my_pham.repository.OrderRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import do_an_java.quan_ly_my_pham.service.dto.DashboardSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public DashboardSummary getSummary() {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long lowStockProducts = productService.findLowStockProducts().size();
        BigDecimal completedRevenue = orderRepository.findByStatus(OrderStatus.COMPLETED).stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummary(
            totalOrders,
            totalCustomers,
            totalProducts,
            lowStockProducts,
            completedRevenue
        );
    }
}
