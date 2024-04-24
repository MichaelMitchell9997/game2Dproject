package game2D;

import java.io.*;
import javax.sound.sampled.*;

public class SoundLoop extends Thread {

    String filename;    // The name of the file to play
    boolean finished;    // A flag showing that the thread has finished

    private Clip clip;
    public FloatControl volumeControl; // Volume control

    public SoundLoop(String fname) {
        filename = fname;
        finished = false;
    }

    public void run() {
        try {
            File file = new File(filename);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            clip.loop(Clip.LOOP_CONTINUOUSLY);  // Set the clip to loop continuously

            synchronized (this) {
                while (!finished) {
                    wait();  // Wait until notified to stop
                }
            }
            clip.stop();
            clip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Call this method to stop the sound playback
    public synchronized void stopSound() {
        finished = true;
        notify();  // Notify the thread to stop waiting
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            // Ensure the volume parameter is between 0.0 and 1.0
            volume = Math.max(0.0f, Math.min(volume, 1.0f));
            // Convert linear volume scale (0.0 - 1.0) to decibels (dB)
            float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
        }
    }
}
