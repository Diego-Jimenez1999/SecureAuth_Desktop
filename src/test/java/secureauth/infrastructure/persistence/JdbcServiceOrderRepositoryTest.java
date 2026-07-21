package secureauth.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.application.usecase.ValidateServiceOrderInventoryUseCase;
import secureauth.application.usecase.ValidateServiceOrderUseCase;
import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.dao.enterprise.InventoryDAO;
import secureauth.domain.services.ServiceOrderStatus;
import secureauth.domain.services.ServiceSummary;

class JdbcServiceOrderRepositoryTest {

    @Test
    void consumesInventoryAndRegistersHistoryActivity() throws SQLException {
        FakeInventoryDAO inventoryDAO = new FakeInventoryDAO(5, true);
        FakeActividadDAO actividadDAO = new FakeActividadDAO();
        JdbcServiceOrderRepository repository = new JdbcServiceOrderRepository(inventoryDAO, actividadDAO,
                new ValidateServiceOrderUseCase(), new ValidateServiceOrderInventoryUseCase());

        repository.registerWithInventoryConsumption(null, 1, 1, 77, List.of(order(2)), "Sistema");

        assertEquals(2, inventoryDAO.decreasedQuantity);
        assertEquals(2, actividadDAO.inserted);
    }

    @Test
    void rejectsInsufficientStockBeforeDecreasingInventory() {
        FakeInventoryDAO inventoryDAO = new FakeInventoryDAO(1, true);
        JdbcServiceOrderRepository repository = new JdbcServiceOrderRepository(inventoryDAO, new FakeActividadDAO(),
                new ValidateServiceOrderUseCase(), new ValidateServiceOrderInventoryUseCase());

        assertThrows(SQLException.class,
                () -> repository.registerWithInventoryConsumption(null, 1, 1, 77, List.of(order(2)), "Sistema"));
        assertEquals(0, inventoryDAO.decreasedQuantity);
    }

    @Test
    void rejectsInactiveProducts() {
        FakeInventoryDAO inventoryDAO = new FakeInventoryDAO(5, false);
        JdbcServiceOrderRepository repository = new JdbcServiceOrderRepository(inventoryDAO, new FakeActividadDAO(),
                new ValidateServiceOrderUseCase(), new ValidateServiceOrderInventoryUseCase());

        assertThrows(SQLException.class,
                () -> repository.registerWithInventoryConsumption(null, 1, 1, 77, List.of(order(1)), "Sistema"));
        assertEquals(0, inventoryDAO.decreasedQuantity);
    }

    private ServiceOrderDTO order(int quantity) {
        ServiceOrderItemDTO item = new ServiceOrderItemDTO(1, "Consulta", "Vet", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), 60, "", 10000d);
        List<ServiceProductDTO> products = List.of(new ServiceProductDTO(10, "SKU-10", "Shampoo", quantity, 12000d));
        return new ServiceOrderDTO(null, 1, "Cliente", 2, "Mascota", ServiceOrderStatus.SCHEDULED, item, products,
                List.of(), ServiceSummary.calculate(item.servicePrice(),
                        List.of(new secureauth.domain.services.ServiceProduct(10, "SKU-10", "Shampoo", quantity,
                                12000d)),
                        0d));
    }

    private static final class FakeInventoryDAO extends InventoryDAO {
        private final int stock;
        private final boolean active;
        private int decreasedQuantity;

        private FakeInventoryDAO(int stock, boolean active) {
            this.stock = stock;
            this.active = active;
        }

        @Override
        public InventoryConsumptionSource findConsumptionSourceForUpdate(Connection conn, int businessId, int branchId,
                int inventoryId) {
            return new InventoryConsumptionSource(inventoryId, "SKU-10", "Shampoo", stock, 5000d, 12000d,
                    active ? "ACTIVO" : "INACTIVO", active);
        }

        @Override
        public void decreaseStock(Connection conn, int businessId, int branchId, int inventoryId, int quantity) {
            decreasedQuantity += quantity;
        }
    }

    private static final class FakeActividadDAO extends ActividadRecienteDAO {
        private int inserted;

        @Override
        public void insert(Connection conn, String descripcion, String tipo, String usuario) {
            inserted++;
        }
    }
}
