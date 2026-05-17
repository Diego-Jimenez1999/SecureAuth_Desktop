package secureauth.model.branding;

/** Snapshot de branding activo del negocio. */
public record BrandingSnapshot(
        String nombreMarca,
        String slogan,
        String primaryColor,
        String secondaryColor,
        String tertiaryColor,
        String fontTitle,
        String fontSubtitle,
        String logoLargePath,
        String logoMediumPath,
        String loginBannerPath,
        String loginTitle,
        String loginSubtitle
) { }
