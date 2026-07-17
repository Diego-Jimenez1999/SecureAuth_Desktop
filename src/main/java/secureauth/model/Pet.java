package secureauth.model;

/**
 * POJO que representa la entidad de negocio para la tabla {@code pets}.
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class Pet {

    private int id;
    private int businessId;
    private int ownerId;
    private String nombreMascota;
    private String raza;
    private String edad;
    private double peso;
    private String sexo;
    private String frecuenciaAlimentacion;
    private String tipoAlimento;
    private String estadoSalud;
    private String vacunas;
    private String cuidadosEspeciales;
    private String notasAdicionales;
    private String imagenPath;

    /**
     * Constructor vacío requerido para frameworks y mapeos manuales.
     */
    public Pet() {
    }

    /**
     * Constructor completo para inicialización rápida del objeto.
     *
     * @param id identificador de la mascota
     * @param ownerId identificador del dueño (llave foránea)
     * @param nombreMascota nombre de la mascota
     * @param raza raza de la mascota
     * @param edad edad de la mascota
     * @param peso peso de la mascota
     * @param sexo sexo de la mascota
     * @param frecuenciaAlimentacion frecuencia de alimentación
     * @param tipoAlimento tipo de alimento principal
     * @param estadoSalud estado de salud general
     * @param vacunas información de vacunas
     * @param cuidadosEspeciales cuidados médicos o especiales
     * @param notasAdicionales notas libres para observaciones
     * @param imagenPath ruta de imagen asociada
     */
    public Pet(
            int id,
            int ownerId,
            String nombreMascota,
            String raza,
            String edad,
            double peso,
            String sexo,
            String frecuenciaAlimentacion,
            String tipoAlimento,
            String estadoSalud,
            String vacunas,
            String cuidadosEspeciales,
            String notasAdicionales,
            String imagenPath) {
        this(id, 0, ownerId, nombreMascota, raza, edad, peso, sexo, frecuenciaAlimentacion,
                tipoAlimento, estadoSalud, vacunas, cuidadosEspeciales, notasAdicionales, imagenPath);
    }

    /**
     * Constructor completo con contexto multiempresa.
     *
     * @param id identificador de la mascota
     * @param businessId identificador de la empresa activa
     * @param ownerId identificador del dueño (llave foránea)
     * @param nombreMascota nombre de la mascota
     * @param raza raza de la mascota
     * @param edad edad de la mascota
     * @param peso peso de la mascota
     * @param sexo sexo de la mascota
     * @param frecuenciaAlimentacion frecuencia de alimentación
     * @param tipoAlimento tipo de alimento principal
     * @param estadoSalud estado de salud general
     * @param vacunas información de vacunas
     * @param cuidadosEspeciales cuidados médicos o especiales
     * @param notasAdicionales notas libres para observaciones
     * @param imagenPath ruta de imagen asociada
     */
    public Pet(
            int id,
            int businessId,
            int ownerId,
            String nombreMascota,
            String raza,
            String edad,
            double peso,
            String sexo,
            String frecuenciaAlimentacion,
            String tipoAlimento,
            String estadoSalud,
            String vacunas,
            String cuidadosEspeciales,
            String notasAdicionales,
            String imagenPath) {
        this.id = id;
        this.businessId = businessId;
        this.ownerId = ownerId;
        this.nombreMascota = nombreMascota;
        this.raza = raza;
        this.edad = edad;
        this.peso = peso;
        this.sexo = sexo;
        this.frecuenciaAlimentacion = frecuenciaAlimentacion;
        this.tipoAlimento = tipoAlimento;
        this.estadoSalud = estadoSalud;
        this.vacunas = vacunas;
        this.cuidadosEspeciales = cuidadosEspeciales;
        this.notasAdicionales = notasAdicionales;
        this.imagenPath = imagenPath;
    }

    /**
     * @return identificador de la mascota
     */
    public int getId() {
        return id;
    }

    /**
     * @param id identificador de la mascota
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return identificador de la empresa activa
     */
    public int getBusinessId() {
        return businessId;
    }

    /**
     * @param businessId identificador de la empresa activa
     */
    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    /**
     * @return identificador del dueño
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId identificador del dueño
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return nombre de la mascota
     */
    public String getNombreMascota() {
        return nombreMascota;
    }

    /**
     * @param nombreMascota nombre de la mascota
     */
    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    /**
     * @return raza de la mascota
     */
    public String getRaza() {
        return raza;
    }

    /**
     * @param raza raza de la mascota
     */
    public void setRaza(String raza) {
        this.raza = raza;
    }

    /**
     * @return edad de la mascota
     */
    public String getEdad() {
        return edad;
    }

    /**
     * @param edad edad de la mascota
     */
    public void setEdad(String edad) {
        this.edad = edad;
    }

    /**
     * @return peso de la mascota
     */
    public double getPeso() {
        return peso;
    }

    /**
     * @param peso peso de la mascota
     */
    public void setPeso(double peso) {
        this.peso = peso;
    }

    /**
     * @return sexo de la mascota
     */
    public String getSexo() {
        return sexo;
    }

    /**
     * @param sexo sexo de la mascota
     */
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    /**
     * @return frecuencia de alimentación
     */
    public String getFrecuenciaAlimentacion() {
        return frecuenciaAlimentacion;
    }

    /**
     * @param frecuenciaAlimentacion frecuencia de alimentación
     */
    public void setFrecuenciaAlimentacion(String frecuenciaAlimentacion) {
        this.frecuenciaAlimentacion = frecuenciaAlimentacion;
    }

    /**
     * @return tipo de alimento
     */
    public String getTipoAlimento() {
        return tipoAlimento;
    }

    /**
     * @param tipoAlimento tipo de alimento
     */
    public void setTipoAlimento(String tipoAlimento) {
        this.tipoAlimento = tipoAlimento;
    }

    /**
     * @return estado de salud
     */
    public String getEstadoSalud() {
        return estadoSalud;
    }

    /**
     * @param estadoSalud estado de salud
     */
    public void setEstadoSalud(String estadoSalud) {
        this.estadoSalud = estadoSalud;
    }

    /**
     * @return datos de vacunación
     */
    public String getVacunas() {
        return vacunas;
    }

    /**
     * @param vacunas datos de vacunación
     */
    public void setVacunas(String vacunas) {
        this.vacunas = vacunas;
    }

    /**
     * @return cuidados especiales
     */
    public String getCuidadosEspeciales() {
        return cuidadosEspeciales;
    }

    /**
     * @param cuidadosEspeciales cuidados especiales
     */
    public void setCuidadosEspeciales(String cuidadosEspeciales) {
        this.cuidadosEspeciales = cuidadosEspeciales;
    }

    /**
     * @return notas adicionales
     */
    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    /**
     * @param notasAdicionales notas adicionales
     */
    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }

    /**
     * @return ruta de imagen de la mascota
     */
    public String getImagenPath() {
        return imagenPath;
    }

    /**
     * @param imagenPath ruta de imagen de la mascota
     */
    public void setImagenPath(String imagenPath) {
        this.imagenPath = imagenPath;
    }
}
