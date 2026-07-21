package secureauth.shared.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clase de utilidad centralizada para la lógica relacionada con fechas de servicios.
 * Permite detectar si un servicio es de varios días, calcular su duración
 * automáticamente y validar el intervalo de tiempo seleccionado.
 *
 * @author Diego
 * @version 1.0
 */
public final class ServiceScheduleHelper {

    private ServiceScheduleHelper() {
    }

    /**
     * Determina si un servicio es de múltiples días según su nombre o categoría.
     *
     * @param name nombre del servicio
     * @param category categoría del servicio
     * @return true si el servicio es de múltiples días (por ejemplo: hospedaje, hospitalización, etc.)
     */
    public static boolean isMultiDayService(String name, String category) {
        if (name == null) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        String lowerCategory = category != null ? category.toLowerCase(Locale.ROOT) : "";
        return lowerName.contains("hospedaje") || lowerName.contains("hospitaliz")
                || lowerName.contains("observac") || lowerName.contains("recuperac")
                || lowerName.contains("guarder")
                || lowerCategory.contains("hospedaje") || lowerCategory.contains("hospitaliz")
                || lowerCategory.contains("observac") || lowerCategory.contains("recuperac")
                || lowerCategory.contains("guarder");
    }

    /**
     * Calcula automáticamente la duración de un servicio en base a su fecha/hora de inicio y fin.
     *
     * @param serviceName nombre del servicio para discernir el tipo de cálculo
     * @param startDate fecha de inicio
     * @param startTime hora de inicio
     * @param endDate fecha de fin
     * @param endTime hora de fin
     * @return una representación en texto de la duración del servicio (ej. "4 días" o "72 horas")
     */
    public static String calculateDurationString(String serviceName, LocalDate startDate, LocalTime startTime,
            LocalDate endDate, LocalTime endTime) {
        if (startDate == null || startTime == null || endDate == null || endTime == null) {
            return "-";
        }
        LocalDateTime start = LocalDateTime.of(startDate, startTime);
        LocalDateTime end = LocalDateTime.of(endDate, endTime);
        if (end.isBefore(start)) {
            return "Intervalo inválido";
        }

        String lower = serviceName != null ? serviceName.toLowerCase(Locale.ROOT) : "";
        boolean isDayBased = lower.contains("hospedaje") || lower.contains("guarder");

        if (isMultiDayService(serviceName, "")) {
            if (isDayBased) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                return days + " " + (days == 1 ? "día" : "días");
            } else {
                long hours = java.time.temporal.ChronoUnit.HOURS.between(start, end);
                return hours + " " + (hours == 1 ? "hora" : "horas");
            }
        } else {
            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(start, end);
            if (minutes >= 60) {
                long hours = minutes / 60;
                long remMinutes = minutes % 60;
                if (remMinutes == 0) {
                    return hours + " " + (hours == 1 ? "hora" : "horas");
                } else {
                    return hours + " " + (hours == 1 ? "hora" : "horas") + " " + remMinutes + " minutos";
                }
            }
            return minutes + " minutos";
        }
    }

    /**
     * Valida que un intervalo de tiempo sea correcto y lanza excepciones claras en caso contrario.
     *
     * @param startD fecha de inicio
     * @param startT hora de inicio
     * @param endD fecha de fin
     * @param endT hora de fin
     * @throws IllegalArgumentException si el intervalo no es válido o tiene datos incompletos
     */
    public static void validateInterval(LocalDate startD, LocalTime startT, LocalDate endD, LocalTime endT) {
        if (startD == null || startT == null || endD == null || endT == null) {
            throw new IllegalArgumentException("Completa la fecha y hora de inicio y finalización.");
        }

        LocalDateTime start = LocalDateTime.of(startD, startT);
        LocalDateTime end = LocalDateTime.of(endD, endT);

        if (endD.isBefore(startD)) {
            throw new IllegalArgumentException("La fecha final no puede ser menor a la fecha inicial.");
        }

        if (endD.equals(startD) && endT.isBefore(startT)) {
            throw new IllegalArgumentException("La hora final no puede ser menor a la hora inicial cuando ambas fechas son iguales.");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("El intervalo de tiempo no es válido (duración negativa).");
        }
    }

    /**
     * Formatea un intervalo de fechas para mostrarlo amigablemente.
     * Ej. "10 Jul → 13 Jul" o simplemente "10 Jul" si es el mismo día.
     *
     * @param start fecha de inicio
     * @param end fecha de fin
     * @return texto formateado
     */
    public static String formatInterval(LocalDate start, LocalDate end) {
        if (start == null) {
            return "-";
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM", Locale.of("es"));
        if (end == null || end.equals(start)) {
            return start.format(fmt);
        }
        return start.format(fmt) + " → " + end.format(fmt);
    }
}
