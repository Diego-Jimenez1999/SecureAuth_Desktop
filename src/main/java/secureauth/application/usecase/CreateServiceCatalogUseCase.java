package secureauth.application.usecase;

import secureauth.application.dto.ServiceCatalogDTO;
import secureauth.application.validation.ServiceCatalogValidator;
import secureauth.infrastructure.repository.ServiceCatalogRepository;

public class CreateServiceCatalogUseCase {

    private final ServiceCatalogRepository repository;
    private final ServiceCatalogValidator validator;

    public CreateServiceCatalogUseCase(ServiceCatalogRepository repository) {
        this(repository, new ServiceCatalogValidator());
    }

    public CreateServiceCatalogUseCase(ServiceCatalogRepository repository, ServiceCatalogValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public ServiceCatalogDTO create(ServiceCatalogDTO service) {
        validator.validate(service);
        return repository.create(service);
    }
}
