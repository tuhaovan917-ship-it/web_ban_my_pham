package do_an_java.quan_ly_my_pham.model;

public enum OrderStatus {
    PAYMENT_IN_PROGRESS("Đang xử lý thanh toán", "badge-pending"),
    PENDING_CONFIRMATION("Chờ xác nhận", "badge-pending"),
    CONFIRMED("Đã xác nhận", "badge-confirmed"),
    SHIPPING("Đang giao hàng", "badge-shipping"),
    COMPLETED("Đã hoàn thành", "badge-completed"),
    CANCELLED("Đã hủy", "badge-cancelled");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
