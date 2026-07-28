package secureauth.ui.sales;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import secureauth.dao.SalesCatalogDAO;
import secureauth.domain.sales.SaleItemType;
import secureauth.service.enterprise.EnterpriseContext;

/**
 * Repositorio unificado de categorías, servicios y precios por tamaño.
 */
public final class SalesServiceCatalog {

    private static final SalesServiceCatalog INSTANCE = new SalesServiceCatalog();

    private final AtomicInteger categoryIdGen = new AtomicInteger(1);
    private final AtomicInteger itemIdGen = new AtomicInteger(1);
    private final List<CategoryEntry> categories = new ArrayList<>();
    private final List<ServiceItemEntry> items = new ArrayList<>();
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private SalesCatalogDAO dao;
    private final EnterpriseContext context = EnterpriseContext.getInstance();

    private SalesServiceCatalog() {
    }

    public synchronized void setDao(SalesCatalogDAO dao) {
        this.dao = dao;
        loadFromPersistenceOrSeed();
    }

    public static SalesServiceCatalog getInstance() { return INSTANCE; }

    public void addCatalogListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener("catalog", listener);
    }

    public void removeCatalogListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener("catalog", listener);
    }

    public synchronized List<CategoryEntry> getCategories() {
        return Collections.unmodifiableList(new ArrayList<>(categories));
    }

    public synchronized List<ServiceItemEntry> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized void reload() {
        categories.clear();
        items.clear();
        loadFromPersistenceOrSeed();
        fireChanged();
    }

    public synchronized void addCategory(String category, String subcategory) {
        int id = dao.insertCategory(context.getActiveBusinessId(), context.getActiveBranchId(), category, subcategory);
        if (id > 0) {
            categories.add(new CategoryEntry(id, category, subcategory));
        } else {
            categories.add(new CategoryEntry(categoryIdGen.getAndIncrement(), category, subcategory));
        }
        fireChanged();
    }

    public synchronized void removeCategory(int categoryId) {
        Optional<CategoryEntry> found = categories.stream().filter(c -> c.id() == categoryId).findFirst();
        if (found.isEmpty()) {
            return;
        }
        CategoryEntry category = found.get();
        categories.remove(category);
        List<Integer> removedIds = items.stream()
                .filter(i -> i.category().equalsIgnoreCase(category.category()) && i.subcategory().equalsIgnoreCase(category.subcategory()))
                .map(ServiceItemEntry::id)
                .toList();
        items.removeIf(i -> removedIds.contains(i.id()));
        dao.deleteCategory(categoryId);
        removedIds.forEach(dao::deleteItem);
        fireChanged();
    }

    public synchronized void upsertItem(ServiceItemEntry entry) {
        ServiceItemEntry normalized = normalize(entry);
        int storedId = dao.upsertItem(context.getActiveBusinessId(), context.getActiveBranchId(), normalized);
        int finalId = storedId > 0 ? storedId : (entry.id() > 0 ? entry.id() : itemIdGen.getAndIncrement());
        ServiceItemEntry persisted = normalized.withId(finalId);

        if (entry.id() <= 0) {
            items.add(persisted);
        } else {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).id() == entry.id()) {
                    items.set(i, persisted);
                    fireChanged();
                    return;
                }
            }
            items.add(persisted);
        }
        fireChanged();
    }

    public synchronized void removeItem(int itemId) {
        items.removeIf(i -> i.id() == itemId);
        dao.deleteItem(itemId);
        fireChanged();
    }

    private ServiceItemEntry normalize(ServiceItemEntry entry) {
        double gain = entry.price() - entry.cost();
        return new ServiceItemEntry(entry.id(), entry.category(), entry.subcategory(), entry.name(), entry.type(),
                entry.price(), entry.cost(), gain, entry.status(), entry.stock(), new LinkedHashMap<>(entry.sizePrices()));
    }

    private void loadFromPersistenceOrSeed() {
        dao.ensureSchema();
        dao.syncInventoryCatalog(context.getActiveBusinessId(), context.getActiveBranchId());
        dao.removeLegacyDomainData(context.getActiveBusinessId(), context.getActiveBranchId());
        List<CategoryEntry> dbCategories = dao.findAllCategories(context.getActiveBusinessId(), context.getActiveBranchId());
        List<ServiceItemEntry> dbItems = dao.findAllItems(context.getActiveBusinessId(), context.getActiveBranchId());
        List<ServiceItemEntry> inventoryItems = dao.findInventoryItems(context.getActiveBusinessId(), context.getActiveBranchId());

        if (!dbCategories.isEmpty() || !dbItems.isEmpty() || !inventoryItems.isEmpty()) {
            categories.addAll(dbCategories);
            items.addAll(dbItems);
            items.addAll(inventoryItems);
            categoryIdGen.set(categories.stream().mapToInt(CategoryEntry::id).max().orElse(0) + 1);
            itemIdGen.set(items.stream().mapToInt(ServiceItemEntry::id).filter(id -> id > 0).max().orElse(0) + 1);
            return;
        }

        seedDefaults();
    }

    private void fireChanged() {
        changeSupport.firePropertyChange("catalog", null, null);
    }

    /**
     * Seed inicial orientado a guardería canina, con estructura extensible para otros negocios.
     */
    private void seedDefaults() {
        addCategory("Guardería Canina", "Cuidado Diario");
        addCategory("Guardería Canina", "Baño y Estética");
        addCategory("Guardería Canina", "Salud Veterinaria");

        Map<String, Double> estancia = new LinkedHashMap<>();
        estancia.put("Medio Día", 30000d);
        estancia.put("Día Completo", 55000d);
        estancia.put("Noche", 70000d);
        upsertItem(new ServiceItemEntry(0, "Guardería Canina", "Cuidado Diario", "Estadía", "Servicio", 55000d,
                30000d, 25000d, "Activo", null, estancia));

        Map<String, Double> bano = new LinkedHashMap<>();
        bano.put("Pequeño", 28000d);
        bano.put("Mediano", 35000d);
        bano.put("Grande", 42000d);
        upsertItem(new ServiceItemEntry(0, "Guardería Canina", "Baño y Estética", "Baño Canino", "Servicio", 35000d,
                18000d, 17000d, "Activo", null, bano));

        Map<String, Double> consulta = new LinkedHashMap<>();
        consulta.put("Básica", 35000d);
        consulta.put("Especializada", 65000d);
        consulta.put("Urgencia", 110000d);
        upsertItem(new ServiceItemEntry(0, "Guardería Canina", "Salud Veterinaria", "Consulta", "Servicio", 35000d,
                16000d, 19000d, "Activo", null, consulta));
    }

    public record CategoryEntry(int id, String category, String subcategory) { }

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
}
