package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import secureauth.config.DatabaseConnection;

/** DAO de branding dinámico basado en tabla branding_config. */
public class BrandingDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS branding_config (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_id INT NOT NULL,
                    nombre_marca VARCHAR(150),
                    slogan VARCHAR(255),
                    primary_color VARCHAR(20),
                    secondary_color VARCHAR(20),
                    tertiary_color VARCHAR(20),
                    font_title VARCHAR(100),
                    font_subtitle VARCHAR(100),
                    logo_large_path VARCHAR(255),
                    logo_medium_path VARCHAR(255),
                    login_banner_path VARCHAR(255),
                    login_title VARCHAR(150),
                    login_subtitle VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);
        }
    }

    public BrandingData findByBusinessId(int businessId) throws SQLException {
        String sql = "SELECT primary_color, secondary_color, tertiary_color, logo_medium_path, login_banner_path, login_title, nombre_marca, slogan, font_title, font_subtitle, logo_large_path, login_subtitle FROM branding_config WHERE business_id=? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BrandingData(
                            rs.getString("primary_color"),
                            rs.getString("secondary_color"),
                            rs.getString("tertiary_color"),
                            rs.getString("logo_medium_path"),
                            rs.getString("login_banner_path"),
                            rs.getString("login_title"),
                            rs.getString("nombre_marca"),
                            rs.getString("slogan"),
                            rs.getString("font_title"),
                            rs.getString("font_subtitle"),
                            rs.getString("logo_large_path"),
                            rs.getString("login_subtitle")
                    );
                }
            }
        }
        return new BrandingData(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public void upsertBranding(int businessId, BrandingData data) throws SQLException {
        String exists = "SELECT id FROM branding_config WHERE business_id=? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(exists)) {
            ps.setInt(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    updateBranding(businessId, data);
                    return;
                }
            }
        }
        String insert = "INSERT INTO branding_config(business_id,nombre_marca,slogan,primary_color,secondary_color,tertiary_color,font_title,font_subtitle,logo_large_path,logo_medium_path,login_banner_path,login_title,login_subtitle) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, businessId);
            ps.setString(2, data.brandName());
            ps.setString(3, data.slogan());
            ps.setString(4, data.primary());
            ps.setString(5, data.secondary());
            ps.setString(6, data.tertiary());
            ps.setString(7, data.fontTitle());
            ps.setString(8, data.fontSubtitle());
            ps.setString(9, data.logoLargePath());
            ps.setString(10, data.logoPath());
            ps.setString(11, data.bannerPath());
            ps.setString(12, data.appTitle());
            ps.setString(13, data.loginSubtitle());
            ps.executeUpdate();
        }
    }

    private void updateBranding(int businessId, BrandingData data) throws SQLException {
        String sql = "UPDATE branding_config SET nombre_marca=?, slogan=?, primary_color=?, secondary_color=?, tertiary_color=?, font_title=?, font_subtitle=?, logo_large_path=?, logo_medium_path=?, login_banner_path=?, login_title=?, login_subtitle=? WHERE business_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.brandName());
            ps.setString(2, data.slogan());
            ps.setString(3, data.primary());
            ps.setString(4, data.secondary());
            ps.setString(5, data.tertiary());
            ps.setString(6, data.fontTitle());
            ps.setString(7, data.fontSubtitle());
            ps.setString(8, data.logoLargePath());
            ps.setString(9, data.logoPath());
            ps.setString(10, data.bannerPath());
            ps.setString(11, data.appTitle());
            ps.setString(12, data.loginSubtitle());
            ps.setInt(13, businessId);
            ps.executeUpdate();
        }
    }

    public record BrandingData(String primary, String secondary, String tertiary, String logoPath, String bannerPath,
                               String appTitle, String brandName, String slogan, String fontTitle,
                               String fontSubtitle, String logoLargePath, String loginSubtitle) { }
}
