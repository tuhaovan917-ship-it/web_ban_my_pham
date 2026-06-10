package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.repository.BrandRepository;
import do_an_java.quan_ly_my_pham.repository.CategoryRepository;
import do_an_java.quan_ly_my_pham.service.ProductService;
import do_an_java.quan_ly_my_pham.service.dto.ProductForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @GetMapping
    public String products(Model model) {
        model.addAttribute("products", productService.findAllForAdmin());
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        addFormOptions(model);
        model.addAttribute("product", null);
        model.addAttribute("actionUrl", "/admin/products");
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        return "admin/product-form";
    }

    @PostMapping
    public String createProduct(
        @RequestParam Integer categoryId,
        @RequestParam(required = false) Integer brandId,
        @RequestParam String name,
        @RequestParam BigDecimal price,
        @RequestParam(required = false) BigDecimal salePrice,
        @RequestParam Integer stockQuantity,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) MultipartFile imageFile,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Product product = productService.createProduct(
                toForm(categoryId, brandId, name, price, salePrice, stockQuantity, description, featured, active),
                imageFile
            );
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm " + product.getName());
            return "redirect:/admin/products";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable Integer id, Model model) {
        addFormOptions(model);
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("actionUrl", "/admin/products/" + id);
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        return "admin/product-form";
    }

    @PostMapping("/{id}")
    public String updateProduct(
        @PathVariable Integer id,
        @RequestParam Integer categoryId,
        @RequestParam(required = false) Integer brandId,
        @RequestParam String name,
        @RequestParam BigDecimal price,
        @RequestParam(required = false) BigDecimal salePrice,
        @RequestParam Integer stockQuantity,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) MultipartFile imageFile,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Product product = productService.updateProduct(
                id,
                toForm(categoryId, brandId, name, price, salePrice, stockQuantity, description, featured, active),
                imageFile
            );
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật sản phẩm " + product.getName());
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Product product = productService.toggleActive(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            Boolean.TRUE.equals(product.getActive()) ? "Đã mở bán sản phẩm" : "Đã tạm ẩn sản phẩm"
        );
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean deleted = productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            deleted
                ? "Đã xóa sản phẩm"
                : "Sản phẩm đã có lịch sử đơn hàng/đánh giá nên hệ thống đã tạm ẩn sản phẩm"
        );
        return "redirect:/admin/products";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
    }

    private ProductForm toForm(
        Integer categoryId,
        Integer brandId,
        String name,
        BigDecimal price,
        BigDecimal salePrice,
        Integer stockQuantity,
        String description,
        Boolean featured,
        Boolean active
    ) {
        return new ProductForm(
            categoryId,
            brandId,
            name,
            price,
            salePrice,
            stockQuantity,
            description,
            featured,
            active
        );
    }
}
