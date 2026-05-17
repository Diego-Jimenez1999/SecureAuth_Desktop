package secureauth.shared.session;

import secureauth.model.User;
import secureauth.model.branding.BrandingSnapshot;

/** Sesión global de la aplicación ERP/POS. */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private int currentBusinessId = 1;
    private int currentBranchId = 1;
    private BrandingSnapshot currentBranding;

    private SessionManager() { }

    public static SessionManager getInstance() { return INSTANCE; }

    public synchronized User getCurrentUser() { return currentUser; }
    public synchronized int getCurrentBusinessId() { return currentBusinessId; }
    public synchronized int getCurrentBranchId() { return currentBranchId; }
    public synchronized BrandingSnapshot getCurrentBranding() { return currentBranding; }

    public synchronized void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public synchronized void setCurrentBusinessId(int currentBusinessId) { this.currentBusinessId = currentBusinessId; }
    public synchronized void setCurrentBranchId(int currentBranchId) { this.currentBranchId = currentBranchId; }
    public synchronized void setCurrentBranding(BrandingSnapshot currentBranding) { this.currentBranding = currentBranding; }

    public synchronized void clear() {
        currentUser = null;
        currentBusinessId = 1;
        currentBranchId = 1;
        currentBranding = null;
    }
}
