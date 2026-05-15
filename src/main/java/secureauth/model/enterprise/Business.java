package secureauth.model.enterprise;

/** Entidad principal de negocio. */
public record Business(int id, int businessTypeId, String name, String nit, String address,
                       String phone, String logo, String primaryColor, String secondaryColor) { }
