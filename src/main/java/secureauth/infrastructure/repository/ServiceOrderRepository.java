package secureauth.infrastructure.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import secureauth.application.dto.ServiceOrderDTO;

public interface ServiceOrderRepository {
    void registerWithInventoryConsumption(Connection conn, int businessId, int branchId, int saleId,
            List<ServiceOrderDTO> orders, String userName) throws SQLException;
}
