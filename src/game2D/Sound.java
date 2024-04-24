package game2D;

import java.io.*;
import javax.sound.sampled.*;

public class Sound extends Thread {

	String filename;    // The name of the file to play
	boolean finished;    // A flag showing that the thread has finished

	private Clip clip;
	public FloatControl volumeControl; // Add this line

	public Sound(String fname) {
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

			// Initialize the volume control here after the clip is open
			volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

			clip.start();
			Thread.sleep(100);
			while (clip.isRunning()) {
				Thread.sleep(100);
			}
			clip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finished = true;
	}


	public void setVolume(float volume) {
		if (volumeControl == null) {
			return; // Volume control not initialized, possibly because the clip hasn't been opened yet
		}
		// Ensure the volume parameter is between 0.0 and 1.0
		volume = Math.max(0.0f, Math.min(volume, 1.0f));
		// Convert linear volume scale (0.0 - 1.0) to decibels (dB)
		float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
		volumeControl.setValue(dB);
	}


}
