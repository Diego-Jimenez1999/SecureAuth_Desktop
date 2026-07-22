package secureauth.shared.error;

/**
 * Excepción personalizada de negocio para denegar transacciones no autorizadas.
 *
 * <p>Se lanza automáticamente cuando un usuario intenta realizar una operación
 * para la cual su rol actual no cuenta con los permisos necesarios.</p>
 *
 * @author Jules
 * @version 1.0
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje de error especificado.
     *
     * @param message mensaje descriptivo del error de acceso
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
