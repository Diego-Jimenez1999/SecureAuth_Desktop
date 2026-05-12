package secureauth.model;

/**
 * Modelo de dominio para dueños de mascotas.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class Owner {

    private int id;
    private String nombreCompleto;
    private String telefono;
    private String correo;
    private String direccion;

    public Owner() {
    }

    public Owner(int id, String nombreCompleto, String telefono, String correo, String direccion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return nombreCompleto + " - " + telefono;
    }
}
