/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.model;


import java.time.LocalDate;


/**
 * Clase que representa un usuario dentro del sistema SecureAuth.
 * <p>
 * Esta clase pertenece a la capa de modelo (Model) dentro del patrón MVC.
 * Es utilizada para transportar datos entre las diferentes capas:
 * controlador, servicio y acceso a datos (DAO).
 * =========================
 * 🔗 DEPENDENCIAS (FLUJO)
 * =========================
 * User ← UserDAO ← AuthService ← AuthController ← LoginFrame
 * </p>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 *     <li>Encapsular los datos del usuario</li>
 *     <li>Servir como objeto de transferencia (DTO)</li>
 * </ul>
 *
 * <h2>Ejemplo de uso</h2>
 * <pre>
 * {@code
 * User user = new User();
 * user.setEmail("test@gmail.com");
 * user.setPassword("1234");
 * }
 * </pre>
 *
 * @author Diego
 * @version 1.0
 * @see dao.UserDAO
 */
public class User {

    /** Identificador único del usuario */
    private int id;

    /** Correo electrónico del usuario */
    private String email;

    /** Contraseña del usuario (debe almacenarse en hash) */
    private String password;

    /** Nombre del usuario */
    private String nombre;

    /** Apellido del usuario */
    private String apellido;

    /** Fecha de nacimiento */
    private LocalDate fechaNacimiento;

    /** Género del usuario */
    private String genero;
    
  

    /**
     * Constructor vacío.
     * <p>
     * Necesario para frameworks y para inicialización manual.
     * </p>
     */
    public User() {
    }
    
    
    
    /**
     * Costructor inicio de seccion
     * @param password contraseña (hash)
     * @param email correo electrónico
     */
    
    public User (String password,String email){
        
        this.email = email;
        this.password = password; 
    }

    /**
     * Constructor completo.
     *
     * @param id identificador del usuario
     * @param email correo electrónico
     * @param password contraseña (hash)
     * @param nombre nombre del usuario
     * @param apellido apellido del usuario
     * @param fechaNacimiento fecha de nacimiento
     * @param genero género
     */
    public User(int id, String email, String password,
                String nombre, String apellido,
                LocalDate fechaNacimiento, String genero) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================

    /**
     * Obtiene el ID del usuario.
     *
     * @return id del usuario
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el ID del usuario.
     *
     * @param id identificador
     */
    public void setId(int id) {
        this.id = id;
    }
    
   
    /**
     * Obtiene el correo electrónico.
     *
     * @return email del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico.
     *
     * @param email correo
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña.
     *
     * @return contraseña (hash)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña.
     *
     * @param password contraseña en texto plano o hash
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre
     */
    public String getNombrecompletoString() {
        return nombre + " " + apellido;
    }


    /**
     * Establece el nombre del usuario.
     *
     * @param nombre nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del usuario.
     *
     * @return apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del usuario.
     *
     * @param apellido apellido
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene la fecha de nacimiento.
     *
     * @return {@link LocalDate}
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento.
     *
     * @param fechaNacimiento fecha
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Obtiene el género del usuario.
     *
     * @return género
     */
    public String getGenero() {
        return genero;
    }

    /**
     * Establece el género del usuario.
     *
     * @param genero género
     */
    public void setGenero(String genero) {
        this.genero = genero;
    }

    /**
     * Representación en texto del usuario.
     *
     * @return cadena con datos principales
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                '}';
    }
    
  
    
}



