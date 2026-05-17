package secureauth.service.enterprise;

import java.sql.SQLException;

import secureauth.dao.enterprise.SalesTransactionDAO;
import secureauth.shared.session.SessionManager;

/** Servicio de ventas POS y métricas de dashboard por sucursal. */
public class SalesTransactionService {

    private final SalesTransactionDAO dao = new SalesTransactionDAO();
    private final EnterpriseContext context = EnterpriseContext.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
    }

    public void registerSale(double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        int userId = sessionManager.getCurrentUser() != null ? sessionManager.getCurrentUser().getId() : 1;
        dao.insertTx(context.getActiveBusinessId(), context.getActiveBranchId(), userId, total, gain, tax, items, paymentMethod);
    }

    public DashboardStats loadStats() throws SQLException {
        int businessId = context.getActiveBusinessId();
        int branchId = context.getActiveBranchId();
        return new DashboardStats(
                dao.salesToday(businessId, branchId),
                dao.salesMonth(businessId, branchId),
                dao.gainMonth(businessId, branchId),
                dao.itemsMonth(businessId, branchId)
        );
    }

    public record DashboardStats(double salesToday, double salesMonth, double gainMonth, int itemsMonth) { }
}
