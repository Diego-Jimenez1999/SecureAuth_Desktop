package secureauth.ui.components.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import secureauth.config.AppContext;
import secureauth.config.DatabaseConnection;
import secureauth.service.enterprise.EnterpriseContext;

public final class SalesByBranchCard implements DashboardCard {
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    @Override
    public String getId() {
        return "sales_by_branch";
    }

    @Override
    public String getDefaultTitle() {
        return "Ventas por Sucursal";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10104.png";
    }

    @Override
    public boolean isSummaryCard() {
        return false;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        var context = EnterpriseContext.getInstance();
        int businessId = context.getActiveBusinessId();
        int branchId = context.getActiveBranchId();

        String branchName = "Sucursal " + branchId;
        String sql = "SELECT branch_name FROM branches WHERE business_id = ? AND id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    branchName = rs.getString("branch_name");
                }
            }
        } catch (Exception ignored) {}

        var stats = appContext.getSalesTransactionService().loadStats();
        return branchName + ": " + currency.format(stats.salesMonth());
    }
}
