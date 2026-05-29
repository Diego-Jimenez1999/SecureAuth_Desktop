package secureauth.model;

/**
 * Detalle persistible de una venta.
 *
 * <p>Representa una fila de {@code detalle_venta}: producto vendido,
 * cantidad, precio unitario y subtotal. Se construye desde el carrito antes de
 * guardar la transacción JDBC.</p>
 *
 * @param idDetalle identificador del detalle
 * @param idVenta identificador de la venta padre
 * @param idProducto producto de inventario asociado; puede ser {@code null} si es servicio
 * @param cantidad cantidad vendida
 * @param precioUnitario precio unitario aplicado
 * @param subtotal total de la línea
 */
public record DetalleVenta(Integer idDetalle, Integer idVenta, Integer idProducto, int cantidad,
                           double precioUnitario, double subtotal) {
}
