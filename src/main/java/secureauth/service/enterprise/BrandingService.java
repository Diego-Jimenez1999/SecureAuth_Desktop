package secureauth.service.enterprise;

import java.awt.Color;

import secureauth.dao.enterprise.BrandingDAO;
import secureauth.model.branding.BrandingSnapshot;
import secureauth.shared.session.SessionManager;
import secureauth.ui.utils.UiTheme;

/** Servicio de branding dinámico para aplicar tema por negocio activo. */
public class BrandingService {

    private final BrandingDAO dao = new BrandingDAO();
    private final EnterpriseContext context = EnterpriseContext.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public void loadAndApplyBranding() {
        try {
            dao.ensureSchema();
            BrandingDAO.BrandingData data = dao.findByBusinessId(context.getActiveBusinessId());
            UiTheme.applyDynamicBranding(
                    parseOrDefault(data.primary(), UiTheme.ACCENT_RED),
                    parseOrDefault(data.secondary(), UiTheme.DARK_PRIMARY),
                    parseOrDefault(data.tertiary(), UiTheme.BTN_DARK),
                    data.logoPath(),
                    data.bannerPath(),
                    data.appTitle());
            sessionManager.setCurrentBranding(new BrandingSnapshot(
                    data.brandName(), data.slogan(), data.primary(), data.secondary(), data.tertiary(),
                    data.fontTitle(), data.fontSubtitle(), data.logoLargePath(), data.logoPath(),
                    data.bannerPath(), data.appTitle(), data.loginSubtitle()
            ));
        } catch (Exception ignored) {
            UiTheme.applyDynamicBranding(UiTheme.ACCENT_RED, UiTheme.DARK_PRIMARY, UiTheme.BTN_DARK, null, null, null);
        }
    }

    public void saveBranding(String primary, String secondary, String tertiary, String logoPath, String bannerPath, String appTitle) throws Exception {
        dao.ensureSchema();
        dao.upsertBranding(context.getActiveBusinessId(), new BrandingDAO.BrandingData(
                primary, secondary, tertiary, logoPath, bannerPath, appTitle,
                UiTheme.APP_NAME, UiTheme.APP_SUBTITLE, UiTheme.FONT_FAMILY, UiTheme.FONT_FAMILY, logoPath, UiTheme.APP_SUBTITLE
        ));
        loadAndApplyBranding();
    }

    private Color parseOrDefault(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) {
            return fallback;
        }
        try {
            return Color.decode(hex);
        } catch (Exception ex) {
            return fallback;
        }
    }
}
