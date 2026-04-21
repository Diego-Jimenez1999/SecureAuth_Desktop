/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.model;

import secureauth.dao.UserDAO;
//import secureauth.model.User;
import secureauth.security.PasswordHasher;

import java.time.LocalDate;

/**
 * Clase de prueba para validar operaciones de base de datos.
 * 
 * @author Diego
 * @version 1.0
 */
public class TestUserDAO {

    public static void main(String[] args) {

        try {
            // =========================
            // 1. CREAR USUARIO
            // =========================
            User user = new User();

            user.setEmail("test@gmail.com");
            
            // 🔐 IMPORTANTE: password con hash
            user.setPassword(PasswordHasher.hash("1234"));
            
            user.setNombre("Diego");
            user.setApellido("Jimenez");
            user.setFechaNacimiento(LocalDate.of(2000, 1, 1));
            user.setGenero("M");

            // =========================
            // 2. INSERTAR USUARIO
            // =========================
            UserDAO dao = new UserDAO();

            boolean inserted = dao.insert(user);

            if (inserted) {
                System.out.println("✅ Usuario insertado correctamente");
            } else {
                System.out.println("❌ No se pudo insertar");
            }

            // =========================
            // 3. BUSCAR USUARIO (LOGIN SIMULADO)
            // =========================
            User foundUser = dao.findByEmail("test@gmail.com");

            if (foundUser != null) {
                System.out.println("✅ Usuario encontrado");

                // =========================
                // 4. VALIDAR PASSWORD
                // =========================
                boolean valid = PasswordHasher.verify("1234", foundUser.getPassword());

                if (valid) {
                    System.out.println("🔐 Login correcto");
                } else {
                    System.out.println("❌ Password incorrecto");
                }

            } else {
                System.out.println("❌ Usuario no encontrado");
            }

        } catch (Exception e) {
            System.out.println("❌ Error en la prueba");
            e.printStackTrace();
        }
    }
}