package secureauth.model;

/**
 * Modelo de dominio para un servicio vendible del módulo de ventas.
 */
public class SaleItem {

    private final String name;
    private final double price;

    /**
     * Crea un item de venta.
     *
     * @param name nombre del servicio
     * @param price precio unitario del servicio
     */
    public SaleItem(String name, double price) {
        this.name = name;
        this.price = price;
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

    @Override
    public String toString() {
        return name + " - $" + String.format("%,.0f", price);
    }
}
