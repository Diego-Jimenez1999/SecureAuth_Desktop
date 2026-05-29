package secureauth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import secureauth.dao.PetDAO;
import secureauth.model.Pet;

/**
 * Servicio de negocio para operaciones de mascotas.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class PetService {

    private final PetDAO petDAO;

    /**
     * Constructor por inyección de dependencias.
     *
     * @param petDAO DAO para persistencia de mascotas
     */
    public PetService(PetDAO petDAO) {
        this.petDAO = Objects.requireNonNull(petDAO, "PetDAO es requerido");
    }

    /**
     * Registra una mascota validando reglas de negocio e imagen.
     *
     * <p>Campos obligatorios: nombre, raza, peso y owner_id.</p>
     *
     * @param pet entidad de mascota a registrar
     * @return {@code true} si el registro fue exitoso, {@code false} si no se pudo persistir
     * @throws IllegalArgumentException cuando falta un campo obligatorio o el peso es inválido
     * @throws IOException cuando falla la copia de la imagen a {@code /resources/pets/}
     */
    public boolean registerPet(Pet pet) throws IOException {
        validateRequiredFields(pet);
        petDAO.ensureSchema();

        if (!isBlank(pet.getImagenPath())) {
            String copiedPath = copyImageToInternalResources(pet.getImagenPath());
            pet.setImagenPath(copiedPath);
        }

        return petDAO.insert(pet);
    }

    private void validateRequiredFields(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("La mascota no puede ser nula.");
        }
        if (isBlank(pet.getNombreMascota())) {
            throw new IllegalArgumentException("El nombre de la mascota es obligatorio.");
        }
        if (isBlank(pet.getRaza())) {
            throw new IllegalArgumentException("La raza es obligatoria.");
        }
        if (pet.getPeso() <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que 0.");
        }
        if (pet.getOwnerId() <= 0) {
            throw new IllegalArgumentException("El owner_id es obligatorio.");
        }
    }

    /**
     * Copia la imagen seleccionada por el usuario a la carpeta interna del proyecto.
     *
     * @param originalPath ruta original seleccionada en el sistema de archivos
     * @return ruta relativa almacenable en base de datos
     * @throws IOException si ocurre un error al crear carpetas o copiar el archivo
     */
    private String copyImageToInternalResources(String originalPath) throws IOException {
        Path source = Path.of(originalPath);
        if (!Files.exists(source)) {
            throw new IOException("La imagen seleccionada no existe: " + originalPath);
        }

        // Usar una carpeta fuera del JAR para persistencia real
        Path targetDir = Path.of(System.getProperty("user.home"), ".secureauth", "uploads", "pets");
        Files.createDirectories(targetDir);

        String fileName = source.getFileName().toString();
        String extension = "";
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0) {
            extension = fileName.substring(dot);
        }
        String newName = UUID.randomUUID() + extension;
        Path target = targetDir.resolve(newName);

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return "pets/" + newName;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
