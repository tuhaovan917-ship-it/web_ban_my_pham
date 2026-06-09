package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Brand;
import do_an_java.quan_ly_my_pham.model.Category;
import do_an_java.quan_ly_my_pham.model.Product;
import do_an_java.quan_ly_my_pham.model.ProductImage;
import do_an_java.quan_ly_my_pham.repository.BrandRepository;
import do_an_java.quan_ly_my_pham.repository.CartItemRepository;
import do_an_java.quan_ly_my_pham.repository.CategoryRepository;
import do_an_java.quan_ly_my_pham.repository.OrderDetailRepository;
import do_an_java.quan_ly_my_pham.repository.ProductImageRepository;
import do_an_java.quan_ly_my_pham.repository.ProductRepository;
import do_an_java.quan_ly_my_pham.repository.ReviewRepository;
import do_an_java.quan_ly_my_pham.service.dto.ProductFilter;
import do_an_java.quan_ly_my_pham.service.dto.ProductForm;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final Path PRODUCT_IMAGE_DIRECTORY = Paths.get("uploads", "products");
    private static final DateTimeFormatter IMAGE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;

    public List<Product> findAllActive() {
        return productRepository.findVisibleProducts();
    }

    public List<Product> findAllForAdmin() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Product findById(Integer productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
    }

    public List<Product> findNewestProducts() {
        return productRepository.findVisibleOrderByCreatedAtDesc()
            .stream()
            .limit(8)
            .toList();
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findVisibleFeaturedProducts();
    }

    public Product findVisibleById(Integer productId) {
        return productRepository.findVisibleById(productId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
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

    @Transactional
    public Product createProduct(ProductForm form, MultipartFile imageFile) {
        Product product = new Product();
        product.setCreatedAt(LocalDateTime.now());
        applyProductForm(product, form);
        Product savedProduct = productRepository.save(product);
        updateMainImage(savedProduct, imageFile);
        return productRepository.save(savedProduct);
    }

    @Transactional
    public Product updateProduct(Integer productId, ProductForm form, MultipartFile imageFile) {
        Product product = findById(productId);
        applyProductForm(product, form);
        product.setUpdatedAt(LocalDateTime.now());
        updateMainImage(product, imageFile);
        return productRepository.save(product);
    }

    @Transactional
    public Product toggleActive(Integer productId) {
        Product product = findById(productId);
        product.setActive(!Boolean.TRUE.equals(product.getActive()));
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(Integer productId) {
        Product product = findById(productId);
        boolean hasHistory = orderDetailRepository.countByProductId(productId) > 0
            || reviewRepository.countByProductId(productId) > 0;

        cartItemRepository.deleteByProductId(productId);

        if (hasHistory) {
            product.setActive(false);
            product.setFeatured(false);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            return false;
        }

        productRepository.delete(product);
        return true;
    }

    private void applyProductForm(Product product, ProductForm form) {
        validateProductForm(form);

        Category category = categoryRepository.findById(form.categoryId())
            .orElseThrow(() -> new NotFoundException("Khong tim thay danh muc"));

        Brand brand = null;
        if (form.brandId() != null) {
            brand = brandRepository.findById(form.brandId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay thuong hieu"));
        }

        product.setCategory(category);
        product.setBrand(brand);
        product.setName(form.name().trim());
        product.setPrice(form.price());
        product.setSalePrice(form.salePrice());
        product.setStockQuantity(form.stockQuantity());
        product.setDescription(form.description());
        product.setFeatured(Boolean.TRUE.equals(form.featured()));
        product.setActive(form.active() == null || Boolean.TRUE.equals(form.active()));
    }

    private void validateProductForm(ProductForm form) {
        if (form.categoryId() == null) {
            throw new BusinessException("Danh muc khong duoc de trong");
        }
        if (form.name() == null || form.name().isBlank()) {
            throw new BusinessException("Ten san pham khong duoc de trong");
        }
        if (form.price() == null || form.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Gia san pham khong hop le");
        }
        if (form.salePrice() != null && form.salePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Gia khuyen mai khong hop le");
        }
        if (form.salePrice() != null && form.salePrice().compareTo(form.price()) > 0) {
            throw new BusinessException("Gia khuyen mai khong duoc lon hon gia goc");
        }
        if (form.stockQuantity() == null || form.stockQuantity() < 0) {
            throw new BusinessException("So luong ton kho khong hop le");
        }
    }

    private void updateMainImage(Product product, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        String originalFilename = imageFile.getOriginalFilename();
        String extension = extractImageExtension(originalFilename);
        String safeName = toSafeFileName(product.getName());
        String timestamp = LocalDateTime.now().format(IMAGE_TIMESTAMP_FORMATTER);
        String fileName = product.getId() + "-" + safeName + "-" + timestamp + extension;

        try {
            Files.createDirectories(PRODUCT_IMAGE_DIRECTORY);
            Path uploadDirectory = PRODUCT_IMAGE_DIRECTORY.toAbsolutePath().normalize();
            Path target = uploadDirectory.resolve(fileName).normalize();
            if (!target.startsWith(uploadDirectory)) {
                throw new BusinessException("Ten file anh khong hop le");
            }
            Files.copy(imageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String imagePath = "/uploads/products/" + fileName;
            product.setMainImagePath(imagePath);
            syncMainProductImage(product, imagePath);
        } catch (IOException ex) {
            throw new BusinessException("Khong the luu anh san pham");
        }
    }

    private void syncMainProductImage(Product product, String imagePath) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
        images.forEach(image -> image.setMain(false));
        productImageRepository.saveAll(images);

        ProductImage mainImage = images.stream()
            .filter(image -> imagePath.equals(image.getImagePath()))
            .findFirst()
            .orElseGet(ProductImage::new);

        mainImage.setProduct(product);
        mainImage.setImagePath(imagePath);
        mainImage.setDisplayOrder(1);
        mainImage.setMain(true);
        productImageRepository.save(mainImage);
    }

    private String extractImageExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException("Anh san pham phai co dinh dang jpg, jpeg, png hoac webp");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        if (!List.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
            throw new BusinessException("Chi chap nhan anh jpg, jpeg, png hoac webp");
        }
        return extension;
    }

    private String toSafeFileName(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "product" : normalized;
    }

    private Specification<Product> toSpecification(ProductFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isTrue(root.get("active")));
            predicates.add(builder.isTrue(root.get("category").get("active")));

            Join<Object, Object> brandJoin = root.join("brand", JoinType.LEFT);
            predicates.add(builder.or(
                builder.isNull(root.get("brand")),
                builder.isTrue(brandJoin.get("active"))
            ));

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
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
