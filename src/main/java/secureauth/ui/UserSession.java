package secureauth.ui;

/**
 * DTO de sesión para almacenar los datos del usuario autenticado.
 */
public record UserSession(
    int userId,
    int businessId,
    int branchId,
    int roleId,
    String roleName,
    String fullName,
    String email
) {
    @Override
    public String toString() {
        return "UserSession{user=" + email + ", role=" + roleName + ", biz=" + businessId + "}";
    }
}