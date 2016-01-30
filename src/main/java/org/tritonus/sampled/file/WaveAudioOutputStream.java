/*
 *	WaveAudioOutputStream.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2000 by Florian Bomers <http://www.bomers.de>
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package org.tritonus.sampled.file;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;

/**
 * AudioOutputStream for Wave files.
 *
 * @author Florian Bomers
 */

public class WaveAudioOutputStream extends TAudioOutputStream {

	// this constant is used for chunk lengths when the length is not known yet
	private static final int LENGTH_NOT_KNOWN=-1;

	public WaveAudioOutputStream(AudioFormat audioFormat,
	                             long lLength,
	                             TDataOutputStream dataOutputStream) {
		// always do backpatching if the stream supports seeking, in case the 
		// reported stream length is longer than the actual data
		super(audioFormat,
		      lLength,
		      dataOutputStream,
		      dataOutputStream.supportsSeek());
		// wave cannot store more than 4GB
		if (lLength != AudioSystem.NOT_SPECIFIED
		    && (lLength+WaveTool.DATA_OFFSET)>0xFFFFFFFFl) {
			if (TDebug.TraceAudioOutputStream) {
				TDebug.out("WaveAudioOutputStream: Length exceeds 4GB: "
				           +lLength+"=0x"+Long.toHexString(lLength)
				           +" with header="+(lLength+WaveTool.DATA_OFFSET)
				           +"=0x"+Long.toHexString(lLength+WaveTool.DATA_OFFSET));
			}
			throw new IllegalArgumentException("Wave files cannot be larger than 4GB.");
		}
		// double-check that we can write this audio format
		if (WaveTool.getFormatCode(getFormat()) == WaveTool.WAVE_FORMAT_UNSPECIFIED) {
			throw new IllegalArgumentException("Unknown encoding/format for WAVE file: "+audioFormat);
		}
		// WAVE requires unsigned 8-bit data
		requireSign8bit(false);
		// WAVE requires little endian
		requireEndianness(false);
		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("Writing WAVE: "+audioFormat.getSampleSizeInBits()+" bits, "+audioFormat.getEncoding());
		}
	}

	@Override
	protected void writeHeader()
	throws IOException {
		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("WaveAudioOutputStream.writeHeader()");
		}
		int formatCode = WaveTool.getFormatCode(getFormat());
		AudioFormat		format = getFormat();
		long			lLength = getLength();
		int formatChunkAdd=0;
		if (formatCode==WaveTool.WAVE_FORMAT_GSM610) {
			// space for extra fields
			formatChunkAdd+=2;
		}
		int dataOffset=WaveTool.DATA_OFFSET+formatChunkAdd;
		if (formatCode!=WaveTool.WAVE_FORMAT_PCM) {
			// space for fact chunk
			dataOffset+=4+WaveTool.CHUNK_HEADER_SIZE;
		}

		// if patching the header, and the length has not been known at first
		// writing of the header, just truncate the size fields, don't throw an exception
		if (lLength != AudioSystem.NOT_SPECIFIED
		    && lLength+dataOffset>0xFFFFFFFFL) {
			lLength=0xFFFFFFFFL-dataOffset;
		}

		// chunks must be on word-boundaries
		long lDataChunkSize = lLength+(lLength%2);
		if (lLength == AudioSystem.NOT_SPECIFIED || lDataChunkSize > 0xFFFFFFFFL) {
			lDataChunkSize = 0xFFFFFFFFL;
		}
		
		long RIFF_Size = lDataChunkSize+dataOffset-WaveTool.CHUNK_HEADER_SIZE; 
		if (lLength == AudioSystem.NOT_SPECIFIED || RIFF_Size > 0xFFFFFFFFL) {
			RIFF_Size = 0xFFFFFFFFL;
		}
		
		TDataOutputStream	dos = getDataOutputStream();

		// write RIFF container chunk
		dos.writeInt(WaveTool.WAVE_RIFF_MAGIC);
		dos.writeLittleEndian32((int) RIFF_Size);
		dos.writeInt(WaveTool.WAVE_WAVE_MAGIC);

		// write fmt_ chunk
		int formatChunkSize=WaveTool.FMT_CHUNK_SIZE+formatChunkAdd;
		short sampleSizeInBits=(short) format.getSampleSizeInBits();
		int decodedSamplesPerBlock=1;

		if (formatCode==WaveTool.WAVE_FORMAT_GSM610) {
			if (format.getFrameSize()==33) {
				decodedSamplesPerBlock=160;
			} else if (format.getFrameSize()==65) {
				decodedSamplesPerBlock=320;
			} else {
				// how to retrieve this value here ?
				decodedSamplesPerBlock=(int) (format.getFrameSize()*(320.0f/65.0f));
			}
			sampleSizeInBits=0; // MS standard
		}


		int avgBytesPerSec=((int) format.getSampleRate())/decodedSamplesPerBlock*format.getFrameSize();
		dos.writeInt(WaveTool.WAVE_FMT_MAGIC);
		dos.writeLittleEndian32(formatChunkSize);
		dos.writeLittleEndian16((short) formatCode);             // wFormatTag
		dos.writeLittleEndian16((short) format.getChannels());   // nChannels
		dos.writeLittleEndian32((int) format.getSampleRate());   // nSamplesPerSec
		dos.writeLittleEndian32(avgBytesPerSec);                 // nAvgBytesPerSec
		dos.writeLittleEndian16((short) format.getFrameSize());  // nBlockalign
		dos.writeLittleEndian16(sampleSizeInBits);               // wBitsPerSample
		dos.writeLittleEndian16((short) formatChunkAdd);         // cbSize

		if (formatCode==WaveTool.WAVE_FORMAT_GSM610) {
			dos.writeLittleEndian16((short) decodedSamplesPerBlock); // wSamplesPerBlock
		}

		// write fact chunk


		if (formatCode != WaveTool.WAVE_FORMAT_PCM) {
			// write "fact" chunk: number of samples
			// todo: add this as an attribute or property
			// in AudioOutputStream or AudioInputStream
			long samples=0;
			if (lLength != AudioSystem.NOT_SPECIFIED) {
				samples = lLength / format.getFrameSize() * decodedSamplesPerBlock;
			}
			// saturate sample count
			if (samples>0xFFFFFFFFL) {
				samples = (0xFFFFFFFFL/decodedSamplesPerBlock)*decodedSamplesPerBlock;
			}
			dos.writeInt(WaveTool.WAVE_FACT_MAGIC);
			dos.writeLittleEndian32(4);
			dos.writeLittleEndian32((int) (samples & 0xFFFFFFFF));
		}

		// write header of data chunk
		dos.writeInt(WaveTool.WAVE_DATA_MAGIC);
		dos.writeLittleEndian32((lLength != AudioSystem.NOT_SPECIFIED)?((int) lLength):LENGTH_NOT_KNOWN);
	}

	@Override
	protected void patchHeader()
	throws IOException {
		TDataOutputStream	tdos = getDataOutputStream();
		tdos.seek(0);
		setLengthFromCalculatedLength();
		writeHeader();
	}

	@Override
	public void close() throws IOException {
		long nBytesWritten=getCalculatedLength();

		if ((nBytesWritten % 2)==1) {
			if (TDebug.TraceAudioOutputStream) {
				TDebug.out("WaveOutputStream.close(): adding padding byte");
			}
			// extra byte for to align on word boundaries
			TDataOutputStream tdos = getDataOutputStream();
			tdos.writeByte(0);
			// DON'T adjust calculated length !
		}


		super.close();
	}

}

/*** WaveAudioOutputStream.java ***/
