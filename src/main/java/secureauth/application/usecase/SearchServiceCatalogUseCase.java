package secureauth.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import secureauth.application.dto.ServiceCatalogDTO;
import secureauth.infrastructure.repository.ServiceCatalogRepository;

public class SearchServiceCatalogUseCase {

    private final ServiceCatalogRepository repository;

    public SearchServiceCatalogUseCase(ServiceCatalogRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCatalogDTO> search(String query, StatusFilter statusFilter) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        StatusFilter filter = statusFilter == null ? StatusFilter.ALL : statusFilter;
        return repository.findAll().stream()
                .filter(service -> matchesStatus(service, filter))
                .filter(service -> matchesQuery(service, normalized))
                .sorted(Comparator.comparing(ServiceCatalogDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean matchesStatus(ServiceCatalogDTO service, StatusFilter filter) {
        return switch (filter) {
            case ACTIVE -> service.active();
            case INACTIVE -> !service.active();
            case ALL -> true;
        };
    }

    private boolean matchesQuery(ServiceCatalogDTO service, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return contains(service.name(), query)
                || contains(service.code(), query)
                || contains(service.category(), query)
                || (service.active() && "activo".contains(query))
                || (!service.active() && "inactivo".contains(query));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    public enum StatusFilter {
        ALL,
        ACTIVE,
        INACTIVE
    }
}
