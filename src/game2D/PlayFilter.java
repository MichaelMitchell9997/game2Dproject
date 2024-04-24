package game2D;

import java.io.*;
import javax.sound.sampled.*;

/**
 * The type Play filter.
 */
public class PlayFilter extends Thread{

    /**
     * The Filename.
     */
    String filename;

    /**
     * Instantiates a new Play filter.
     *
     * @param fname the fname
     */
    public PlayFilter(String fname) {
        filename = fname;

    }
    public void run()
    {
        try {
            File file = new File(filename);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat	format = stream.getFormat();
            EchoFilterStream filtered = new EchoFilterStream(stream);
            AudioInputStream f = new AudioInputStream(filtered,format,stream.getFrameLength());
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            Clip clip = (Clip)AudioSystem.getLine(info);
            clip.open(f);
            clip.start();
            Thread.sleep(100);
            while (clip.isRunning()) { Thread.sleep(100); }
            clip.close();
        }
        catch (Exception e) { 	}

    }
}
