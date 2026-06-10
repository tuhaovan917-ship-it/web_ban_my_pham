package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.DiscountType;
import do_an_java.quan_ly_my_pham.model.Promotion;
import do_an_java.quan_ly_my_pham.repository.PromotionRepository;
import do_an_java.quan_ly_my_pham.service.dto.PromotionDiscount;
import do_an_java.quan_ly_my_pham.service.dto.PromotionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final PromotionRepository promotionRepository;

    public List<Promotion> findAllForAdmin() {
        return promotionRepository.findAllByOrderByStartDateDesc();
    }

    public Promotion findById(Integer promotionId) {
        return promotionRepository.findById(promotionId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy khuyến mãi"));
    }

    public PromotionDiscount calculateDiscount(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return PromotionDiscount.none();
        }

        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code.trim())
            .orElseThrow(() -> new BusinessException("Mã khuyến mãi không tồn tại"));

        validatePromotion(promotion, subtotal);
        BigDecimal amount = calculateAmount(promotion, subtotal);
        return new PromotionDiscount(promotion, amount);
    }

    public void increaseUsedCount(Promotion promotion) {
        if (promotion == null) {
            return;
        }

        int usedCount = promotion.getUsedCount() == null ? 0 : promotion.getUsedCount();
        promotion.setUsedCount(usedCount + 1);
        promotionRepository.save(promotion);
    }

    public void decreaseUsedCount(Promotion promotion) {
        if (promotion == null) {
            return;
        }

        int usedCount = promotion.getUsedCount() == null ? 0 : promotion.getUsedCount();
        promotion.setUsedCount(Math.max(0, usedCount - 1));
        promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion createPromotion(PromotionForm form) {
        validatePromotionForm(form, null);

        Promotion promotion = new Promotion();
        promotion.setUsedCount(0);
        applyPromotionForm(promotion, form);
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion updatePromotion(Integer promotionId, PromotionForm form) {
        validatePromotionForm(form, promotionId);

        Promotion promotion = findById(promotionId);
        if (form.usageLimit() != null
            && promotion.getUsedCount() != null
            && form.usageLimit() < promotion.getUsedCount()) {
            throw new BusinessException("Giới hạn lượt dùng không được nhỏ hơn số lượt đã sử dụng");
        }
        applyPromotionForm(promotion, form);
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion toggleActive(Integer promotionId) {
        Promotion promotion = findById(promotionId);
        promotion.setActive(!Boolean.TRUE.equals(promotion.getActive()));
        return promotionRepository.save(promotion);
    }

    @Transactional
    public boolean deletePromotion(Integer promotionId) {
        Promotion promotion = findById(promotionId);
        if (promotion.getUsedCount() != null && promotion.getUsedCount() > 0) {
            promotion.setActive(false);
            promotionRepository.save(promotion);
            return false;
        }

        promotionRepository.delete(promotion);
        return true;
    }

    private void validatePromotionForm(PromotionForm form, Integer currentId) {
        if (form.code() == null || form.code().isBlank()) {
            throw new BusinessException("Mã khuyến mãi không được để trống");
        }
        String code = form.code().trim().toUpperCase();
        if (code.length() > 30) {
            throw new BusinessException("Mã khuyến mãi tối đa 30 ký tự");
        }
        boolean duplicated = currentId == null
            ? promotionRepository.existsByCodeIgnoreCase(code)
            : promotionRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        if (duplicated) {
            throw new BusinessException("Mã khuyến mãi đã tồn tại");
        }

        if (form.discountType() == null) {
            throw new BusinessException("Loại giảm giá không được để trống");
        }
        if (form.discountValue() == null || form.discountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá trị giảm giá phải lớn hơn 0");
        }
        if (form.discountType() == DiscountType.PERCENT && form.discountValue().compareTo(ONE_HUNDRED) > 0) {
            throw new BusinessException("Giảm giá phần trăm không được lớn hơn 100");
        }
        if (form.minOrderAmount() != null && form.minOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Giá trị đơn tối thiểu không được âm");
        }
        if (form.maxDiscount() != null && form.maxDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Mức giảm tối đa không được âm");
        }
        if (form.startDate() == null || form.endDate() == null) {
            throw new BusinessException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (!form.endDate().isAfter(form.startDate())) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (form.usageLimit() != null && form.usageLimit() < 0) {
            throw new BusinessException("Giới hạn lượt dùng không được âm");
        }
    }

    private void applyPromotionForm(Promotion promotion, PromotionForm form) {
        promotion.setCode(form.code().trim().toUpperCase());
        promotion.setDescription(blankToNull(form.description()));
        promotion.setDiscountType(form.discountType());
        promotion.setDiscountValue(form.discountValue());
        promotion.setMinOrderAmount(form.minOrderAmount() == null ? BigDecimal.ZERO : form.minOrderAmount());
        promotion.setMaxDiscount(form.maxDiscount());
        promotion.setStartDate(form.startDate());
        promotion.setEndDate(form.endDate());
        promotion.setUsageLimit(form.usageLimit());
        promotion.setActive(form.active() == null || Boolean.TRUE.equals(form.active()));
    }

    private void validatePromotion(Promotion promotion, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(promotion.getActive())) {
            throw new BusinessException("Mã khuyến mãi đã bị tắt");
        }

        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new BusinessException("Mã khuyến mãi không nằm trong thời gian áp dụng");
        }

        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new BusinessException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        if (subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã khuyến mãi");
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
