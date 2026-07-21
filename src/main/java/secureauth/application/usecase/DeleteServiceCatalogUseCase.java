package secureauth.application.usecase;

import secureauth.infrastructure.repository.ServiceCatalogRepository;

public class DeleteServiceCatalogUseCase {

    private final ServiceCatalogRepository repository;

    public DeleteServiceCatalogUseCase(ServiceCatalogRepository repository) {
        this.repository = repository;
    }

    public void delete(int serviceId) {
        if (serviceId <= 0) {
            throw new IllegalArgumentException("Selecciona un servicio válido para eliminar.");
        }
        repository.delete(serviceId);
    }
}
