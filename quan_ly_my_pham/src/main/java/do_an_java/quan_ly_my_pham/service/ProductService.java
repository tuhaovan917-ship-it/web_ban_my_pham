package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.service.dto.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public Product findById(Integer productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
    }

    public List<Product> findNewestProducts() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    public List<Product> findLowStockProducts() {
        return productRepository.findAll((root, query, builder) -> builder.and(
            builder.isTrue(root.get("active")),
            builder.lessThan(root.get("stockQuantity"), LOW_STOCK_THRESHOLD)
        ), Sort.by(Sort.Direction.ASC, "stockQuantity"));
    }

    public List<Product> filter(ProductFilter filter) {
        return productRepository.findAll(toSpecification(filter), toSort(filter.sort()));
    }

    public BigDecimal currentPrice(Product product) {
        if (product.getSalePrice() != null) {
            return product.getSalePrice();
        }
        return product.getPrice();
    }

    private Specification<Product> toSpecification(ProductFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isTrue(root.get("active")));

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                predicates.add(builder.like(
                    builder.lower(root.get("name")),
                    "%" + filter.keyword().trim().toLowerCase() + "%"
                ));
            }

            if (filter.categoryId() != null) {
                predicates.add(builder.equal(root.get("category").get("id"), filter.categoryId()));
            }

            if (filter.brandId() != null) {
                predicates.add(builder.equal(root.get("brand").get("id"), filter.brandId()));
            }

            if (filter.minPrice() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }

            if (filter.maxPrice() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            if (filter.featured() != null) {
                predicates.add(builder.equal(root.get("featured"), filter.featured()));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort toSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
