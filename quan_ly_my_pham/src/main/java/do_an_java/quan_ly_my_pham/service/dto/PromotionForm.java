package do_an_java.quan_ly_my_pham.service.dto;

import do_an_java.quan_ly_my_pham.model.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionForm(
    String code,
    String description,
    DiscountType discountType,
    BigDecimal discountValue,
    BigDecimal minOrderAmount,
    BigDecimal maxDiscount,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer usageLimit,
    Boolean active
) {
}
