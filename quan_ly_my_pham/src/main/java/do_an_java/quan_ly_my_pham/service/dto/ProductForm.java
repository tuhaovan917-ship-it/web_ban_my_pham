package do_an_java.quan_ly_my_pham.service.dto;

import java.math.BigDecimal;

public record ProductForm(
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
}
