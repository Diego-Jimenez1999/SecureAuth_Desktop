package secureauth.ui.config;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Configuración visual y operativa persistida en TXT local. */
public final class ApplicationVisualSettings {

    public static final Path CONFIG_PATH = Path.of("secureauth.config.txt");

    public static final String DEFAULT_BRANDING = "SecureAuth";
    public static final String DEFAULT_LOGOTIPO_TEXT = "SecureAuth";
    public static final String DEFAULT_PRIMARY_COLOR = "#1F2937";
    public static final String DEFAULT_SECONDARY_COLOR = "#2563EB";
    public static final String DEFAULT_TERTIARY_COLOR = "#7C3AED";
    public static final String DEFAULT_SLOGAN = "Sistema inteligente de gestión";
    public static final String DEFAULT_LOGIN_TITLE = "SecureAuth";
    public static final String DEFAULT_LOGIN_SUBTITLE = "Sistema inteligente de gestión";
    public static final double DEFAULT_TAX = 0.19d;
    public static final String DEFAULT_CURRENCY = "COP";
    public static final String DEFAULT_FORMAT = "#,##0.00";
    public static final List<String> DEFAULT_SIZES = List.of("Pequeña", "Mediana", "Grande");

    private String branding = DEFAULT_BRANDING;
    private String logotipoText = DEFAULT_LOGOTIPO_TEXT;
    private String primaryColor = DEFAULT_PRIMARY_COLOR;
    private String secondaryColor = DEFAULT_SECONDARY_COLOR;
    private String tertiaryColor = DEFAULT_TERTIARY_COLOR;
    private String slogan = DEFAULT_SLOGAN;
    private String loginTitle = DEFAULT_LOGIN_TITLE;
    private String loginSubtitle = DEFAULT_LOGIN_SUBTITLE;
    private double tax = DEFAULT_TAX;
    private String currency = DEFAULT_CURRENCY;
    private String format = DEFAULT_FORMAT;
    private List<String> sizes = DEFAULT_SIZES;
    private String mainLogoPath = "";
    private String logotipoImagePath = "";

    // --- NUEVAS PROPIEDADES (GOAL 8) ---
    // General
    private String empresaNombre = "SecureAuth Veterinarias S.A.S.";
    private String empresaDireccion = "Calle 100 #15-32";
    private String empresaCiudad = "Bogotá";
    private String empresaTelefono = "+57 300 1234567";
    private String empresaCorreo = "contacto@secureauth.com";

    // Agenda
    private String agendaApertura = "08:00";
    private String agendaCierre = "18:00";
    private int agendaIntervalo = 30; // minutos
    private int agendaDuracion = 60; // minutos
    private String agendaDiasLaborales = "Lunes,Martes,Miércoles,Jueves,Viernes,Sábado";

    // Ventas
    private double ventasIva = 19.0;
    private String ventasMoneda = "COP";
    private String ventasPagoDefecto = "Efectivo";
    private double ventasDescuentoMax = 20.0; // %

    // Inventario
    private int inventarioStockMin = 5;
    private int inventarioAlertaVencimiento = 30; // días
    private boolean inventarioAlertasAuto = true;

    // Usuarios
    private String usuariosRoles = "Administrador,Veterinario,Recepcionista";
    private String usuariosPermisos = "Lectura,Escritura,Completo";
    private int usuariosTiempoSesion = 15; // minutos

    // Sistema
    private String sistemaTema = "Oscuro";
    private String sistemaIdioma = "Español";
    private String sistemaZonaHoraria = "America/Bogota";
    private String sistemaFormatFecha = "yyyy-MM-dd";
    private String sistemaFormatHora = "HH:mm:ss";

    // Tarjetas visibles configurables
    private String visibleMetricsCards = "VENTAS_MES,SERVICIOS_POPULARES,INGRESOS_CATEGORIA,CLIENTES_NUEVOS";

    // IA / Ollama
    private String aiModel = "qwen2.5-coder:3b";
    private double aiTemp = 0.2;
    private boolean aiStream = false;
    private int aiTimeout = 10; // segundos
    private String aiContext = "Asistente clínico para una veterinaria";
    private String aiPrompt = "Eres un generador de código Java experto. Devuelve solo código limpio.";
    private boolean aiActive = true;

    public static ApplicationVisualSettings load() {
        ApplicationVisualSettings settings = new ApplicationVisualSettings();
        if (!Files.exists(CONFIG_PATH)) {
            return settings;
        }

        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException ignored) {
            return settings;
        }

        settings.branding = text(props.getProperty("branding"), DEFAULT_BRANDING);
        settings.logotipoText = text(props.getProperty("logotipo_text"), DEFAULT_LOGOTIPO_TEXT);
        settings.primaryColor = text(props.getProperty("primary_color"), DEFAULT_PRIMARY_COLOR);
        settings.secondaryColor = text(props.getProperty("secondary_color"), DEFAULT_SECONDARY_COLOR);
        settings.tertiaryColor = text(props.getProperty("tertiary_color"), DEFAULT_TERTIARY_COLOR);
        settings.slogan = text(props.getProperty("slogan"), DEFAULT_SLOGAN);
        settings.loginTitle = text(props.getProperty("login_title"), DEFAULT_LOGIN_TITLE);
        settings.loginSubtitle = text(props.getProperty("login_subtitle"), DEFAULT_LOGIN_SUBTITLE);
        settings.tax = parseDouble(props.getProperty("tax"), DEFAULT_TAX);
        settings.currency = text(props.getProperty("currency"), DEFAULT_CURRENCY);
        settings.format = text(props.getProperty("format"), DEFAULT_FORMAT);
        settings.sizes = parseSizes(props.getProperty("sizes"));
        settings.mainLogoPath = text(props.getProperty("main_logo_path"), "");
        settings.logotipoImagePath = text(props.getProperty("logotipo_image_path"), "");

        // Cargar Nuevas Propiedades
        settings.empresaNombre = text(props.getProperty("empresa_nombre"), settings.empresaNombre);
        settings.empresaDireccion = text(props.getProperty("empresa_direccion"), settings.empresaDireccion);
        settings.empresaCiudad = text(props.getProperty("empresa_ciudad"), settings.empresaCiudad);
        settings.empresaTelefono = text(props.getProperty("empresa_telefono"), settings.empresaTelefono);
        settings.empresaCorreo = text(props.getProperty("empresa_correo"), settings.empresaCorreo);

        settings.agendaApertura = text(props.getProperty("agenda_apertura"), settings.agendaApertura);
        settings.agendaCierre = text(props.getProperty("agenda_cierre"), settings.agendaCierre);
        settings.agendaIntervalo = parseInt(props.getProperty("agenda_intervalo"), settings.agendaIntervalo);
        settings.agendaDuracion = parseInt(props.getProperty("agenda_duracion"), settings.agendaDuracion);
        settings.agendaDiasLaborales = text(props.getProperty("agenda_dias_laborales"), settings.agendaDiasLaborales);

        settings.ventasIva = parseDouble(props.getProperty("ventas_iva"), settings.ventasIva);
        settings.ventasMoneda = text(props.getProperty("ventas_moneda"), settings.ventasMoneda);
        settings.ventasPagoDefecto = text(props.getProperty("ventas_pago_defecto"), settings.ventasPagoDefecto);
        settings.ventasDescuentoMax = parseDouble(props.getProperty("ventas_descuento_max"), settings.ventasDescuentoMax);

        settings.inventarioStockMin = parseInt(props.getProperty("inventario_stock_min"), settings.inventarioStockMin);
        settings.inventarioAlertaVencimiento = parseInt(props.getProperty("inventario_alerta_vencimiento"), settings.inventarioAlertaVencimiento);
        settings.inventarioAlertasAuto = parseBoolean(props.getProperty("inventario_alertas_auto"), settings.inventarioAlertasAuto);

        settings.usuariosRoles = text(props.getProperty("usuarios_roles"), settings.usuariosRoles);
        settings.usuariosPermisos = text(props.getProperty("usuarios_permisos"), settings.usuariosPermisos);
        settings.usuariosTiempoSesion = parseInt(props.getProperty("usuarios_tiempo_sesion"), settings.usuariosTiempoSesion);

        settings.sistemaTema = text(props.getProperty("sistema_tema"), settings.sistemaTema);
        settings.sistemaIdioma = text(props.getProperty("sistema_idioma"), settings.sistemaIdioma);
        settings.sistemaZonaHoraria = text(props.getProperty("sistema_zona_horaria"), settings.sistemaZonaHoraria);
        settings.sistemaFormatFecha = text(props.getProperty("sistema_format_fecha"), settings.sistemaFormatFecha);
        settings.sistemaFormatHora = text(props.getProperty("sistema_format_hora"), settings.sistemaFormatHora);
        settings.visibleMetricsCards = text(props.getProperty("visible_metrics_cards"), settings.visibleMetricsCards);

        settings.aiModel = text(props.getProperty("ai_model"), settings.aiModel);
        settings.aiTemp = parseDouble(props.getProperty("ai_temp"), settings.aiTemp);
        settings.aiStream = parseBoolean(props.getProperty("ai_stream"), settings.aiStream);
        settings.aiTimeout = parseInt(props.getProperty("ai_timeout"), settings.aiTimeout);
        settings.aiContext = text(props.getProperty("ai_context"), settings.aiContext);
        settings.aiPrompt = text(props.getProperty("ai_prompt"), settings.aiPrompt);
        settings.aiActive = parseBoolean(props.getProperty("ai_active"), settings.aiActive);

        return settings;
    }

    public void save() throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("# SecureAuth Desktop - Configuracion visual y operativa\n");
        content.append("# Archivo TXT local. No depende de base de datos.\n\n");
        append(content, "branding", branding);
        append(content, "logotipo_text", logotipoText);
        append(content, "primary_color", primaryColor);
        append(content, "secondary_color", secondaryColor);
        append(content, "tertiary_color", tertiaryColor);
        append(content, "slogan", slogan);
        append(content, "login_title", loginTitle);
        append(content, "login_subtitle", loginSubtitle);
        append(content, "tax", String.valueOf(tax));
        append(content, "currency", currency);
        append(content, "format", format);
        append(content, "sizes", String.join(",", sizes));
        append(content, "main_logo_path", mainLogoPath);
        append(content, "logotipo_image_path", logotipoImagePath);

        // Guardar Nuevas Propiedades
        append(content, "empresa_nombre", empresaNombre);
        append(content, "empresa_direccion", empresaDireccion);
        append(content, "empresa_ciudad", empresaCiudad);
        append(content, "empresa_telefono", empresaTelefono);
        append(content, "empresa_correo", empresaCorreo);

        append(content, "agenda_apertura", agendaApertura);
        append(content, "agenda_cierre", agendaCierre);
        append(content, "agenda_intervalo", String.valueOf(agendaIntervalo));
        append(content, "agenda_duracion", String.valueOf(agendaDuracion));
        append(content, "agenda_dias_laborales", agendaDiasLaborales);

        append(content, "ventas_iva", String.valueOf(ventasIva));
        append(content, "ventas_moneda", ventasMoneda);
        append(content, "ventas_pago_defecto", ventasPagoDefecto);
        append(content, "ventas_descuento_max", String.valueOf(ventasDescuentoMax));

        append(content, "inventario_stock_min", String.valueOf(inventarioStockMin));
        append(content, "inventario_alerta_vencimiento", String.valueOf(inventarioAlertaVencimiento));
        append(content, "inventario_alertas_auto", String.valueOf(inventarioAlertasAuto));

        append(content, "usuarios_roles", usuariosRoles);
        append(content, "usuarios_permisos", usuariosPermisos);
        append(content, "usuarios_tiempo_sesion", String.valueOf(usuariosTiempoSesion));

        append(content, "sistema_tema", sistemaTema);
        append(content, "sistema_idioma", sistemaIdioma);
        append(content, "sistema_zona_horaria", sistemaZonaHoraria);
        append(content, "sistema_format_fecha", sistemaFormatFecha);
        append(content, "sistema_format_hora", sistemaFormatHora);
        append(content, "visible_metrics_cards", visibleMetricsCards);

        append(content, "ai_model", aiModel);
        append(content, "ai_temp", String.valueOf(aiTemp));
        append(content, "ai_stream", String.valueOf(aiStream));
        append(content, "ai_timeout", String.valueOf(aiTimeout));
        append(content, "ai_context", aiContext);
        append(content, "ai_prompt", aiPrompt);
        append(content, "ai_active", String.valueOf(aiActive));

        Files.writeString(CONFIG_PATH, content.toString(), StandardCharsets.UTF_8);
    }

    public static Color parseColor(String value, Color fallback) {
        String hex = text(value, "");
        if (hex.isEmpty()) {
            return fallback;
        }
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static String text(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static void append(StringBuilder content, String key, String value) {
        content.append(key).append("=").append(value == null ? "" : value.trim()).append("\n");
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(text(value, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(text(value, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static List<String> parseSizes(String value) {
        List<String> parsed = Arrays.stream(text(value, "").split(","))
                .map(String::trim)
                .filter(size -> !size.isEmpty())
                .toList();
        return parsed.isEmpty() ? DEFAULT_SIZES : parsed;
    }

    public String getBranding() { return branding; }
    public void setBranding(String branding) { this.branding = branding; }
    public String getLogotipoText() { return logotipoText; }
    public void setLogotipoText(String logotipoText) { this.logotipoText = logotipoText; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public String getTertiaryColor() { return tertiaryColor; }
    public void setTertiaryColor(String tertiaryColor) { this.tertiaryColor = tertiaryColor; }
    public String getSlogan() { return slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    public String getLoginTitle() { return loginTitle; }
    public void setLoginTitle(String loginTitle) { this.loginTitle = loginTitle; }
    public String getLoginSubtitle() { return loginSubtitle; }
    public void setLoginSubtitle(String loginSubtitle) { this.loginSubtitle = loginSubtitle; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes == null || sizes.isEmpty() ? DEFAULT_SIZES : List.copyOf(sizes); }
    public String getMainLogoPath() { return mainLogoPath; }
    public void setMainLogoPath(String mainLogoPath) { this.mainLogoPath = mainLogoPath; }
    public String getLogotipoImagePath() { return logotipoImagePath; }
    public void setLogotipoImagePath(String logotipoImagePath) { this.logotipoImagePath = logotipoImagePath; }

    // Getters y Setters para las Nuevas Propiedades
    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }
    public String getEmpresaDireccion() { return empresaDireccion; }
    public void setEmpresaDireccion(String empresaDireccion) { this.empresaDireccion = empresaDireccion; }
    public String getEmpresaCiudad() { return empresaCiudad; }
    public void setEmpresaCiudad(String empresaCiudad) { this.empresaCiudad = empresaCiudad; }
    public String getEmpresaTelefono() { return empresaTelefono; }
    public void setEmpresaTelefono(String empresaTelefono) { this.empresaTelefono = empresaTelefono; }
    public String getEmpresaCorreo() { return empresaCorreo; }
    public void setEmpresaCorreo(String empresaCorreo) { this.empresaCorreo = empresaCorreo; }

    public String getAgendaApertura() { return agendaApertura; }
    public void setAgendaApertura(String agendaApertura) { this.agendaApertura = agendaApertura; }
    public String getAgendaCierre() { return agendaCierre; }
    public void setAgendaCierre(String agendaCierre) { this.agendaCierre = agendaCierre; }
    public int getAgendaIntervalo() { return agendaIntervalo; }
    public void setAgendaIntervalo(int agendaIntervalo) { this.agendaIntervalo = agendaIntervalo; }
    public int getAgendaDuracion() { return agendaDuracion; }
    public void setAgendaDuracion(int agendaDuracion) { this.agendaDuracion = agendaDuracion; }
    public String getAgendaDiasLaborales() { return agendaDiasLaborales; }
    public void setAgendaDiasLaborales(String agendaDiasLaborales) { this.agendaDiasLaborales = agendaDiasLaborales; }

    public double getVentasIva() { return ventasIva; }
    public void setVentasIva(double ventasIva) { this.ventasIva = ventasIva; }
    public String getVentasMoneda() { return ventasMoneda; }
    public void setVentasMoneda(String ventasMoneda) { this.ventasMoneda = ventasMoneda; }
    public String getVentasPagoDefecto() { return ventasPagoDefecto; }
    public void setVentasPagoDefecto(String ventasPagoDefecto) { this.ventasPagoDefecto = ventasPagoDefecto; }
    public double getVentasDescuentoMax() { return ventasDescuentoMax; }
    public void setVentasDescuentoMax(double ventasDescuentoMax) { this.ventasDescuentoMax = ventasDescuentoMax; }

    public int getInventarioStockMin() { return inventarioStockMin; }
    public void setInventarioStockMin(int inventarioStockMin) { this.inventarioStockMin = inventarioStockMin; }
    public int getInventarioAlertaVencimiento() { return inventarioAlertaVencimiento; }
    public void setInventarioAlertaVencimiento(int inventarioAlertaVencimiento) { this.inventarioAlertaVencimiento = inventarioAlertaVencimiento; }
    public boolean isInventarioAlertasAuto() { return inventarioAlertasAuto; }
    public void setInventarioAlertasAuto(boolean inventarioAlertasAuto) { this.inventarioAlertasAuto = inventarioAlertasAuto; }

    public String getUsuariosRoles() { return usuariosRoles; }
    public void setUsuariosRoles(String usuariosRoles) { this.usuariosRoles = usuariosRoles; }
    public String getUsuariosPermisos() { return usuariosPermisos; }
    public void setUsuariosPermisos(String usuariosPermisos) { this.usuariosPermisos = usuariosPermisos; }
    public int getUsuariosTiempoSesion() { return usuariosTiempoSesion; }
    public void setUsuariosTiempoSesion(int usuariosTiempoSesion) { this.usuariosTiempoSesion = usuariosTiempoSesion; }

    public String getSistemaTema() { return sistemaTema; }
    public void setSistemaTema(String sistemaTema) { this.sistemaTema = sistemaTema; }
    public String getSistemaIdioma() { return sistemaIdioma; }
    public void setSistemaIdioma(String sistemaIdioma) { this.sistemaIdioma = sistemaIdioma; }
    public String getSistemaZonaHoraria() { return sistemaZonaHoraria; }
    public void setSistemaZonaHoraria(String sistemaZonaHoraria) { this.sistemaZonaHoraria = sistemaZonaHoraria; }
    public String getSistemaFormatFecha() { return sistemaFormatFecha; }
    public void setSistemaFormatFecha(String sistemaFormatFecha) { this.sistemaFormatFecha = sistemaFormatFecha; }
    public String getSistemaFormatHora() { return sistemaFormatHora; }
    public void setSistemaFormatHora(String sistemaFormatHora) { this.sistemaFormatHora = sistemaFormatHora; }

    public String getVisibleMetricsCards() { return visibleMetricsCards; }
    public void setVisibleMetricsCards(String visibleMetricsCards) { this.visibleMetricsCards = visibleMetricsCards; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public double getAiTemp() { return aiTemp; }
    public void setAiTemp(double aiTemp) { this.aiTemp = aiTemp; }
    public boolean isAiStream() { return aiStream; }
    public void setAiStream(boolean aiStream) { this.aiStream = aiStream; }
    public int getAiTimeout() { return aiTimeout; }
    public void setAiTimeout(int aiTimeout) { this.aiTimeout = aiTimeout; }
    public String getAiContext() { return aiContext; }
    public void setAiContext(String aiContext) { this.aiContext = aiContext; }
    public String getAiPrompt() { return aiPrompt; }
    public void setAiPrompt(String aiPrompt) { this.aiPrompt = aiPrompt; }
    public boolean isAiActive() { return aiActive; }
    public void setAiActive(boolean aiActive) { this.aiActive = aiActive; }
}
