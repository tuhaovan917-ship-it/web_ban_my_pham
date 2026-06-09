package do_an_java.quan_ly_my_pham.service.dto;

import java.math.BigDecimal;

public record ProductFilter(
    String keyword,
    Integer categoryId,
    Integer brandId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean featured,
    String sort
) {
}
