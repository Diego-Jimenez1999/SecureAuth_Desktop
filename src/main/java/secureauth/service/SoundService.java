package secureauth.service;

import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Synthesizer;

/**
 * Servicio encargado de reproducir efectos de sonido según eventos del sistema.
 *
 * Permite activar o desactivar sonidos mediante la propiedad de configuración 'sound_enabled'.
 */
public final class SoundService {

    private static final Logger LOGGER = Logger.getLogger(SoundService.class.getName());
    private static final SoundService INSTANCE = new SoundService();

    private Synthesizer synthesizer;
    private MidiChannel midiChannel;

    private SoundService() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Midi synthesizer no disponible. Se usará beep del sistema.", e);
        }
    }

    public static SoundService getInstance() {
        return INSTANCE;
    }

    public enum SoundEvent {
        ERROR,
        WARNING,
        CONFIRMATION,
        VENTA,
        CITA,
        LOGIN,
        LOW_INVENTORY
    }

    /**
     * Reproduce el sonido correspondiente al evento indicado si los sonidos están habilitados.
     */
    public void playSound(SoundEvent event) {
        String soundEnabledStr = ConfigurationService.getInstance().getSetting("sound_enabled", "true");
        if (!Boolean.parseBoolean(soundEnabledStr)) {
            return;
        }

        new Thread(() -> {
            try {
                if (midiChannel != null) {
                    int note = resolveNoteForEvent(event);
                    int velocity = 80;
                    midiChannel.noteOn(note, velocity);
                    Thread.sleep(300);
                    midiChannel.noteOff(note);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error al reproducir sonido", e);
            }
        }).start();
    }

    private int resolveNoteForEvent(SoundEvent event) {
        return switch (event) {
            case ERROR -> 48; // Low tone
            case WARNING -> 55;
            case CONFIRMATION -> 72; // High pleasant tone
            case VENTA -> 76;
            case CITA -> 67;
            case LOGIN -> 69;
            case LOW_INVENTORY -> 60;
        };
    }
}
