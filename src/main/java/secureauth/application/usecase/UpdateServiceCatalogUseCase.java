package secureauth.application.usecase;

import secureauth.application.dto.ServiceCatalogDTO;
import secureauth.application.validation.ServiceCatalogValidator;
import secureauth.infrastructure.repository.ServiceCatalogRepository;

public class UpdateServiceCatalogUseCase {

    private final ServiceCatalogRepository repository;
    private final ServiceCatalogValidator validator;

    public UpdateServiceCatalogUseCase(ServiceCatalogRepository repository) {
        this(repository, new ServiceCatalogValidator());
    }

    public UpdateServiceCatalogUseCase(ServiceCatalogRepository repository, ServiceCatalogValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public ServiceCatalogDTO update(ServiceCatalogDTO service) {
        if (service.id() == null || service.id() <= 0) {
            throw new IllegalArgumentException("Selecciona un servicio válido para actualizar.");
        }
        validator.validate(service);
        return repository.update(service);
    }
}
