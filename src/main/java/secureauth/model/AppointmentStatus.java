package secureauth.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Estados operativos soportados por la agenda de servicios.
 *
 * <p>Conserva aliases legacy para leer datos ya registrados antes de la
 * normalizacion de estados.</p>
 */
public enum AppointmentStatus {

    PENDING("PENDIENTE", "Pendiente"),
    CONFIRMED("CONFIRMADA", "Confirmada"),
    IN_PROGRESS("EN_PROCESO", "En proceso"),
    FINALIZED("FINALIZADO", "Finalizado", Set.of("FINALIZADA", "REALIZADO")),
    CANCELLED("CANCELADA", "Cancelada", Set.of("CANCELADO")),
    ARCHIVED("ARCHIVADA", "Archivada", Set.of("ARCHIVED", "ARCHIVADO"));

    public static final String STATUS_PENDING = "PENDIENTE";
    public static final String STATUS_CONFIRMED = "CONFIRMADA";
    public static final String STATUS_IN_PROGRESS = "EN_PROCESO";
    public static final String STATUS_DONE = "FINALIZADO";
    public static final String STATUS_CANCELLED = "CANCELADA";
    public static final String STATUS_ARCHIVED = "ARCHIVADA";

    private final String databaseValue;
    private final String displayName;
    private final Set<String> aliases;

    AppointmentStatus(String databaseValue, String displayName) {
        this(databaseValue, displayName, Set.of());
    }

    AppointmentStatus(String databaseValue, String displayName, Set<String> aliases) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
        this.aliases = aliases;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String displayName() {
        return displayName;
    }

    public boolean matches(String value) {
        String normalized = normalize(value);
        return databaseValue.equals(normalized) || aliases.contains(normalized);
    }

    public static Optional<AppointmentStatus> fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.matches(value))
                .findFirst();
    }

    public static boolean isSupported(String value) {
        return fromDatabaseValue(value).isPresent();
    }

    public static String normalizeForStorage(String value) {
        return fromDatabaseValue(value)
                .map(AppointmentStatus::databaseValue)
                .orElse(normalize(value));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
