package secureauth.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import secureauth.application.dto.ServiceCatalogDTO;
import secureauth.application.dto.ServiceCategoryDTO;
import secureauth.application.dto.ServiceProductDTO;

public interface ServiceCatalogRepository {
    ServiceCatalogDTO create(ServiceCatalogDTO service);

    ServiceCatalogDTO update(ServiceCatalogDTO service);

    void delete(int serviceId);

    List<ServiceCatalogDTO> findAll();

    Optional<ServiceCatalogDTO> findById(int serviceId);

    List<ServiceCategoryDTO> findCategories();

    ServiceCategoryDTO saveCategory(ServiceCategoryDTO category);

    List<ServiceProductDTO> findInventoryProducts(String query);
}
