package secureauth.controller;

import java.util.regex.Pattern;

import secureauth.model.Mascota;
import secureauth.service.MascotaService;
import secureauth.ui.frames.MascotaRegistroFrame;

/**
 * Controlador del formulario de registro de mascotas.
 */
public class MascotaController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final MascotaService mascotaService;
    private MascotaRegistroFrame view;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    public void bindView(MascotaRegistroFrame view) {
        this.view = view;
    }

    public void guardarMascota() {
        if (view == null) {
            return;
        }

        String nombre = view.getNombreMascota();
        String tipo = view.getTipoMascota();
        String raza = view.getRazaMascota();
        String edadTxt = view.getEdadMascota();
        String pesoTxt = view.getPesoMascota();
        String sexo = view.getSexoMascota();
        String frecuencia = view.getFrecuenciaAlimentacion();
        String cuidados = view.getCuidadosEspeciales();
        String estadoSalud = view.getEstadoSalud();

        String nombreDueno = view.getNombreDueno();
        String telefonoDueno = view.getTelefonoDueno();
        String correoDueno = view.getCorreoDueno();
        String direccionDueno = view.getDireccionDueno();

        if (isBlank(nombre) || isBlank(tipo) || isBlank(raza) || isBlank(edadTxt)
                || isBlank(pesoTxt) || isBlank(sexo) || isBlank(frecuencia) || isBlank(estadoSalud)
                || isBlank(nombreDueno) || isBlank(telefonoDueno) || isBlank(correoDueno) || isBlank(direccionDueno)) {
            view.showError("Completa todos los campos obligatorios.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(correoDueno.trim()).matches()) {
            view.showError("El correo del dueño no es válido.");
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTxt.trim());
            if (edad < 0) {
                view.showError("La edad debe ser un número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("La edad debe ser numérica.");
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoTxt.trim());
            if (peso <= 0) {
                view.showError("El peso debe ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            view.showError("El peso debe ser numérico.");
            return;
        }

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre.trim());
        mascota.setTipo(tipo.trim());
        mascota.setRaza(raza.trim());
        mascota.setEdad(edad);
        mascota.setPeso(peso);
        mascota.setSexo(sexo.trim());
        mascota.setFrecuenciaAlimentacion(frecuencia.trim());
        mascota.setDescripcionCuidados(cuidados == null ? "" : cuidados.trim());
        mascota.setEstadoSalud(estadoSalud.trim());
        mascota.setNombreDueno(nombreDueno.trim());
        mascota.setTelefonoDueno(telefonoDueno.trim());
        mascota.setCorreoDueno(correoDueno.trim());
        mascota.setDireccionDueno(direccionDueno.trim());
        mascota.setRutaImagen(view.getRutaImagenSeleccionada());

        boolean ok = mascotaService.registrar(mascota);
        if (ok) {
            view.showSuccess("Mascota registrada correctamente.");
            view.limpiarFormulario();
        } else {
            view.showError("No se pudo guardar la mascota. Verifica la base de datos.");
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
