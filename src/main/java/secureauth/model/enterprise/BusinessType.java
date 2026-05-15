package secureauth.model.enterprise;

/** Tipo de negocio disponible en la plataforma ERP multi-negocio. */
public record BusinessType(int id, String name, String description, String icon, String status) { }
