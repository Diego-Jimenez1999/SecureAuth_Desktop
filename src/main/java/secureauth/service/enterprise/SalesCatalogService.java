package secureauth.service.enterprise;

import java.util.List;

import secureauth.dao.SalesCatalogDAO;
import secureauth.domain.sales.CategoryEntry;
import secureauth.domain.sales.ServiceItemEntry;

/**
 * Servicio de negocio para el catálogo de categorías, servicios y precios.
 *
 * <p>Introducido en la Fase 3 de estabilización arquitectónica: antes,
 * {@link secureauth.service.enterprise.SalesServiceCatalog} llamaba directamente a
 * {@link SalesCatalogDAO}. Ahora la UI pasa por este servicio, respetando
 * la dirección de dependencias {@code UI -> Service -> DAO}.</p>
 *
 * <p>Por ahora es un envoltorio directo del DAO (sin reglas de negocio
 * propias todavía), ya que el objetivo de esta fase es solo reordenar
 * capas, no introducir nueva lógica.</p>
 */
public class SalesCatalogService {

    private final SalesCatalogDAO dao;

    public SalesCatalogService(SalesCatalogDAO dao) {
        this.dao = dao;
    }

    public void ensureSchema() {
        dao.ensureSchema();
    }

    public void syncInventoryCatalog(int businessId, int branchId) {
        dao.syncInventoryCatalog(businessId, branchId);
    }

    public void removeLegacyDomainData(int businessId, int branchId) {
        dao.removeLegacyDomainData(businessId, branchId);
    }

    public List<CategoryEntry> findAllCategories(int businessId, int branchId) {
        return dao.findAllCategories(businessId, branchId);
    }

    public List<ServiceItemEntry> findAllItems(int businessId, int branchId) {
        return dao.findAllItems(businessId, branchId);
    }

    public List<ServiceItemEntry> findInventoryItems(int businessId, int branchId) {
        return dao.findInventoryItems(businessId, branchId);
    }

    public int insertCategory(int businessId, int branchId, String category, String subcategory) {
        return dao.insertCategory(businessId, branchId, category, subcategory);
    }

    public void deleteCategory(int categoryId) {
        dao.deleteCategory(categoryId);
    }

    public int upsertItem(int businessId, int branchId, ServiceItemEntry item) {
        return dao.upsertItem(businessId, branchId, item);
    }

    public void deleteItem(int itemId) {
        dao.deleteItem(itemId);
    }
}
