package secureauth.service.enterprise;

import java.sql.SQLException;
import java.util.List;

import secureauth.dao.enterprise.SalesTransactionDAO;
import secureauth.dao.enterprise.SalesTransactionDAO.SaleReportRow;

/** Servicio de ventas POS y métricas de dashboard por sucursal. */
public class SalesTransactionService {

    private final SalesTransactionDAO dao = new SalesTransactionDAO();
    private final EnterpriseContext context = EnterpriseContext.getInstance();

    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
    }

    public void registerSale(double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        dao.insertTx(context.getActiveBusinessId(), context.getActiveBranchId(), total, gain, tax, items, paymentMethod);
    }

    public void registerSale(double total, double gain, double tax, int items, String paymentMethod,
            String itemsSummary, String clientName, String userName) throws SQLException {
        dao.insertTx(context.getActiveBusinessId(), context.getActiveBranchId(), total, gain, tax, items, paymentMethod,
                itemsSummary, clientName, userName);
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

    public List<SaleReportRow> recentSales(int limit) throws SQLException {
        return dao.recentSales(context.getActiveBusinessId(), context.getActiveBranchId(), limit);
    }
}
