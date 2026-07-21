package secureauth.infrastructure.repository;

import java.sql.SQLException;
import java.util.List;

import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleDTO;

public interface SalesRepository {
    void registerSale(SaleDTO sale, double gain, double tax, String itemsSummary, List<AppointmentDTO> appointments)
            throws SQLException;
}
