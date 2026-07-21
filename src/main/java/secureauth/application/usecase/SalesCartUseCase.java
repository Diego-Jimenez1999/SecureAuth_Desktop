package secureauth.application.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import secureauth.application.dto.SaleItemDTO;
import secureauth.application.validation.SaleValidator;

public class SalesCartUseCase {

    public static final double TAX_RATE = 0.19d;

    private final List<SaleItemDTO> items = new ArrayList<>();
    private final SaleValidator validator;

    public SalesCartUseCase() {
        this(new SaleValidator());
    }

    public SalesCartUseCase(SaleValidator validator) {
        this.validator = validator;
    }

    public void addItem(SaleItemDTO item) {
        validator.validateItem(item);
        int existingIndex = findMatchingItemIndex(item);
        if (existingIndex >= 0) {
            updateQuantity(existingIndex, items.get(existingIndex).quantity() + item.quantity());
        } else {
            items.add(item);
        }
    }

    public boolean removeItemAt(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        items.remove(index);
        return true;
    }

    public boolean incrementQuantity(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        return updateQuantity(index, items.get(index).quantity() + 1);
    }

    public boolean decrementQuantity(int index) {
        if (!isValidIndex(index)) {
            return false;
        }
        int currentQuantity = items.get(index).quantity();
        if (currentQuantity > 1) {
            return updateQuantity(index, currentQuantity - 1);
        }
        return true;
    }

    public boolean updateQuantity(int index, int quantity) {
        if (!isValidIndex(index)) {
            return false;
        }
        SaleItemDTO updated = items.get(index).withQuantity(quantity);
        validator.validateItem(updated);
        items.set(index, updated);
        return true;
    }

    public List<SaleItemDTO> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void clear() {
        items.clear();
    }

    public double subtotal() {
        return items.stream().mapToDouble(SaleItemDTO::subtotal).sum();
    }

    public double tax() {
        return subtotal() * TAX_RATE;
    }

    public double total() {
        return subtotal() + tax();
    }

    public int unitsCount() {
        return items.stream().mapToInt(SaleItemDTO::quantity).sum();
    }

    private int findMatchingItemIndex(SaleItemDTO item) {
        if (item.requiresAppointment()) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            SaleItemDTO current = items.get(i);
            if (current.requiresAppointment()) {
                continue;
            }
            boolean sameInventory = current.inventoryItemId() != null
                    && current.inventoryItemId().equals(item.inventoryItemId());
            boolean sameCatalog = current.inventoryItemId() == null
                    && current.catalogItemId() == item.catalogItemId()
                    && current.name().equals(item.name())
                    && Double.compare(current.price(), item.price()) == 0;
            if (sameInventory || sameCatalog) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < items.size();
    }
}
