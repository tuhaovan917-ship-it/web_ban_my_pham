package do_an_java.quan_ly_my_pham.service.dto;

import do_an_java.quan_ly_my_pham.model.Promotion;

import java.math.BigDecimal;

public record PromotionDiscount(
    Promotion promotion,
    BigDecimal amount
) {
    public static PromotionDiscount none() {
        return new PromotionDiscount(null, BigDecimal.ZERO);
    }
}
