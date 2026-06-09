package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Brand;
import do_an_java.quan_ly_my_pham.model.Category;
import do_an_java.quan_ly_my_pham.repository.BrandRepository;
import do_an_java.quan_ly_my_pham.repository.CategoryRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.service.dto.BrandForm;
import do_an_java.quan_ly_my_pham.service.dto.CategoryForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogAdminService {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public List<Category> findAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<Brand> findAllBrands() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Category findCategory(Integer id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Khong tim thay danh muc"));
    }

    public Brand findBrand(Integer id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Khong tim thay thuong hieu"));
    }

    @Transactional
    public Category createCategory(CategoryForm form) {
        validateCategory(form, null);

        Category category = new Category();
        applyCategoryForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryForm form) {
        validateCategory(form, id);

        Category category = findCategory(id);
        applyCategoryForm(category, form);
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean deleteCategory(Integer id) {
        Category category = findCategory(id);
        if (productRepository.countByCategoryId(id) > 0) {
            category.setActive(false);
            categoryRepository.save(category);
            return false;
        }

        categoryRepository.delete(category);
        return true;
    }

    @Transactional
    public Brand createBrand(BrandForm form) {
        validateBrand(form, null);

        Brand brand = new Brand();
        applyBrandForm(brand, form);
        return brandRepository.save(brand);
    }

    @Transactional
    public Brand updateBrand(Integer id, BrandForm form) {
        validateBrand(form, id);

        Brand brand = findBrand(id);
        applyBrandForm(brand, form);
        return brandRepository.save(brand);
    }

    @Transactional
    public boolean deleteBrand(Integer id) {
        Brand brand = findBrand(id);
        if (productRepository.countByBrandId(id) > 0) {
            brand.setActive(false);
            brandRepository.save(brand);
            return false;
        }

        brandRepository.delete(brand);
        return true;
    }

    private void validateCategory(CategoryForm form, Integer currentId) {
        if (form.name() == null || form.name().isBlank()) {
            throw new BusinessException("Ten danh muc khong duoc de trong");
        }
        if (form.name().trim().length() > 50) {
            throw new BusinessException("Ten danh muc toi da 50 ky tu");
        }

        boolean duplicated = currentId == null
            ? categoryRepository.existsByNameIgnoreCase(form.name().trim())
            : categoryRepository.existsByNameIgnoreCaseAndIdNot(form.name().trim(), currentId);
        if (duplicated) {
            throw new BusinessException("Ten danh muc da ton tai");
        }
    }

    private void validateBrand(BrandForm form, Integer currentId) {
        if (form.name() == null || form.name().isBlank()) {
            throw new BusinessException("Ten thuong hieu khong duoc de trong");
        }
        if (form.name().trim().length() > 80) {
            throw new BusinessException("Ten thuong hieu toi da 80 ky tu");
        }

        boolean duplicated = currentId == null
            ? brandRepository.existsByNameIgnoreCase(form.name().trim())
            : brandRepository.existsByNameIgnoreCaseAndIdNot(form.name().trim(), currentId);
        if (duplicated) {
            throw new BusinessException("Ten thuong hieu da ton tai");
        }
    }

    private void applyCategoryForm(Category category, CategoryForm form) {
        category.setName(form.name().trim());
        category.setDescription(blankToNull(form.description()));
        category.setActive(form.active() == null || Boolean.TRUE.equals(form.active()));
    }

    private void applyBrandForm(Brand brand, BrandForm form) {
        brand.setName(form.name().trim());
        brand.setDescription(blankToNull(form.description()));
        brand.setActive(form.active() == null || Boolean.TRUE.equals(form.active()));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
