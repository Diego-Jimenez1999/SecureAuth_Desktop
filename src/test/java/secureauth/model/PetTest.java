package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PetTest {

    @Test
    void overloadedConstructorInitializesCoreFields() {
        Pet pet = new Pet(11, 2, "Firulais", "Labrador", "3 años", 25.5, "Macho",
                "2 veces al día", "Croquetas", "Sano", "Vacunas al día", "Sin cuidados",
                "Observación", "img/firulais.png");

        assertEquals(11, pet.getId());
        assertEquals(0, pet.getBusinessId());
        assertEquals(2, pet.getOwnerId());
        assertEquals("Firulais", pet.getNombreMascota());
        assertEquals("Labrador", pet.getRaza());
        assertEquals("3 años", pet.getEdad());
        assertEquals(25.5, pet.getPeso());
        assertEquals("Macho", pet.getSexo());
        assertEquals("img/firulais.png", pet.getImagenPath());
    }

    @Test
    void settersUpdateValuesForAllAttributes() {
        Pet pet = new Pet();

        pet.setId(42);
        pet.setBusinessId(9);
        pet.setOwnerId(18);
        pet.setNombreMascota("Milo");
        pet.setRaza("Pastor Alemán");
        pet.setEdad("5 años");
        pet.setPeso(30.2);
        pet.setSexo("Hembra");
        pet.setFrecuenciaAlimentacion("1 vez al día");
        pet.setTipoAlimento("Pasta");
        pet.setEstadoSalud("En recuperación");
        pet.setVacunas("Pendientes");
        pet.setCuidadosEspeciales("Rehabilitación");
        pet.setNotasAdicionales("Muy activo");
        pet.setImagenPath("img/milo.png");

        assertEquals(42, pet.getId());
        assertEquals(9, pet.getBusinessId());
        assertEquals(18, pet.getOwnerId());
        assertEquals("Milo", pet.getNombreMascota());
        assertEquals("Pastor Alemán", pet.getRaza());
        assertEquals("5 años", pet.getEdad());
        assertEquals(30.2, pet.getPeso());
        assertEquals("Hembra", pet.getSexo());
        assertEquals("1 vez al día", pet.getFrecuenciaAlimentacion());
        assertEquals("Pasta", pet.getTipoAlimento());
        assertEquals("En recuperación", pet.getEstadoSalud());
        assertEquals("Pendientes", pet.getVacunas());
        assertEquals("Rehabilitación", pet.getCuidadosEspeciales());
        assertEquals("Muy activo", pet.getNotasAdicionales());
        assertEquals("img/milo.png", pet.getImagenPath());
    }

    @Test
    void defaultConstructorLeavesFieldsEmpty() {
        Pet pet = new Pet();

        assertEquals(0, pet.getId());
        assertEquals(0, pet.getBusinessId());
        assertEquals(0, pet.getOwnerId());
        assertNull(pet.getNombreMascota());
        assertNull(pet.getRaza());
        assertNull(pet.getEdad());
        assertEquals(0.0, pet.getPeso());
        assertNull(pet.getSexo());
        assertNull(pet.getFrecuenciaAlimentacion());
        assertNull(pet.getTipoAlimento());
        assertNull(pet.getEstadoSalud());
        assertNull(pet.getVacunas());
        assertNull(pet.getCuidadosEspeciales());
        assertNull(pet.getNotasAdicionales());
        assertNull(pet.getImagenPath());
    }
}
