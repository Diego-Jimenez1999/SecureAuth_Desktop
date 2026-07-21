package secureauth.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.application.dto.AppointmentDTO;
import secureauth.domain.sales.SaleItemType;
import secureauth.domain.services.ServiceOrderStatus;
import secureauth.domain.services.ServiceSummary;
import secureauth.model.Appointment;
import secureauth.model.Venta;
import secureauth.service.enterprise.SalesTransactionService;

class JdbcSalesRepositoryServiceOrderTest {

    @Test
    void passesServiceOrdersFromSaleItemsToTransactionalService() throws SQLException {
        CapturingSalesTransactionService service = new CapturingSalesTransactionService();
        JdbcSalesRepository repository = new JdbcSalesRepository(service);
        ServiceOrderDTO order = order();
        SaleDTO sale = new SaleDTO(null, java.time.LocalDateTime.now(), "Mostrador", 10000d, "Efectivo", "Sistema",
                List.of(new SaleItemDTO("Consulta", 10000d, 1, null, null, SaleItemType.SERVICE, "Veterinaria",
                        null, 1000d, 1, null, order)));

        repository.registerSale(sale, 1000d, 1900d, "1 x Consulta", List.of());

        assertEquals(1, service.orders.size());
        assertEquals("Consulta", service.orders.get(0).item().serviceName());
    }

    @Test
    void passesExactAppointmentDateAndTimeToTransactionalService() throws SQLException {
        CapturingSalesTransactionService service = new CapturingSalesTransactionService();
        JdbcSalesRepository repository = new JdbcSalesRepository(service);
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(15, 45);
        AppointmentDTO appointment = new AppointmentDTO(null, 1, "Consulta", 1, "Cliente", 2, "Mascota",
                date, time, "PENDIENTE", "", java.time.LocalDateTime.now(), "Sistema");
        SaleDTO sale = new SaleDTO(null, java.time.LocalDateTime.now(), "Mostrador", 10000d, "Efectivo", "Sistema",
                List.of(new SaleItemDTO("Consulta", 10000d, 1, null, null, SaleItemType.SERVICE, "Veterinaria",
                        null, 1000d, 1, appointment, null)));

        repository.registerSale(sale, 1000d, 1900d, "1 x Consulta", List.of(appointment));

        assertEquals(1, service.appointments.size());
        assertEquals(date, service.appointments.get(0).getAppointmentDate());
        assertEquals(time, service.appointments.get(0).getAppointmentTime());
    }

    private ServiceOrderDTO order() {
        ServiceOrderItemDTO item = new ServiceOrderItemDTO(1, "Consulta", "Vet", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), 60, "", 10000d);
        List<ServiceProductDTO> products = List.of(new ServiceProductDTO(10, "SKU-10", "Shampoo", 1, 12000d));
        return new ServiceOrderDTO(null, 1, "Cliente", 2, "Mascota", ServiceOrderStatus.SCHEDULED, item, products,
                List.of(), ServiceSummary.calculate(item.servicePrice(),
                        List.of(new secureauth.domain.services.ServiceProduct(10, "SKU-10", "Shampoo", 1, 12000d)),
                        0d));
    }

    private static final class CapturingSalesTransactionService extends SalesTransactionService {
        private List<ServiceOrderDTO> orders = List.of();
        private List<Appointment> appointments = List.of();

        @Override
        public void registrarVentaConCitas(Venta venta, double gain, double tax, String itemsSummary,
                List<Appointment> appointments, List<ServiceOrderDTO> serviceOrders) {
            this.appointments = appointments;
            this.orders = serviceOrders;
        }
    }
}
