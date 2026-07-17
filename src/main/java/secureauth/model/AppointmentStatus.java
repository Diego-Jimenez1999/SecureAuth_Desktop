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
    FINALIZED("FINALIZADA", "Finalizada", Set.of("REALIZADO")),
    CANCELLED("CANCELADA", "Cancelada", Set.of("CANCELADO"));

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
