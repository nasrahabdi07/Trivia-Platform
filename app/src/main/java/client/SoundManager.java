package client;

public class SoundManager {
    private static SoundManager instance;

    private boolean soundEnabled = true;

    private SoundManager() {
        try {
            // Using simple beep tones - you can replace with actual sound files
            // For now, we'll use JavaFX Toolkit beep or create simple tones
            // Note: In a real app, you'd load .wav or .mp3 files from resources
        } catch (Exception e) {
            System.err.println("Failed to load sounds: " + e.getMessage());
        }
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playCorrect() {
        if (soundEnabled) {
            // Play a pleasant "ding" sound
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    public void playWrong() {
        if (soundEnabled) {
            // Play a "buzz" sound
            new Thread(() -> {
                try {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(100);
                    java.awt.Toolkit.getDefaultToolkit().beep();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();
        }
    }

    public void playTick() {
        if (soundEnabled) {
            // Subtle tick sound
            new Thread(() -> {
                try {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) {
                    // Ignore
                }
            }).start();
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
