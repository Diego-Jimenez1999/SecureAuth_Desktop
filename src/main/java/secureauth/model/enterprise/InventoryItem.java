package secureauth.model.enterprise;

/** Item de inventario aislado por negocio y sucursal. */
public record InventoryItem(int id, int businessId, int branchId, String sku, String name, String category,
                            int stock, int minStock, String supplier, double cost, double price, String status) { }
