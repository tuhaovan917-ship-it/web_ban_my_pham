package do_an_java.quan_ly_my_pham.service.dto;

import do_an_java.quan_ly_my_pham.model.PaymentMethod;

import java.math.BigDecimal;

public record CheckoutRequest(
    Integer userId,
    String receiverName,
    String receiverPhone,
    String shippingAddress,
    PaymentMethod paymentMethod,
    String promotionCode,
    BigDecimal shippingFee,
    String note
) {
}
