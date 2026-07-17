package secureauth.config;

import secureauth.controller.AuthController;
import secureauth.dao.OwnerDAO;
import secureauth.dao.PetDAO;
import secureauth.dao.UserDAO;
import secureauth.dao.enterprise.ActividadRecienteDAO;
import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.dao.enterprise.EnterpriseBootstrapDAO;
import secureauth.dao.enterprise.InventoryDAO;
import secureauth.dao.enterprise.SalesTransactionDAO;
import secureauth.repository.UserRepository;
import secureauth.repository.UserRepositoryImpl;
import secureauth.service.AuthService;
import secureauth.service.OwnerService;
import secureauth.service.PetService;
import secureauth.service.UserService;
import secureauth.service.enterprise.ActividadRecienteService;
import secureauth.service.enterprise.AppointmentService;
import secureauth.service.enterprise.EnterpriseContext;
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

    private final UserDAO userDAO;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;
    private final AuthController authController;
    private final PetService petService;
    private final OwnerService ownerService;
    private final OwnerDAO ownerDAO;
    private final PetDAO petDAO;
    private final InventoryDAO inventoryDAO;
    private final SalesTransactionDAO salesTransactionDAO;
    private final ActividadRecienteDAO actividadRecienteDAO;
    private final AppointmentDAO appointmentDAO;
    private final EnterpriseBootstrapDAO enterpriseBootstrapDAO;
    private final EnterpriseContext enterpriseContext;
    private final InventoryService inventoryService;
    private final SalesTransactionService salesTransactionService;
    private final ActividadRecienteService actividadRecienteService;
    private final AppointmentService appointmentService;
    private final EnterpriseBootstrapService enterpriseBootstrapService;

    public AppContext() {
        this.userDAO = new UserDAO();
        this.userRepository = new UserRepositoryImpl(userDAO);
        this.authService = new AuthService(userRepository);
        this.userService = new UserService(userRepository, userDAO);
        this.authController = new AuthController(authService);
        this.petDAO = new PetDAO();
        this.ownerDAO = new OwnerDAO();
        this.petService = new PetService(petDAO);
        this.ownerService = new OwnerService(ownerDAO);
        this.enterpriseContext = EnterpriseContext.getInstance();
        this.inventoryDAO = new InventoryDAO();
        this.salesTransactionDAO = new SalesTransactionDAO();
        this.actividadRecienteDAO = new ActividadRecienteDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.enterpriseBootstrapDAO = new EnterpriseBootstrapDAO();
        this.inventoryService = new InventoryService(inventoryDAO);
        this.salesTransactionService = new SalesTransactionService(
                salesTransactionDAO,
                inventoryDAO,
                actividadRecienteDAO,
                appointmentDAO);
        this.actividadRecienteService = new ActividadRecienteService(actividadRecienteDAO);
        this.appointmentService = new AppointmentService(appointmentDAO, petDAO, actividadRecienteDAO);
        this.enterpriseBootstrapService = new EnterpriseBootstrapService(enterpriseBootstrapDAO, enterpriseContext);
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

    public ActividadRecienteService getActividadRecienteService() {
        return actividadRecienteService;
    }

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }
}
