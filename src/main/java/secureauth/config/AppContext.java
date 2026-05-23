package secureauth.config;

import secureauth.controller.AuthController;
import secureauth.dao.OwnerDAO;
import secureauth.dao.PetDAO;
import secureauth.repository.UserRepository;
import secureauth.repository.UserRepositoryImpl;
import secureauth.service.AuthService;
import secureauth.service.OwnerService;
import secureauth.service.PetService;
import secureauth.service.UserService;
import secureauth.service.enterprise.EnterpriseBootstrapService;
import secureauth.service.enterprise.InventoryService;
import secureauth.service.enterprise.SalesTransactionService;

/**
 * Contenedor simple de dependencias de la aplicación.
 *
 * <p>Centraliza la construcción de servicios, repositorios y controladores
 * compartidos para evitar {@code new Service()} y {@code new DAO()} dispersos
 * dentro de ventanas Swing.</p>
 */
public final class AppContext {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;
    private final AuthController authController;
    private final PetService petService;
    private final OwnerService ownerService;
    private final OwnerDAO ownerDAO;
    private final PetDAO petDAO;
    private final InventoryService inventoryService;
    private final SalesTransactionService salesTransactionService;
    private final EnterpriseBootstrapService enterpriseBootstrapService;

    public AppContext() {
        this.userRepository = new UserRepositoryImpl();
        this.authService = new AuthService(userRepository);
        this.userService = new UserService(userRepository);
        this.authController = new AuthController(authService);
        this.petDAO = new PetDAO();
        this.ownerDAO = new OwnerDAO();
        this.petService = new PetService(petDAO);
        this.ownerService = new OwnerService(ownerDAO);
        this.inventoryService = new InventoryService();
        this.salesTransactionService = new SalesTransactionService();
        this.enterpriseBootstrapService = new EnterpriseBootstrapService();
    }

    public void initialize() {
        enterpriseBootstrapService.initialize();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public AuthController getAuthController() {
        return authController;
    }

    public PetService getPetService() {
        return petService;
    }

    public OwnerService getOwnerService() {
        return ownerService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public SalesTransactionService getSalesTransactionService() {
        return salesTransactionService;
    }
}
