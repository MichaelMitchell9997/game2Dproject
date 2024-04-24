package game2D;

import java.io.*;

public class EchoFilterStream extends FilterInputStream {

    EchoFilterStream(InputStream in) { super(in); }

    // Get a value from the array 'buffer' at the given 'position'
    // and convert it into short big-endian format
    public short getSample(byte[] buffer, int position)
    {
        return (short) (((buffer[position+1] & 0xff) << 8) |
                (buffer[position] & 0xff));
    }

    // Set a short value 'sample' in the array 'buffer' at the
    // given 'position' in little-endian format
    public void setSample(byte[] buffer, int position, short sample)
    {
        buffer[position] = (byte)(sample & 0xFF);
        buffer[position+1] = (byte)((sample >> 8) & 0xFF);
    }

    public int read(byte [] sample, int offset, int length) throws IOException
    {
        // Get the number of bytes in the data stream
        int 	bytesRead = super.read(sample,offset,length);
        int		p;			// Loop variable
        short 	amp = 0;	// The amplitude read from the sound sample
        short	val = 0;	// The value read from further down the sample array
        short	echoed = 0;	// The amplitude for the echoed sound

        int		delay = 100000;	// The delay for the echo (how long it takes the sound to bounce back
        int		delayed = 0;	// Position of the echoed delay in the 'sample' array

        //	Loop through the sample 2 bytes at a time
        for (p=0; p<bytesRead; p = p + 2)
        {
            // Get the value at the front of the sound buffer
            amp = getSample(sample,p);

            // Work out where to put the new echoed sound
            delayed = p + delay;
            if (delayed < bytesRead)
            {
                val = getSample(sample, delayed);
                echoed = (short)((amp + val) * 0.5);
                setSample(sample,delayed,echoed);
            }

        }
        return length;
    }
}
