package secureauth.application.mapper;

import secureauth.application.dto.CustomerDTO;
import secureauth.model.Owner;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerDTO toDTO(Owner owner) {
        return new CustomerDTO(owner.getId(), owner.getNombreCompleto(), owner.getTelefono(), owner.getCorreo());
    }

    public static Owner toDomain(CustomerDTO dto) {
        return new Owner(dto.id() == null ? 0 : dto.id(), dto.fullName(), dto.phone(), dto.email(), null);
    }
}
