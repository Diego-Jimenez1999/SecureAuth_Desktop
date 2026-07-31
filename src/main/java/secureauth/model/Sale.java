package secureauth.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de dominio para una venta completa.
 *
 * <p>Contiene los datos de cabecera solicitados por el módulo POS y las líneas
 * del carrito. El servicio de ventas lo usa como unidad transaccional para
 * guardar venta, detalle, descuento de inventario y actividad reciente.</p>
 *
 * <h2>Ejemplo</h2>
 * <pre>{@code
 * Sale sale = new Sale(null, LocalDateTime.now(), "Mostrador", 53550, "Efectivo", "admin");
 * sale.addItem(new SaleItem("Juguete", 15000));
 * }</pre>
 */
public class Sale {

    private Integer id;
    private final LocalDateTime date;
    private final String customerName;
    private final double total;
    private final String paymentMethod;
    private final String sellerName;
    private final List<SaleItem> items;

    /**
     * Crea una venta de cabecera.
     *
     * @param id identificador de base de datos, si ya existe
     * @param date fecha de registro
     * @param customerName nombre del cliente
     * @param total total final de la venta
     * @param paymentMethod método de pago
     * @param sellerName usuario que atiende la venta
     */
    public Sale(Integer id, LocalDateTime date, String customerName, double total, String paymentMethod,
            String sellerName) {
        this.id = id;
        this.date = date;
        this.customerName = customerName;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.sellerName = sellerName;
        this.items = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotal() {
        return total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void addItem(SaleItem item) {
        items.add(item);
    }

    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
