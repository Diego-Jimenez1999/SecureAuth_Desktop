package secureauth.shared.error;

/**
 * Excepción lanzada cuando un usuario no posee el permiso requerido para realizar una acción o acceder a un módulo.
 */
public class AccessDeniedException extends RuntimeException {

    private final String requiredPermission;

    public AccessDeniedException(String message, String requiredPermission) {
        super(message);
        this.requiredPermission = requiredPermission;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}
