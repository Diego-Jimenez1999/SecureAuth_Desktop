package secureauth.application.dto;

import java.util.List;

public record ServiceCatalogDTO(
        Integer id,
        String name,
        String code,
        String description,
        String category,
        String categoryColorHex,
        double price,
        int durationMinutes,
        String colorHex,
        boolean active,
        String observations,
        List<ServiceProductDTO> suggestedProducts) {

    public ServiceCatalogDTO {
        suggestedProducts = List.copyOf(suggestedProducts == null ? List.of() : suggestedProducts);
    }
}
