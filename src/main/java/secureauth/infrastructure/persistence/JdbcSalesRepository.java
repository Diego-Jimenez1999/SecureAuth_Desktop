package secureauth.infrastructure.persistence;

import java.sql.SQLException;
import java.util.List;

import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.mapper.AppointmentMapper;
import secureauth.application.mapper.SaleMapper;
import secureauth.infrastructure.repository.SalesRepository;
import secureauth.service.enterprise.SalesTransactionService;

public class JdbcSalesRepository implements SalesRepository {

    private final SalesTransactionService salesTransactionService;

    public JdbcSalesRepository() {
        this(new SalesTransactionService());
    }

    public JdbcSalesRepository(SalesTransactionService salesTransactionService) {
        this.salesTransactionService = salesTransactionService;
    }

    @Override
    public void registerSale(SaleDTO sale, double gain, double tax, String itemsSummary,
            List<AppointmentDTO> appointments) throws SQLException {
        salesTransactionService.registrarVentaConCitas(SaleMapper.toDomain(sale), gain, tax, itemsSummary,
                appointments.stream().map(AppointmentMapper::toDomain).toList(), serviceOrdersFrom(sale));
    }

    private List<ServiceOrderDTO> serviceOrdersFrom(SaleDTO sale) {
        return sale.items().stream()
                .map(SaleItemDTO::serviceOrder)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
