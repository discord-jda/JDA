package voice;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;

import net.dv8tion.jda.audio.AudioPacket;
import net.dv8tion.jda.audio.AudioWebSocket;
import net.tomp2p.opuswrapper.Opus;

public class OpusExample3 {

	static {
		try {
			System.loadLibrary("opus");
		} catch (UnsatisfiedLinkError e1) {
			try {
//				File f = Native.extractFromResourcePath("opus");
				System.load(new File("./natives/win32-x86-64/opus.dll").getCanonicalPath());
			} catch (Exception e2) {
				e1.printStackTrace();
				e2.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws LineUnavailableException {
		new OpusExample3().testCodec();
	}

	public void testCodec() throws LineUnavailableException {

		AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, true);
		ShortBuffer dataFromMic = recordFromMicrophone(format, 5000);
		List<ByteBuffer> packets = encode(dataFromMic);
        List<ByteBuffer> packets2 = new ArrayList<>();
		char seq = 0;
		int timestamp = 0;
		System.out.println(packets.size());
		for (ByteBuffer packet : packets)
		{
			byte[] audio = new byte[packet.remaining()];
			packet.get(audio);
			AudioPacket audioPacket = new AudioPacket(seq, timestamp, AudioWebSocket.ssrc, audio);

//            int i = 0;
//			for (byte b : audioPacket.getRawPacket())
//			{
//                if (i == 12)
//                    System.out.print("_");
//				System.out.print(b + ",");
//                i++;
//			}
//			System.out.println();
            try
            {
                AudioWebSocket.udpSocket.send(audioPacket.asUdpPacket(AudioWebSocket.address));
                Thread.sleep(10);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
//            catch (InterruptedException e)
//            {
//                e.printStackTrace();
//            }

            if (seq + 1 > 65535)
				seq = 0;
			else
				seq++;
			timestamp += 960;
		}



		// packets go over network
//		ShortBuffer decodedFromNetwork = decode(packets);
//		playBack(format, decodedFromNetwork);
	}

	private ShortBuffer decode(List<ByteBuffer> packets) {
		IntBuffer error = IntBuffer.allocate(4);
		PointerByReference opusDecoder = Opus.INSTANCE.opus_decoder_create(48000, 2, error);

		ShortBuffer shortBuffer = ShortBuffer.allocate(1024 * 1024);
		for (ByteBuffer dataBuffer : packets) {
			byte[] transferedBytes = new byte[dataBuffer.remaining()];
			dataBuffer.get(transferedBytes);
			int decoded = Opus.INSTANCE.opus_decode(opusDecoder, transferedBytes, transferedBytes.length, shortBuffer, 960, 0);
			shortBuffer.position(shortBuffer.position() + decoded);
		}
		shortBuffer.flip();

		Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
		return shortBuffer;
	}

	private List<ByteBuffer> encode(ShortBuffer shortBuffer) {
		IntBuffer error = IntBuffer.allocate(4);
		PointerByReference opusEncoder = Opus.INSTANCE.opus_encoder_create(48000, 2, Opus.OPUS_APPLICATION_VOIP, error);
		int read = 0;
		List<ByteBuffer> list = new ArrayList<>();
		while (shortBuffer.hasRemaining()) {
			ByteBuffer dataBuffer = ByteBuffer.allocate(4096);
			int toRead = Math.min(shortBuffer.remaining(), dataBuffer.remaining());
			/*
			 * if (frame_size==sampling_rate/400) variable_duration =
			 * OPUS_FRAMESIZE_2_5_MS; else if (frame_size==sampling_rate/200)
			 * variable_duration = OPUS_FRAMESIZE_5_MS; else if
			 * (frame_size==sampling_rate/100) variable_duration =
			 * OPUS_FRAMESIZE_10_MS; else if (frame_size==sampling_rate/50)
			 * variable_duration = OPUS_FRAMESIZE_20_MS; else if
			 * (frame_size==sampling_rate/25) variable_duration =
			 * OPUS_FRAMESIZE_40_MS;
			 */
			read = Opus.INSTANCE.opus_encode(opusEncoder, shortBuffer, 960, dataBuffer, toRead);
			// System.err.println("read: "+read);
			dataBuffer.position(dataBuffer.position() + read);
			dataBuffer.flip();
			list.add(dataBuffer);
			shortBuffer.position(shortBuffer.position() + 960);
		}
		Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
		// used for debugging
		shortBuffer.flip();
		return list;
	}

	private void playBack(AudioFormat format, ShortBuffer shortBuffer) throws LineUnavailableException {
		SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
		speaker.open(format);
		speaker.start();

		short[] shortAudioBuffer = new short[shortBuffer.remaining()];
		shortBuffer.get(shortAudioBuffer);
		byte[] audio = ShortToByte_Twiddle_Method(shortAudioBuffer);
		speaker.write(audio, 0, audio.length);
	}

	private ShortBuffer recordFromMicrophone(AudioFormat format, int lengthMillis) throws LineUnavailableException {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			throw new LineUnavailableException("not supported");
		}
		TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
		// Obtain and open the line.
		microphone.open(format);

		// Assume that the TargetDataLine, line, has already been obtained and
		// opened.

        System.out.println(microphone.getBufferSize());
		byte[] data = new byte[microphone.getBufferSize() / 5];

		// Begin audio capture.
		microphone.start();
		// probably way too big
		ShortBuffer shortBuffer = ShortBuffer.allocate(1024 * 1024);
		// Here, stopped is a global boolean set by another thread.
		long start = System.currentTimeMillis();
		int numBytesRead;
		while (System.currentTimeMillis() - start < lengthMillis) {
			// Read the next chunk of data from the TargetDataLine.
			numBytesRead = microphone.read(data, 0, data.length);
			// Save this chunk of data.
			for (int i = 0; i < numBytesRead; i += 2) {
				int b1 = data[i + 1] & 0xff;
				int b2 = data[i] << 8;
				shortBuffer.put((short) (b1 | b2));
			}
		}
		shortBuffer.flip();
		return shortBuffer;
	}

	private byte[] ShortToByte_Twiddle_Method(final short[] input) {
		final int len = input.length;
		final byte[] buffer = new byte[len * 2];
		for (int i = 0; i < len; i++) {
			buffer[(i * 2) + 1] = (byte) (input[i]);
			buffer[(i * 2)] = (byte) (input[i] >> 8);
		}
		return buffer;
	}
}
