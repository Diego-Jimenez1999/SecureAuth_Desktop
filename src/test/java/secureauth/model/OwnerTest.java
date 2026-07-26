package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OwnerTest {

    @Test
    void defaultConstructorInitializesEmptyFields() {
        Owner owner = new Owner();

        assertEquals(0, owner.getId());
        assertNull(owner.getNombreCompleto());
        assertNull(owner.getTelefono());
        assertNull(owner.getCorreo());
        assertNull(owner.getDireccion());
    }

    @Test
    void constructorAndMutatorsPopulateValuesAndStringRepresentation() {
        Owner owner = new Owner(7, "Ana Gómez", "3001234567", "ana@example.com", "Calle 10");

        owner.setDireccion("Carrera 20");
        owner.setTelefono("3209876543");

        assertEquals(7, owner.getId());
        assertEquals("Ana Gómez", owner.getNombreCompleto());
        assertEquals("3209876543", owner.getTelefono());
        assertEquals("ana@example.com", owner.getCorreo());
        assertEquals("Carrera 20", owner.getDireccion());
        assertTrue(owner.toString().contains("Ana Gómez"));
        assertTrue(owner.toString().contains("3209876543"));
    }
}
