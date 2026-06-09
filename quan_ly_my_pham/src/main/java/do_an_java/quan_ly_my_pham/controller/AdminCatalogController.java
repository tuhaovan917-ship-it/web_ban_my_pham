package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.service.CatalogAdminService;
import do_an_java.quan_ly_my_pham.service.dto.BrandForm;
import do_an_java.quan_ly_my_pham.service.dto.CategoryForm;
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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCatalogController {
    private final CatalogAdminService catalogAdminService;

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogAdminService.findAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", null);
        model.addAttribute("actionUrl", "/admin/categories");
        model.addAttribute("pageTitle", "Them danh muc");
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String createCategory(
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            catalogAdminService.createCategory(new CategoryForm(name, description, active));
            redirectAttributes.addFlashAttribute("successMessage", "Da them danh muc");
            return "redirect:/admin/categories";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/categories/new";
        }
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Integer id, Model model) {
        model.addAttribute("category", catalogAdminService.findCategory(id));
        model.addAttribute("actionUrl", "/admin/categories/" + id);
        model.addAttribute("pageTitle", "Sua danh muc");
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(
        @PathVariable Integer id,
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            catalogAdminService.updateCategory(id, new CategoryForm(name, description, active));
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat danh muc");
            return "redirect:/admin/categories";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean deleted = catalogAdminService.deleteCategory(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            deleted ? "Da xoa danh muc" : "Danh muc dang co san pham nen he thong da tam an danh muc"
        );
        return "redirect:/admin/categories";
    }

    @GetMapping("/brands")
    public String brands(Model model) {
        model.addAttribute("brands", catalogAdminService.findAllBrands());
        return "admin/brands";
    }

    @GetMapping("/brands/new")
    public String newBrand(Model model) {
        model.addAttribute("brand", null);
        model.addAttribute("actionUrl", "/admin/brands");
        model.addAttribute("pageTitle", "Them thuong hieu");
        return "admin/brand-form";
    }

    @PostMapping("/brands")
    public String createBrand(
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            catalogAdminService.createBrand(new BrandForm(name, description, active));
            redirectAttributes.addFlashAttribute("successMessage", "Da them thuong hieu");
            return "redirect:/admin/brands";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/brands/new";
        }
    }

    @GetMapping("/brands/{id}/edit")
    public String editBrand(@PathVariable Integer id, Model model) {
        model.addAttribute("brand", catalogAdminService.findBrand(id));
        model.addAttribute("actionUrl", "/admin/brands/" + id);
        model.addAttribute("pageTitle", "Sua thuong hieu");
        return "admin/brand-form";
    }

    @PostMapping("/brands/{id}")
    public String updateBrand(
        @PathVariable Integer id,
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        RedirectAttributes redirectAttributes
    ) {
        try {
            catalogAdminService.updateBrand(id, new BrandForm(name, description, active));
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat thuong hieu");
            return "redirect:/admin/brands";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/brands/" + id + "/edit";
        }
    }

    @PostMapping("/brands/{id}/delete")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean deleted = catalogAdminService.deleteBrand(id);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            deleted ? "Da xoa thuong hieu" : "Thuong hieu dang co san pham nen he thong da tam an thuong hieu"
        );
        return "redirect:/admin/brands";
    }
}
