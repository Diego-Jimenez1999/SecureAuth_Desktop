package secureauth.model;

/**
 * Modelo de dominio para registrar mascotas y datos del propietario.
 */
public class Mascota {

    private int id;
    private int ownerId;
    private String nombre;
    private String tipo;
    private String raza;
    private int edad;
    private double peso;
    private String sexo;
    private String frecuenciaAlimentacion;
    private String descripcionCuidados;
    private String estadoSalud;

    private String nombreDueno;
    private String telefonoDueno;
    private String correoDueno;
    private String direccionDueno;

    private String rutaImagen;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getFrecuenciaAlimentacion() { return frecuenciaAlimentacion; }
    public void setFrecuenciaAlimentacion(String frecuenciaAlimentacion) { this.frecuenciaAlimentacion = frecuenciaAlimentacion; }
    public String getDescripcionCuidados() { return descripcionCuidados; }
    public void setDescripcionCuidados(String descripcionCuidados) { this.descripcionCuidados = descripcionCuidados; }
    public String getEstadoSalud() { return estadoSalud; }
    public void setEstadoSalud(String estadoSalud) { this.estadoSalud = estadoSalud; }
    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }
    public String getTelefonoDueno() { return telefonoDueno; }
    public void setTelefonoDueno(String telefonoDueno) { this.telefonoDueno = telefonoDueno; }
    public String getCorreoDueno() { return correoDueno; }
    public void setCorreoDueno(String correoDueno) { this.correoDueno = correoDueno; }
    public String getDireccionDueno() { return direccionDueno; }
    public void setDireccionDueno(String direccionDueno) { this.direccionDueno = direccionDueno; }
    public String getRutaImagen() { return rutaImagen; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }
}
