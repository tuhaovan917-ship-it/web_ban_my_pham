package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.DiscountType;
import do_an_java.quan_ly_my_pham.model.Promotion;
import do_an_java.quan_ly_my_pham.service.PromotionService;
import do_an_java.quan_ly_my_pham.service.dto.PromotionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {
    private final PromotionService promotionService;

    @GetMapping
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionService.findAllForAdmin());
        return "admin/promotions";
    }

    @GetMapping("/new")
    public String newPromotion(Model model) {
        addFormOptions(model);
        model.addAttribute("promotion", null);
        model.addAttribute("actionUrl", "/admin/promotions");
        model.addAttribute("pageTitle", "Thêm khuyến mãi");
        return "admin/promotion-form";
    }

    @PostMapping
    public String createPromotion(
        @RequestParam String code,
        @RequestParam(required = false) String description,
        @RequestParam DiscountType discountType,
        @RequestParam BigDecimal discountValue,
        @RequestParam(required = false) BigDecimal minOrderAmount,
        @RequestParam(required = false) BigDecimal maxDiscount,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) Integer usageLimit,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Promotion promotion = promotionService.createPromotion(toForm(
                code,
                description,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscount,
                startDate,
                endDate,
                usageLimit,
                active
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm mã khuyến mãi " + promotion.getCode());
            return "redirect:/admin/promotions";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/promotions/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editPromotion(@PathVariable Integer id, Model model) {
        addFormOptions(model);
        model.addAttribute("promotion", promotionService.findById(id));
        model.addAttribute("actionUrl", "/admin/promotions/" + id);
        model.addAttribute("pageTitle", "Sửa khuyến mãi");
        return "admin/promotion-form";
    }

    @PostMapping("/{id}")
    public String updatePromotion(
        @PathVariable Integer id,
        @RequestParam String code,
        @RequestParam(required = false) String description,
        @RequestParam DiscountType discountType,
        @RequestParam BigDecimal discountValue,
        @RequestParam(required = false) BigDecimal minOrderAmount,
        @RequestParam(required = false) BigDecimal maxDiscount,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) Integer usageLimit,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Promotion promotion = promotionService.updatePromotion(id, toForm(
                code,
                description,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscount,
                startDate,
                endDate,
                usageLimit,
                active
            ));
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mã khuyến mãi " + promotion.getCode());
            return "redirect:/admin/promotions";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/promotions/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Promotion promotion = promotionService.toggleActive(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            Boolean.TRUE.equals(promotion.getActive()) ? "Đã bật mã khuyến mãi" : "Đã tắt mã khuyến mãi"
        );
        return "redirect:/admin/promotions";
    }

    @PostMapping("/{id}/delete")
    public String deletePromotion(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean deleted = promotionService.deletePromotion(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            deleted ? "Đã xóa mã khuyến mãi" : "Mã đã có lượt sử dụng nên hệ thống đã tạm tắt mã khuyến mãi"
        );
        return "redirect:/admin/promotions";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("discountTypes", DiscountType.values());
    }

    private PromotionForm toForm(
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
        return new PromotionForm(
            code,
            description,
            discountType,
            discountValue,
            minOrderAmount,
            maxDiscount,
            startDate,
            endDate,
            usageLimit,
            active
        );
    }
}
