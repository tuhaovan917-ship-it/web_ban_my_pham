package do_an_java.quan_ly_my_pham.model;

public enum UserRole {
    ADMIN("Quản trị viên"),
    CUSTOMER("Khách hàng");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
