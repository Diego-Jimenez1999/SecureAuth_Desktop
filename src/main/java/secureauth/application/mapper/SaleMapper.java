package secureauth.application.mapper;

import java.util.List;

import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.domain.sales.SaleItemType;
import secureauth.model.SaleItem;
import secureauth.model.Venta;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static SaleItemDTO toDTO(SaleItem item) {
        return new SaleItemDTO(item.getName(), item.getPrice(), item.getCatalogItemId(), item.getInventoryItemId(),
                item.getSku(), SaleItemType.fromCatalogValue(item.getType()), item.getCategory(),
                item.getStockAvailable(), 0d, item.getQuantity());
    }

    public static SaleItem toDomain(SaleItemDTO dto) {
        SaleItem item = new SaleItem(dto.name(), dto.price(), dto.catalogItemId(), dto.inventoryItemId(), dto.sku(),
                dto.type() == null ? null : dto.type().displayName(), dto.category(), dto.stockAvailable());
        item.setQuantity(dto.quantity());
        return item;
    }

    public static List<SaleItem> toDomainItems(List<SaleItemDTO> items) {
        return items.stream().map(SaleMapper::toDomain).toList();
    }

    public static Venta toDomain(SaleDTO dto) {
        Venta venta = new Venta(dto.id(), dto.date(), dto.customerName(), dto.total(), dto.paymentMethod(),
                dto.sellerName());
        for (SaleItemDTO item : dto.items()) {
            venta.addItem(toDomain(item));
        }
        return venta;
    }
}
