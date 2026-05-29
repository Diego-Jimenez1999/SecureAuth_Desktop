package secureauth.model;

/**
 * Modelo de dominio para un producto o servicio vendible del módulo de ventas.
 *
 * <p>El objeto mantiene precio unitario, cantidad y metadatos de inventario para
 * permitir ventas con múltiples productos y descuento automático de stock.</p>
 *
 * <h2>Ejemplo</h2>
 * <pre>{@code
 * SaleItem shampoo = new SaleItem("Shampoo", 18000, 10, 4, "SKU-4", "Producto", "Aseo", 12);
 * shampoo.setQuantity(2);
 * double subtotal = shampoo.getSubtotal();
 * }</pre>
 */
public class SaleItem {

    private final String name;
    private final double price;
    private final int catalogItemId;
    private final Integer inventoryItemId;
    private final String sku;
    private final String type;
    private final String category;
    private final Integer stockAvailable;
    private int quantity;

    /**
     * Crea un item de venta.
     *
     * @param name nombre del servicio
     * @param price precio unitario del servicio
     */
    public SaleItem(String name, double price) {
        this(name, price, 0, null, null, null, null, null);
    }

    public SaleItem(String name, double price, int catalogItemId, Integer inventoryItemId,
            String sku, String type, String category) {
        this(name, price, catalogItemId, inventoryItemId, sku, type, category, null);
    }

    /**
     * Crea un item de venta con metadatos completos.
     *
     * @param name nombre visible del producto o servicio
     * @param price precio unitario
     * @param catalogItemId identificador del catálogo de ventas
     * @param inventoryItemId identificador de inventario, si aplica
     * @param sku código SKU del inventario, si aplica
     * @param type tipo comercial: Producto o Servicio
     * @param category categoría comercial
     * @param stockAvailable stock visual disponible para productos inventariados
     */
    public SaleItem(String name, double price, int catalogItemId, Integer inventoryItemId,
            String sku, String type, String category, Integer stockAvailable) {
        this.name = name;
        this.price = price;
        this.catalogItemId = catalogItemId;
        this.inventoryItemId = inventoryItemId;
        this.sku = sku;
        this.type = type;
        this.category = category;
        this.stockAvailable = stockAvailable;
        this.quantity = 1;
    }

    /**
     * @return nombre del servicio
     */
    public String getName() {
        return name;
    }

    /**
     * @return precio del servicio
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return cantidad seleccionada en el carrito
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Actualiza la cantidad del item.
     *
     * @param quantity nueva cantidad; debe ser mayor a cero
     * @throws IllegalArgumentException si la cantidad es cero, negativa o excede el stock visible
     */
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (stockAvailable != null && quantity > stockAvailable) {
            throw new IllegalArgumentException("No hay suficiente inventario disponible.");
        }
        this.quantity = quantity;
    }

    /**
     * Incrementa la cantidad en una unidad respetando el stock visible.
     */
    public void incrementQuantity() {
        setQuantity(quantity + 1);
    }

    /**
     * Disminuye la cantidad en una unidad sin permitir valores menores a uno.
     */
    public void decrementQuantity() {
        if (quantity > 1) {
            setQuantity(quantity - 1);
        }
    }

    /**
     * @return subtotal calculado como precio unitario por cantidad
     */
    public double getSubtotal() {
        return price * quantity;
    }

    public int getCatalogItemId() {
        return catalogItemId;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public String getSku() {
        return sku;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public Integer getStockAvailable() {
        return stockAvailable;
    }

    /**
     * @return true si el item pertenece a inventario físico
     */
    public boolean isInventoryBacked() {
        return inventoryItemId != null;
    }

    @Override
    public String toString() {
        return quantity + " x " + name + " - $" + String.format("%,.0f", getSubtotal());
    }
}
