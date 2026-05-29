package secureauth.dao;

/**
 * Excepción personalizada para errores de acceso a datos de mascotas.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class PetDataAccessException extends RuntimeException {

    /**
     * Crea una excepción de acceso a datos con mensaje y causa original.
     *
     * @param message mensaje técnico/funcional del error
     * @param cause excepción raíz
     */
    public PetDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
