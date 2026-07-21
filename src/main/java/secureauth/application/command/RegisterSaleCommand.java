package secureauth.application.command;

import java.util.List;

import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleDTO;

public record RegisterSaleCommand(SaleDTO sale, List<AppointmentDTO> appointments) {
    public RegisterSaleCommand {
        appointments = List.copyOf(appointments == null ? List.of() : appointments);
    }
}
