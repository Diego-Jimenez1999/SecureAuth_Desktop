package secureauth.service.enterprise;

import java.sql.SQLException;
import java.util.List;

import secureauth.dao.enterprise.EnterpriseBootstrapDAO;
import secureauth.model.enterprise.Branch;
import secureauth.model.enterprise.Business;
import secureauth.model.enterprise.BusinessType;

/** Servicio de bootstrap y consulta del contexto enterprise multi-negocio. */
public class EnterpriseBootstrapService {

    private final EnterpriseBootstrapDAO dao;
    private final EnterpriseContext context;

    public EnterpriseBootstrapService() {
        this(new EnterpriseBootstrapDAO(), EnterpriseContext.getInstance());
    }

    public EnterpriseBootstrapService(EnterpriseBootstrapDAO dao, EnterpriseContext context) {
        this.dao = dao;
        this.context = context;
    }

    /** Inicializa esquema y deja contexto activo válido. */
    public void initialize() {
        try {
            dao.ensureSchema();
            dao.seedBusinessTypes();
            int defaultBusinessId = dao.ensureDefaultBusinessAndBranch();
            List<Branch> branches = dao.findBranchesByBusiness(defaultBusinessId);
            int branchId = branches.isEmpty() ? 1 : branches.getFirst().id();
            context.setActiveContext(defaultBusinessId, branchId);
        } catch (SQLException ignored) {
            context.setActiveContext(1, 1);
        }
    }

    public List<BusinessType> getBusinessTypes() throws SQLException {
        return dao.findBusinessTypes();
    }

    public List<Business> getBusinesses() throws SQLException {
        return dao.findBusinesses();
    }

    public List<Branch> getBranches(int businessId) throws SQLException {
        return dao.findBranchesByBusiness(businessId);
    }
}
