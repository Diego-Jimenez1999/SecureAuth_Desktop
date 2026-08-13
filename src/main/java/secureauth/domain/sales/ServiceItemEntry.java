package secureauth.domain.sales;

import java.util.Map;

public record ServiceItemEntry(int id, String category, String subcategory, String name, String type,
                               double price, double cost, double gain, String status, Integer stock,
                               Map<String, Double> sizePrices, Integer inventoryItemId, String sku) {
    public ServiceItemEntry(int id, String category, String subcategory, String name, String type,
            double price, double cost, double gain, String status, Integer stock,
            Map<String, Double> sizePrices) {
        this(id, category, subcategory, name, type, price, cost, gain, status, stock, sizePrices, null, null);
    }

    public ServiceItemEntry withId(int newId) {
        return new ServiceItemEntry(newId, category, subcategory, name, type, price, cost, gain, status, stock,
                sizePrices, inventoryItemId, sku);
    }

    public SaleItemType saleItemType() {
        return SaleItemType.fromCatalogValue(type);
    }
}
