package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public String listProducts(Model model) {
        // Lấy toàn bộ sản phẩm từ DB
        model.addAttribute("products", productRepository.findAll());
        return "products"; // Trả về file products.html trong thư mục templates
    }
}