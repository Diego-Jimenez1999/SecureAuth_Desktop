package secureauth.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.application.usecase.ValidateServiceOrderInventoryUseCase;
import secureauth.application.usecase.ValidateServiceOrderUseCase;
import secureauth.dao.enterprise.RecentActivityDAO;
import secureauth.dao.enterprise.InventoryDAO;
import secureauth.infrastructure.repository.ServiceOrderRepository;

public class JdbcServiceOrderRepository implements ServiceOrderRepository {

    private final InventoryDAO inventoryDAO;
    private final RecentActivityDAO actividadDAO;
    private final ValidateServiceOrderUseCase serviceOrderValidator;
    private final ValidateServiceOrderInventoryUseCase inventoryValidator;

    public JdbcServiceOrderRepository() {
        this(new InventoryDAO(), new RecentActivityDAO(), new ValidateServiceOrderUseCase(),
                new ValidateServiceOrderInventoryUseCase());
    }

    public JdbcServiceOrderRepository(InventoryDAO inventoryDAO, RecentActivityDAO actividadDAO,
            ValidateServiceOrderUseCase serviceOrderValidator,
            ValidateServiceOrderInventoryUseCase inventoryValidator) {
        this.inventoryDAO = inventoryDAO;
        this.actividadDAO = actividadDAO;
        this.serviceOrderValidator = serviceOrderValidator;
        this.inventoryValidator = inventoryValidator;
    }

    @Override
    public void registerWithInventoryConsumption(Connection conn, int businessId, int branchId, int saleId,
            List<ServiceOrderDTO> orders, String userName) throws SQLException {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        inventoryValidator.validate(orders);
        for (ServiceOrderDTO order : orders) {
            serviceOrderValidator.validate(order);
            actividadDAO.insert(conn, "Orden de servicio registrada #" + saleId + " - " + order.item().serviceName(),
                    "ORDEN_SERVICIO", userName);
            for (ServiceProductDTO product : order.products()) {
                InventoryDAO.InventoryConsumptionSource source = inventoryDAO.findConsumptionSourceForUpdate(conn,
                        businessId, branchId, product.productId());
                if (source == null) {
                    throw new SQLException("Producto de inventario no existe: " + product.name());
                }
                if (!source.active()) {
                    throw new SQLException("Producto de inventario inactivo: " + product.name());
                }
                if (source.stock() < product.quantity()) {
                    throw new SQLException("Stock insuficiente para " + product.name() + ".");
                }
                inventoryDAO.decreaseStock(conn, businessId, branchId, product.productId(), product.quantity());
                actividadDAO.insert(conn,
                        "Consumo orden #" + saleId + ": " + product.quantity() + " x " + source.name(),
                        "CONSUMO_INVENTARIO", userName);
            }
        }
    }
}
