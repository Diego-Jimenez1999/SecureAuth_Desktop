package secureauth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import secureauth.application.command.RegisterSaleCommand;
import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.domain.sales.SaleItemType;
import secureauth.infrastructure.repository.SalesRepository;

class RegisterSaleUseCaseTest {

    @Test
    void calculatesRegistrationMetricsBeforePersisting() throws SQLException {
        CapturingRepository repository = new CapturingRepository();
        RegisterSaleUseCase useCase = new RegisterSaleUseCase(repository);
        SaleDTO sale = new SaleDTO(null, LocalDateTime.now(), "Mostrador", 23800d, "Efectivo", "Sistema",
                List.of(new SaleItemDTO("Shampoo", 10000d, 1, 9, null, SaleItemType.PRODUCT, "Inventario", null,
                        4000d, 2)));

        SaleRegistrationResult result = useCase.register(new RegisterSaleCommand(sale, List.of()));

        assertEquals(8000d, result.gain());
        assertEquals("2 x Shampoo", result.itemsSummary());
        assertTrue(repository.called);
        assertEquals(3800d, repository.tax);
    }

    @Test
    void detectsAppointmentRequirementFromExplicitType() {
        RegisterSaleUseCase useCase = new RegisterSaleUseCase((sale, gain, tax, itemsSummary, appointments) -> { });

        assertTrue(useCase.requiresAppointment(new SaleItemDTO("Consulta", 35000d, 1, null, null,
                SaleItemType.SERVICE, "Veterinaria", null, 10000d, 1, appointment())));
        org.junit.jupiter.api.Assertions.assertFalse(useCase.requiresAppointment(new SaleItemDTO("Shampoo", 12000d,
                2, 9, "SKU-9", SaleItemType.PRODUCT, "Inventario", 5, 4000d, 1)));
    }

    @Test
    void preservesServiceAppointmentAtTenThirtyThroughSaleItemAndUseCase() throws SQLException {
        assertAppointmentTimePreserved(LocalDate.now().plusDays(1), LocalTime.of(10, 30));
    }

    @Test
    void preservesServiceAppointmentAtFifteenFortyFiveThroughSaleItemAndUseCase() throws SQLException {
        assertAppointmentTimePreserved(LocalDate.now().plusDays(1), LocalTime.of(15, 45));
    }

    @Test
    void preservesServiceAppointmentTomorrowAtNineThroughSaleItemAndUseCase() throws SQLException {
        assertAppointmentTimePreserved(LocalDate.now().plusDays(1), LocalTime.of(9, 0));
    }

    private static final class CapturingRepository implements SalesRepository {
        private boolean called;
        private double tax;
        private List<AppointmentDTO> appointments = List.of();

        @Override
        public void registerSale(SaleDTO sale, double gain, double tax, String itemsSummary,
                List<AppointmentDTO> appointments) {
            this.called = true;
            this.tax = tax;
            this.appointments = new ArrayList<>(appointments);
        }
    }

    private void assertAppointmentTimePreserved(LocalDate date, LocalTime time) throws SQLException {
        CapturingRepository repository = new CapturingRepository();
        RegisterSaleUseCase useCase = new RegisterSaleUseCase(repository);
        AppointmentDTO appointment = appointment(date, time);
        SaleItemDTO item = new SaleItemDTO("Consulta", 35000d, 1, null, null, SaleItemType.SERVICE,
                "Veterinaria", null, 10000d, 1).withAppointment(appointment);
        SaleDTO sale = new SaleDTO(null, LocalDateTime.now(), "Mostrador", 41650d, "Efectivo", "Sistema",
                List.of(item));

        assertEquals(time, item.appointment().appointmentTime());

        useCase.register(new RegisterSaleCommand(sale, List.of()));

        assertTrue(repository.called);
        assertEquals(1, repository.appointments.size());
        assertEquals(date, repository.appointments.get(0).appointmentDate());
        assertEquals(time, repository.appointments.get(0).appointmentTime());
    }

    private static AppointmentDTO appointment() {
        return appointment(java.time.LocalDate.now().plusDays(1), java.time.LocalTime.of(10, 0));
    }

    private static AppointmentDTO appointment(LocalDate date, LocalTime time) {
        return new AppointmentDTO(null, 1, "Consulta", 1, "Cliente", 1, "Mascota",
                date, time, "PENDIENTE", "", LocalDateTime.now(), "Sistema");
    }
}
