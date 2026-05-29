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
 * Venta venta = new Venta(null, LocalDateTime.now(), "Mostrador", 53550, "Efectivo", "admin");
 * venta.addItem(new SaleItem("Juguete", 15000));
 * }</pre>
 */
public class Venta {

    private Integer idVenta;
    private final LocalDateTime fecha;
    private final String cliente;
    private final double total;
    private final String metodoPago;
    private final String usuarioVendedor;
    private final List<SaleItem> items;

    /**
     * Crea una venta de cabecera.
     *
     * @param idVenta identificador de base de datos, si ya existe
     * @param fecha fecha de registro
     * @param cliente nombre del cliente
     * @param total total final de la venta
     * @param metodoPago método de pago
     * @param usuarioVendedor usuario que atiende la venta
     */
    public Venta(Integer idVenta, LocalDateTime fecha, String cliente, double total, String metodoPago,
            String usuarioVendedor) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.cliente = cliente;
        this.total = total;
        this.metodoPago = metodoPago;
        this.usuarioVendedor = usuarioVendedor;
        this.items = new ArrayList<>();
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public double getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getUsuarioVendedor() {
        return usuarioVendedor;
    }

    public void addItem(SaleItem item) {
        items.add(item);
    }

    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
