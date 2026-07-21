package secureauth.service.enterprise;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import secureauth.config.DatabaseConnection;
import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.dao.enterprise.SalesTransactionDAO;
import secureauth.dao.enterprise.SalesTransactionDAO.SaleReportRow;
import secureauth.dao.enterprise.InventoryDAO;
import secureauth.application.dto.ServiceOrderDTO;
import secureauth.infrastructure.persistence.JdbcServiceOrderRepository;
import secureauth.infrastructure.repository.ServiceOrderRepository;
import secureauth.model.Appointment;
import secureauth.model.SaleItem;
import secureauth.model.Venta;
import secureauth.shared.events.EventPublisher;
import secureauth.shared.events.InventoryConsumptionEvent;
import secureauth.shared.events.NoOpEventPublisher;
import secureauth.shared.events.ServiceOrderRegisteredEvent;

/**
 * Servicio de ventas POS y métricas de dashboard por sucursal.
 *
 * <p>La operación principal registra venta, detalle, descuento de inventario y
 * actividad reciente dentro de una única transacción JDBC para evitar
 * inconsistencias si ocurre un error intermedio.</p>
 */
public class SalesTransactionService {

    private final SalesTransactionDAO dao;
    private final InventoryDAO inventoryDAO;
    private final ActividadRecienteDAO actividadDAO;
    private final AppointmentDAO appointmentDAO;
    private final ServiceOrderRepository serviceOrderRepository;
    private final EventPublisher eventPublisher;
    private final EnterpriseContext context = EnterpriseContext.getInstance();

    public SalesTransactionService() {
        this(new SalesTransactionDAO(), new InventoryDAO(), new ActividadRecienteDAO(), new AppointmentDAO(),
                new JdbcServiceOrderRepository());
    }

    public SalesTransactionService(SalesTransactionDAO dao, InventoryDAO inventoryDAO,
            ActividadRecienteDAO actividadDAO) {
        this(dao, inventoryDAO, actividadDAO, new AppointmentDAO(), new JdbcServiceOrderRepository());
    }

    public SalesTransactionService(SalesTransactionDAO dao, InventoryDAO inventoryDAO,
            ActividadRecienteDAO actividadDAO, AppointmentDAO appointmentDAO) {
        this(dao, inventoryDAO, actividadDAO, appointmentDAO, new JdbcServiceOrderRepository());
    }

    public SalesTransactionService(SalesTransactionDAO dao, InventoryDAO inventoryDAO,
            ActividadRecienteDAO actividadDAO, AppointmentDAO appointmentDAO,
            ServiceOrderRepository serviceOrderRepository) {
        this(dao, inventoryDAO, actividadDAO, appointmentDAO, serviceOrderRepository, new NoOpEventPublisher());
    }

    public SalesTransactionService(SalesTransactionDAO dao, InventoryDAO inventoryDAO,
            ActividadRecienteDAO actividadDAO, AppointmentDAO appointmentDAO,
            ServiceOrderRepository serviceOrderRepository, EventPublisher eventPublisher) {
        this.dao = dao;
        this.inventoryDAO = inventoryDAO;
        this.actividadDAO = actividadDAO;
        this.appointmentDAO = appointmentDAO;
        this.serviceOrderRepository = serviceOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
        inventoryDAO.ensureSchema();
        actividadDAO.ensureSchema();
        appointmentDAO.ensureSchema();
    }

    public void registerSale(double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        dao.insertTx(context.getActiveBusinessId(), context.getActiveBranchId(), total, gain, tax, items, paymentMethod);
    }

    public void registerSale(double total, double gain, double tax, int items, String paymentMethod,
            String itemsSummary, String clientName, String userName) throws SQLException {
        dao.insertTx(context.getActiveBusinessId(), context.getActiveBranchId(), total, gain, tax, items, paymentMethod,
                itemsSummary, clientName, userName);
    }

    public void registerSaleWithInventory(List<SaleItem> saleItems, double total, double gain, double tax,
            String paymentMethod, String itemsSummary, String clientName, String userName) throws SQLException {
        Venta venta = new Venta(null, LocalDateTime.now(), clientName, total, paymentMethod, userName);
        for (SaleItem item : saleItems) {
            venta.addItem(item);
        }
        registrarVenta(venta, gain, tax, itemsSummary);
    }

    /**
     * Registra una venta y descuenta automáticamente el inventario.
     *
     * <p>Flujo transaccional:
     * valida carrito y cantidades, bloquea filas de inventario, guarda cabecera
     * en {@code ventas}, guarda {@code detalle_venta}, descuenta stock, registra
     * la transacción de reportes y publica actividad reciente. Si cualquier paso
     * falla ejecuta {@code rollback()}.</p>
     *
     * @param venta objeto venta con productos asociados
     * @throws SQLException si ocurre un error durante la transacción
     */
    public void registrarVenta(Venta venta) throws SQLException {
        registrarVenta(venta, 0d, venta.getTotal() * 0.19d / 1.19d, buildItemsSummary(venta.getItems()));
    }

    /**
     * Registra una venta con métricas POS calculadas por la vista.
     *
     * @param venta venta validada
     * @param gain ganancia calculada
     * @param tax impuesto calculado
     * @param itemsSummary resumen de items
     * @throws SQLException si ocurre un error JDBC
     */
    public void registrarVenta(Venta venta, double gain, double tax, String itemsSummary) throws SQLException {
        registrarVentaConCitas(venta, gain, tax, itemsSummary, List.of());
    }

    /**
     * Registra venta, movimiento de pago/reportes, inventario y citas de
     * servicios en una sola transacción.
     *
     * @param venta venta validada
     * @param gain ganancia calculada
     * @param tax impuesto calculado
     * @param itemsSummary resumen de items
     * @param appointments citas asociadas a servicios vendidos
     * @throws SQLException si cualquier paso falla
     */
    public void registrarVentaConCitas(Venta venta, double gain, double tax, String itemsSummary,
            List<Appointment> appointments) throws SQLException {
        registrarVentaConCitas(venta, gain, tax, itemsSummary, appointments, List.of());
    }

    public void registrarVentaConCitas(Venta venta, double gain, double tax, String itemsSummary,
            List<Appointment> appointments, List<ServiceOrderDTO> serviceOrders) throws SQLException {
        validateSale(venta);
        validateAppointments(appointments);
        int businessId = context.getActiveBusinessId();
        int branchId = context.getActiveBranchId();
        int serviceOrderCount = serviceOrders == null ? 0 : serviceOrders.size();
        int consumedProductLines = serviceOrders == null ? 0 : serviceOrders.stream()
                .mapToInt(order -> order.products().size())
                .sum();
        int consumedUnits = serviceOrders == null ? 0 : serviceOrders.stream()
                .flatMap(order -> order.products().stream())
                .mapToInt(secureauth.application.dto.ServiceProductDTO::quantity)
                .sum();

        initializeSchema();
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Map<Integer, Integer> quantities = inventoryQuantities(venta.getItems());
                for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                    boolean available = inventoryDAO.hasStockForUpdate(conn, businessId, branchId, entry.getKey(),
                            entry.getValue());
                    if (!available) {
                        throw new SQLException("No hay suficiente inventario disponible.");
                    }
                }

                int saleId = dao.insertVenta(conn, venta);
                venta.setIdVenta(saleId);
                dao.insertDetalles(conn, saleId, venta.getItems());

                serviceOrderRepository.registerWithInventoryConsumption(conn, businessId, branchId, saleId,
                        serviceOrders, venta.getUsuarioVendedor());

                for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                    inventoryDAO.decreaseStock(conn, businessId, branchId, entry.getKey(), entry.getValue());
                }

                int units = venta.getItems().stream().mapToInt(SaleItem::getQuantity).sum();
                dao.insertTx(conn, businessId, branchId, venta.getTotal(), gain, tax, units, venta.getMetodoPago(),
                        itemsSummary, venta.getCliente(), venta.getUsuarioVendedor());
                actividadDAO.insert(conn, "Venta registrada #" + saleId, "VENTA", venta.getUsuarioVendedor());
                if (!quantities.isEmpty()) {
                    actividadDAO.insert(conn, "Inventario actualizado", "INVENTARIO", venta.getUsuarioVendedor());
                }
                for (Appointment appointment : appointments) {
                    appointmentDAO.insert(conn, appointment);
                    actividadDAO.insert(conn, "Cita agendada para " + appointment.getPetName(), "CITA",
                            appointment.getCreatedBy());
                }
                conn.commit();
                if (!appointments.isEmpty()) {
                    AppointmentService.notifyAppointmentsChanged();
                }
                if (serviceOrderCount > 0) {
                    eventPublisher.publish(new ServiceOrderRegisteredEvent(LocalDateTime.now(), serviceOrderCount,
                            consumedProductLines));
                }
                if (consumedProductLines > 0) {
                    eventPublisher.publish(new InventoryConsumptionEvent(LocalDateTime.now(), consumedProductLines,
                            consumedUnits));
                }
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private Map<Integer, Integer> inventoryQuantities(List<SaleItem> saleItems) {
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (SaleItem item : saleItems) {
            if (item.getInventoryItemId() != null) {
                quantities.merge(item.getInventoryItemId(), item.getQuantity(), Integer::sum);
            }
        }
        return quantities;
    }

    private void validateSale(Venta venta) throws SQLException {
        if (venta == null || venta.getItems().isEmpty()) {
            throw new SQLException("No se permiten ventas vacías.");
        }
        for (SaleItem item : venta.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new SQLException("No se permiten cantidades negativas o en cero.");
            }
            if (item.isInventoryBacked() && item.getInventoryItemId() <= 0) {
                throw new SQLException("Producto de inventario inválido.");
            }
        }
    }

    private void validateAppointments(List<Appointment> appointments) throws SQLException {
        if (appointments == null) {
            return;
        }
        for (Appointment appointment : appointments) {
            if (appointment == null || appointment.getOwnerId() <= 0 || appointment.getPetId() <= 0
                    || appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null
                    || appointment.getServiceName() == null || appointment.getServiceName().trim().isEmpty()) {
                throw new SQLException("La cita del servicio está incompleta.");
            }
        }
    }

    private String buildItemsSummary(List<SaleItem> items) {
        return items.stream()
                .map(item -> item.getQuantity() + " x " + item.getName())
                .collect(Collectors.joining(", "));
    }

    public DashboardStats loadStats() throws SQLException {
        int businessId = context.getActiveBusinessId();
        int branchId = context.getActiveBranchId();
        return new DashboardStats(
                dao.salesToday(businessId, branchId),
                dao.salesMonth(businessId, branchId),
                dao.gainMonth(businessId, branchId),
                dao.itemsMonth(businessId, branchId)
        );
    }

    public record DashboardStats(double salesToday, double salesMonth, double gainMonth, int itemsMonth) { }

    public List<SaleReportRow> recentSales(int limit) throws SQLException {
        return dao.recentSales(context.getActiveBusinessId(), context.getActiveBranchId(), limit);
    }
}
