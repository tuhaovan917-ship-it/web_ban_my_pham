package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.model.Review;
import do_an_java.quan_ly_my_pham.model.ReviewStatus;
import do_an_java.quan_ly_my_pham.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public String reviews(@RequestParam(required = false) ReviewStatus status, Model model) {
        model.addAttribute("reviews", reviewService.findByStatusForAdmin(status));
        model.addAttribute("statuses", ReviewStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/reviews";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
        @PathVariable Integer id,
        @RequestParam ReviewStatus status,
        RedirectAttributes redirectAttributes
    ) {
        Review review = reviewService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Da cap nhat danh gia #" + review.getId() + " sang " + review.getStatus()
        );
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Da xoa danh gia");
        return "redirect:/admin/reviews";
    }
}
