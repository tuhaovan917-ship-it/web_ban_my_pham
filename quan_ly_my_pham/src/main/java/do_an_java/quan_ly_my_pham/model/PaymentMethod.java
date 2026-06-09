package do_an_java.quan_ly_my_pham.model;

public enum PaymentMethod {
    COD("Trả tiền khi nhận hàng"),
    CARD("Thanh toán bằng thẻ"),
    EWALLET("Ví điện tử / QR");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
