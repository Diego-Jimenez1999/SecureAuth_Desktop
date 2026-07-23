package secureauth.application.usecase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import secureauth.application.command.RegisterSaleCommand;
import secureauth.application.dto.SaleItemDTO;
import secureauth.application.validation.AppointmentValidator;
import secureauth.application.validation.SaleValidator;
import secureauth.infrastructure.repository.SalesRepository;
import secureauth.shared.events.EventPublisher;
import secureauth.shared.events.NoOpEventPublisher;
import secureauth.shared.events.SaleRegisteredEvent;

public class RegisterSaleUseCase {

    private final SalesRepository repository;
    private final SaleValidator saleValidator;
    private final AppointmentValidator appointmentValidator;
    private final EventPublisher eventPublisher;

    public RegisterSaleUseCase(SalesRepository repository) {
        this(repository, new SaleValidator(), new AppointmentValidator(), new NoOpEventPublisher());
    }

    public RegisterSaleUseCase(SalesRepository repository, SaleValidator saleValidator,
            AppointmentValidator appointmentValidator, EventPublisher eventPublisher) {
        this.repository = repository;
        this.saleValidator = saleValidator;
        this.appointmentValidator = appointmentValidator;
        this.eventPublisher = eventPublisher;
    }

    public SaleRegistrationResult register(RegisterSaleCommand command) throws SQLException {
        secureauth.service.AuthorizationService.getInstance().verifyPermission("MODULO_VENTAS");

        saleValidator.validate(command.sale());
        List<secureauth.application.dto.AppointmentDTO> appointments = appointmentsFrom(command);
        appointments.forEach(appointmentValidator::validate);
        double gain = command.sale().items().stream()
                .mapToDouble(item -> item.gainPerUnit() * item.quantity())
                .sum();
        String itemsSummary = buildItemsSummary(command.sale().items());
        repository.registerSale(command.sale(), gain, command.sale().total() * SalesCartUseCase.TAX_RATE
                / (1d + SalesCartUseCase.TAX_RATE), itemsSummary, appointments);
        eventPublisher.publish(new SaleRegisteredEvent(command.sale().date(), command.sale().total(),
                command.sale().items().size(), hasInventoryItems(command), !appointments.isEmpty()));

        // Reproducir sonido de venta confirmada
        secureauth.service.SoundService.getInstance().playSound(secureauth.service.SoundService.SoundEvent.VENTA);

        return new SaleRegistrationResult(gain, command.sale().total() * SalesCartUseCase.TAX_RATE
                / (1d + SalesCartUseCase.TAX_RATE), itemsSummary);
    }

    public boolean requiresAppointment(SaleItemDTO item) {
        return item != null && item.requiresAppointment();
    }

    public String buildItemsSummary(java.util.List<SaleItemDTO> items) {
        return items.stream()
                .map(item -> item.quantity() + " x " + item.name())
                .collect(Collectors.joining(", "));
    }

    private boolean hasInventoryItems(RegisterSaleCommand command) {
        return command.sale().items().stream().anyMatch(SaleItemDTO::inventoryBacked);
    }

    private List<secureauth.application.dto.AppointmentDTO> appointmentsFrom(RegisterSaleCommand command) {
        List<secureauth.application.dto.AppointmentDTO> appointments = new ArrayList<>(command.appointments());
        for (SaleItemDTO item : command.sale().items()) {
            if (item.appointment() != null && !appointments.contains(item.appointment())) {
                appointments.add(item.appointment());
            }
        }
        return appointments;
    }
}
