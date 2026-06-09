package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.DiscountType;
import do_an_java.quan_ly_my_pham.model.Promotion;
import do_an_java.quan_ly_my_pham.repository.PromotionRepository;
import do_an_java.quan_ly_my_pham.service.dto.PromotionDiscount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final PromotionRepository promotionRepository;

    public PromotionDiscount calculateDiscount(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return PromotionDiscount.none();
        }

        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code.trim())
            .orElseThrow(() -> new BusinessException("Ma khuyen mai khong ton tai"));

        validatePromotion(promotion, subtotal);
        BigDecimal amount = calculateAmount(promotion, subtotal);
        return new PromotionDiscount(promotion, amount);
    }

    public void increaseUsedCount(Promotion promotion) {
        if (promotion == null) {
            return;
        }

        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);
    }

    private void validatePromotion(Promotion promotion, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(promotion.getActive())) {
            throw new BusinessException("Ma khuyen mai da bi tat");
        }

        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new BusinessException("Ma khuyen mai khong nam trong thoi gian ap dung");
        }

        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new BusinessException("Ma khuyen mai da het luot su dung");
        }

        if (subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BusinessException("Don hang chua dat gia tri toi thieu de dung ma khuyen mai");
        }
    }

    private BigDecimal calculateAmount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal amount;
        if (promotion.getDiscountType() == DiscountType.PERCENT) {
            amount = subtotal.multiply(promotion.getDiscountValue())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        } else {
            amount = promotion.getDiscountValue();
        }

        if (promotion.getMaxDiscount() != null && amount.compareTo(promotion.getMaxDiscount()) > 0) {
            amount = promotion.getMaxDiscount();
        }

        if (amount.compareTo(subtotal) > 0) {
            return subtotal;
        }

        return amount;
    }
}
