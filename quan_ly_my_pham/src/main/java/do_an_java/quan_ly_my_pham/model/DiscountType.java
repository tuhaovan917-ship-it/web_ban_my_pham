package do_an_java.quan_ly_my_pham.model;

public enum DiscountType {
    PERCENT("Theo phần trăm"),
    FIXED("Số tiền cố định");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
