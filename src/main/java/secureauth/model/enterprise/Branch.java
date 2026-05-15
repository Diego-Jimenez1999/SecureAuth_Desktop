package secureauth.model.enterprise;

/** Sucursal asociada a un negocio. */
public record Branch(int id, int businessId, String branchName, String address, String phone, String status) { }
