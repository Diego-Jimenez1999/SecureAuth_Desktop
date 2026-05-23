package secureauth.controller;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import secureauth.model.Owner;
import secureauth.model.Pet;
import secureauth.service.OwnerService;
import secureauth.service.PetService;
import secureauth.ui.components.RegMascotaPanel;

/**
 * Controlador de eventos para el módulo de registro de mascotas.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class PetController {

    private final RegMascotaPanel view;
    private final PetService service;
    private final OwnerService ownerService;

    /**
     * Constructor por inyección de dependencias.
     *
     * @param view vista del registro de mascota
     * @param service servicio de negocio de mascotas
     * @param ownerService servicio de negocio de dueños
     */
    public PetController(RegMascotaPanel view, PetService service, OwnerService ownerService) {
        this.view = view;
        this.service = service;
        this.ownerService = ownerService;
        loadOwnersIntoCombo();
        syncOwnerDetails();
        bindEvents();
    }

    private void bindEvents() {
        view.getBtnGuardarMascota().addActionListener(e -> onGuardarMascota());
        view.getBtnSubirImagen().addActionListener(e -> openImageChooser());
        view.getCbOwner().addActionListener(e -> syncOwnerDetails());
    }

    @SuppressWarnings("UseSpecificCatch")
    private void onGuardarMascota() {
        try {
            Pet pet = buildPetFromView();
            boolean ok = service.registerPet(pet);
            if (ok) {
                view.showSuccess("Mascota registrada correctamente.");
                view.limpiarFormulario();
            } else {
                view.showError("No se pudo guardar la mascota en base de datos.");
            }
        } catch (IllegalArgumentException | IOException ex) {
            String errorPrefix = (ex instanceof IOException) ? "No se pudo procesar la imagen: " : "";
            view.showError(errorPrefix + ex.getMessage());
        } catch (Exception ex) {
            view.showError("Error inesperado al registrar mascota.");
        }
    }

    private Pet buildPetFromView() {
        Owner owner = view.getSelectedOwner();
        if (owner == null) {
            throw new IllegalArgumentException("Debe seleccionar un dueño registrado antes de agregar la mascota");
        }

        Pet pet = new Pet();
        pet.setOwnerId(owner.getId());
        pet.setNombreMascota(view.getNombreMascota());
        pet.setRaza(view.getRazaMascota());
        pet.setEdad(view.getEdadMascota());
        pet.setPeso(parsePeso(view.getPesoMascota()));
        pet.setSexo(normalizeSexo(view.getSexoMascota()));
        pet.setFrecuenciaAlimentacion(view.getFrecuenciaAlimentacion());
        pet.setTipoAlimento(view.getTipoAlimento());
        pet.setEstadoSalud(view.getEstadoSalud());
        pet.setVacunas(view.getVacunas());
        pet.setCuidadosEspeciales(view.getCuidadosEspeciales());
        pet.setNotasAdicionales(view.getNotasAdicionales());
        pet.setImagenPath(view.getRutaImagenSeleccionada());
        return pet;
    }

    public void reloadOwners() {
        loadOwnersIntoCombo();
        syncOwnerDetails();
    }

    private void loadOwnersIntoCombo() {
        Object selected = view.getCbOwner().getSelectedItem();
        Integer selectedId = selected instanceof Owner owner ? owner.getId() : null;
        view.getCbOwner().removeAllItems();
        for (Owner owner : ownerService.findAllOwners()) {
            view.getCbOwner().addItem(owner);
            if (selectedId != null && selectedId == owner.getId()) {
                view.getCbOwner().setSelectedItem(owner);
            }
        }
        if (selectedId == null) {
            view.getCbOwner().setSelectedItem(null);
        }
    }

    private void syncOwnerDetails() {
        Owner owner = view.getSelectedOwner();
        view.setOwnerDetails(owner);
    }

    private double parsePeso(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("El peso debe ser un número válido.");
        }
    }

    private String normalizeSexo(String sexo) {
        if (sexo == null) {
            throw new IllegalArgumentException("El sexo es obligatorio.");
        }
        return switch (sexo.trim().toUpperCase()) {
            case "M", "MACHO" -> "Macho";
            case "F", "HEMBRA" -> "Hembra";
            default -> throw new IllegalArgumentException("Sexo inválido. Usa Macho o Hembra.");
        };
    }

    /**
     * Abre selector de imagen (JPG/PNG), guarda ruta y actualiza preview.
     */
    public void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes JPG/PNG", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            view.setRutaImagenSeleccionada(file.getAbsolutePath());
            updateImagePreview(file);
        }
    }

    private void updateImagePreview(File file) {
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image scaled = icon.getImage().getScaledInstance(330, 360, Image.SCALE_SMOOTH);
        view.getLblImagenMascota().setText("");
        view.getLblImagenMascota().setIcon(new ImageIcon(scaled));
    }
}
