package secureauth.model;

/**
 * Representa información resumida de un empleado/trabajador para listar en la interfaz.
 */
public class EmployeeSummary {
    private final int id;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String genero;
    private final String rol;

    public EmployeeSummary(int id, String nombre, String apellido,
                           String email, String genero, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.genero = genero;
        this.rol = rol;
    }

    public int getId()          { return id; }
    public String getNombre()   { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail()    { return email; }
    public String getGenero()   { return genero; }
    public String getRol()      { return rol; }
}
