package do_an_java.quan_ly_my_pham.service.dto;

import java.math.BigDecimal;

public record DashboardSummary(
    long totalOrders,
    long totalCustomers,
    long totalProducts,
    long lowStockProducts,
    BigDecimal completedRevenue
) {
}
