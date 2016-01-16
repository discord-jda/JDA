package voice;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.PointerByReference;
import net.dv8tion.jda.audio.AudioPacket;
import net.dv8tion.jda.audio.AudioWebSocket;
import net.tomp2p.opuswrapper.Opus;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Austin on 1/15/2016.
 */
public class OpusRewrite
{
    static
    {
        try
        {
            System.loadLibrary("opus");
        }
        catch (UnsatisfiedLinkError e1)
        {
            try
            {
                String lib = "opus/" + Platform.RESOURCE_PREFIX;
                if (lib.contains("win"))
                    lib += "/opus.dll";
                else if (lib.contains("darwin"))
                    lib += "/libopus.dylib";
                else if (lib.contains("linux"))
                    lib += "/libopus.so";
                else
                    throw new RuntimeException("We don't support audio for this operating system. Sorry!");

                System.load(OpusRewrite.class.getClassLoader().getResource(lib).getFile());
            }
            catch (Exception e2)
            {
                e1.printStackTrace();
                e2.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws LineUnavailableException
    {
        new OpusRewrite();
    }

    public OpusRewrite()
    {
//        These are all test files. Enjoy :)
//        File file = new File("mpeg-1-48000.mp3");
        File file = new File("teastep-48000.mp3");
//        File file = new File("piano2.wav");
        final int OPUS_SAMPLE_RATE = 48000; //(Hz) We want to use the highest of qualities! All the bandwidth!
        final int OPUS_FRAME_SIZE = 960;    //An opus frame size of 960 at 48000hz represents 20 milliseconds of audio.
        final int OPUS_FRAME_TIME_AMOUNT = 20; //This is 20 milliseconds. We are only dealing with 20ms opus packets.
        final int OPUS_CHANNEL_COUNT = 2;   //We want to use stereo. If the audio given is mono, the encoder promotes it
                                            // to Left and Right mono (stereo that is the same on both sides)

        IntBuffer error = IntBuffer.allocate(4);
        PointerByReference opusEncoder =
                Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
        AudioInputStream in = null;
        AudioInputStream din = null;
        try
        {
            in = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    //We want the highest possibly quality. If the original was 8 or 16 bits, it will still have that quality.
                    baseFormat.getSampleSizeInBits() != -1 ? baseFormat.getSampleSizeInBits() : 32,
                    baseFormat.getChannels(),
                    baseFormat.getFrameSize() != -1 ? baseFormat.getFrameSize() : 2 * baseFormat.getChannels(),
                    baseFormat.getFrameRate(),
                    true);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);

            char seq = 0;           //Sequence of audio packets. Used to determine the order of the packets.
            int timestamp = 0;      //Used to sync up our packets within the same timeframe of other people talking.
            long start = System.currentTimeMillis();    //Debugging. Just for checking how long this took.
            long lastFrameSent = start; //Used to make sure we only send 1 audio packet per 20 milliseconds.
                                        //Each packet contains 20ms of audio.

            /////////////////////////////////////////////
            /// needed for encoding
            //// START////
            int bytesRead = 0;
            byte[] nonEncoded = new byte[OPUS_FRAME_SIZE * decodedFormat.getFrameSize()];
//            byte[] nonEncoded = new byte[960 * decodedFormat.getFrameSize()];
            while((bytesRead = din.read(nonEncoded, 0, nonEncoded.length)) > 0)
            {
                ShortBuffer nonEncodedBuffer = ShortBuffer.allocate(bytesRead / 2);
                ByteBuffer encoded = ByteBuffer.allocate(4096);
                for (int i = 0; i < bytesRead; i += 2)
                {
                    int firstByte =  (0x000000FF & nonEncoded[i]);      //Promotes to int and handles the fact that it was unsigned.
                    int secondByte = (0x000000FF & nonEncoded[i + 1]);  //

                    //Combines the 2 bytes into a short. Opus deals with unsigned shorts, not bytes.
                    short toShort = (short) ((firstByte << 8) | secondByte);

                    nonEncodedBuffer.put(toShort);
                }
                nonEncodedBuffer.flip();

                //TODO: check for 0 / negative value for error.
                int result = Opus.INSTANCE.opus_encode(opusEncoder, nonEncodedBuffer, OPUS_FRAME_SIZE, encoded, encoded.capacity());
//                nonEncoded = new byte[960 * decodedFormat.getFrameSize()];
                nonEncoded = new byte[OPUS_FRAME_SIZE * decodedFormat.getFrameSize()];

                //ENCODING STOPS HERE

                byte[] audio = new byte[result];
                encoded.get(audio);
                AudioPacket audioPacket = new AudioPacket(seq, timestamp, AudioWebSocket.ssrc, audio);

                AudioWebSocket.udpSocket.send(audioPacket.asUdpPacket(AudioWebSocket.address));

                if (seq + 1 > Character.MAX_VALUE)
                        seq = 0;
                    else
                        seq++;

                timestamp += OPUS_FRAME_SIZE;
                //Time required to fulfill the 20 millisecond interval wait.
                long sleepTime = OPUS_FRAME_TIME_AMOUNT - (System.currentTimeMillis() - lastFrameSent);
                if (sleepTime > 0)
                {
                    Thread.sleep(sleepTime);
                }
                lastFrameSent = System.currentTimeMillis();
            }
            System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");

        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
