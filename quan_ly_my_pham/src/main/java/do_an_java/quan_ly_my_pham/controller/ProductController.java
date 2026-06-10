package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.repository.BrandRepository;
import do_an_java.quan_ly_my_pham.repository.CategoryRepository;
import do_an_java.quan_ly_my_pham.service.ProductService;
import do_an_java.quan_ly_my_pham.service.ReviewService;
import do_an_java.quan_ly_my_pham.service.dto.ProductFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newestProducts", productService.findNewestProducts());
        model.addAttribute("featuredProducts", productService.findFeaturedProducts());
        return "home";
    }

    @GetMapping("/home")
    public String userHome(Model model) {
        model.addAttribute("newestProducts", productService.findNewestProducts());
        model.addAttribute("featuredProducts", productService.findFeaturedProducts());
        return "home";
    }

    @GetMapping("/products")
    public String listProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) Integer brandId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(required = false) String sort,
        Model model
    ) {
        ProductFilter filter = new ProductFilter(keyword, categoryId, brandId, minPrice, maxPrice, featured, sort);
        model.addAttribute("products", productService.filter(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("brands", brandRepository.findByActiveTrueOrderByNameAsc());
        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        Product product = productService.findVisibleById(id);
        model.addAttribute("product", product);
        model.addAttribute("currentPrice", productService.currentPrice(product));
        model.addAttribute("reviews", reviewService.findApprovedByProduct(id));
        return "product-detail";
    }
}
