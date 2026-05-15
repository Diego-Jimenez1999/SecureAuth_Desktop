package secureauth.service.enterprise;

/** Contexto activo de negocio/sucursal para aislar catálogos, inventario y ventas. */
public final class EnterpriseContext {

    private static final EnterpriseContext INSTANCE = new EnterpriseContext();

    private int activeBusinessId = 1;
    private int activeBranchId = 1;

    private EnterpriseContext() {}

    public static EnterpriseContext getInstance() {
        return INSTANCE;
    }

    public synchronized int getActiveBusinessId() {
        return activeBusinessId;
    }

    public synchronized int getActiveBranchId() {
        return activeBranchId;
    }

    public synchronized void setActiveContext(int businessId, int branchId) {
        this.activeBusinessId = businessId;
        this.activeBranchId = branchId;
    }
}
