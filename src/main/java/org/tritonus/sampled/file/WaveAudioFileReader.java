/*
 *	WaveAudioFileReader.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999,2000 by Florian Bomers <http://www.bomers.de>
 *  Copyright (c) 1999 by Matthias Pfisterer
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

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;


/**
 * Class for reading wave files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */

public class WaveAudioFileReader extends TAudioFileReader
{
	private static final int	READ_LIMIT = 1000;



	public WaveAudioFileReader()
	{
		super(READ_LIMIT);
	}



	protected void advanceChunk(DataInputStream dis, long prevLength, long prevRead)
	throws IOException {
		if (prevLength>0) {
			dis.skip(((prevLength+1) & 0xFFFFFFFE)-prevRead);
		}
	}


	protected long findChunk(DataInputStream dis, int key)
	throws UnsupportedAudioFileException, IOException {
		// $$fb 1999-12-18: we should take care that we don't exceed
		// the mark of this stream. When we exceeded the mark and
		// we notice that we don't support this wave file,
		// other potential wave file readers have no chance.
		int thisKey;
		long chunkLength=0;
		do {
			advanceChunk(dis, chunkLength, 0);
			try {
				thisKey = dis.readInt();
			} catch (IOException e)
			{
				if (TDebug.TraceAllExceptions)
				{
					TDebug.out(e);
				}
				// $$fb: when we come here, we skipped past the end of the wave file
				// without finding the chunk.
				// IMHO, this is not an IOException, as there are incarnations
				// of WAVE files which store data in different chunks.
				// maybe we can find a nice description of the "required chunk" ?
				throw new UnsupportedAudioFileException(
				    "unsupported WAVE file: required chunk not found.");
			}
			chunkLength = readLittleEndianInt(dis) & 0xFFFFFFFF; // unsigned
		}
		while (thisKey != key);
		return chunkLength;
	}

	protected AudioFormat readFormatChunk(DataInputStream dis,
	                                      long chunkLength) throws UnsupportedAudioFileException, IOException {
		String debugAdd="";

		int read=WaveTool.MIN_FMT_CHUNK_LENGTH;

		if (chunkLength<WaveTool.MIN_FMT_CHUNK_LENGTH) {
			throw new UnsupportedAudioFileException(
			    "corrupt WAVE file: format chunk is too small");
		}

		short formatCode=readLittleEndianShort(dis);
		short channelCount = readLittleEndianShort(dis);
		if (channelCount <= 0) {
			throw new UnsupportedAudioFileException(
			    "corrupt WAVE file: number of channels must be positive");
		}

		int sampleRate = readLittleEndianInt(dis);
		if (sampleRate <= 0) {
			throw new UnsupportedAudioFileException(
			    "corrupt WAVE file: sample rate must be positive");
		}

		int avgBytesPerSecond=readLittleEndianInt(dis);
		int blockAlign=readLittleEndianShort(dis);

		AudioFormat.Encoding encoding;
		int sampleSizeInBits;
		int frameSize=0;
		float frameRate= sampleRate;

		int cbSize = 0;
		switch (formatCode) {
		case WaveTool.WAVE_FORMAT_PCM:
			if (chunkLength<WaveTool.MIN_FMT_CHUNK_LENGTH+2) {
				throw new UnsupportedAudioFileException(
				    "corrupt WAVE file: format chunk is too small");
			}
			sampleSizeInBits = readLittleEndianShort(dis);
			if (sampleSizeInBits <= 0) {
				throw new UnsupportedAudioFileException(
				    "corrupt WAVE file: sample size must be positive");
			}
			encoding = (sampleSizeInBits <= 8) ?
			           AudioFormat.Encoding.PCM_UNSIGNED : AudioFormat.Encoding.PCM_SIGNED;
			if (TDebug.TraceAudioFileReader) {
				debugAdd+=", wBitsPerSample="+sampleSizeInBits;
			}
			read+=2;
			break;
		case WaveTool.WAVE_FORMAT_ALAW:
			sampleSizeInBits = 8;
			encoding = AudioFormat.Encoding.ALAW;
			break;
		case WaveTool.WAVE_FORMAT_ULAW:
			sampleSizeInBits = 8;
			encoding = AudioFormat.Encoding.ULAW;
			break;
		case WaveTool.WAVE_FORMAT_GSM610:
			if (chunkLength<WaveTool.MIN_FMT_CHUNK_LENGTH+6) {
				throw new UnsupportedAudioFileException(
				    "corrupt WAVE file: extra GSM bytes are missing");
			}
			sampleSizeInBits = readLittleEndianShort(dis); // sample Size (is 0 for GSM)
			cbSize=readLittleEndianShort(dis);
			if (cbSize < 2) {
				throw new UnsupportedAudioFileException(
				    "corrupt WAVE file: extra GSM bytes are corrupt");
			}
			int decodedSamplesPerBlock=readLittleEndianShort(dis) & 0xFFFF; // unsigned
			if (TDebug.TraceAudioFileReader) {
				debugAdd+=", wBitsPerSample="+sampleSizeInBits
				          +", cbSize="+cbSize
				          +", wSamplesPerBlock="+decodedSamplesPerBlock;
			}
			sampleSizeInBits = AudioSystem.NOT_SPECIFIED;
			encoding = WaveTool.GSM0610;
			frameSize=blockAlign;
			frameRate=((float) sampleRate)/((float) decodedSamplesPerBlock);
			read+=6;
			break;

		case WaveTool.WAVE_FORMAT_IMA_ADPCM:
			if (chunkLength < WaveTool.MIN_FMT_CHUNK_LENGTH + 2)
			{
				throw new UnsupportedAudioFileException(
					"corrupt WAVE file: extra GSM bytes are missing");
			}
			sampleSizeInBits = readLittleEndianShort(dis);
			cbSize = readLittleEndianShort(dis);
			if (cbSize < 2)
			{
				throw new UnsupportedAudioFileException(
				    "corrupt WAVE file: extra IMA ADPCM bytes are corrupt");
			}
			int samplesPerBlock = readLittleEndianShort(dis) & 0xFFFF; // unsigned
			if (TDebug.TraceAudioFileReader) {
				debugAdd+=", wBitsPerSample="+sampleSizeInBits
				          +", cbSize="+cbSize
				          +", wSamplesPerBlock=" + samplesPerBlock;
			}
			sampleSizeInBits = AudioSystem.NOT_SPECIFIED;
			encoding = WaveTool.GSM0610;
			frameSize = blockAlign;
			frameRate = ((float) sampleRate)/((float) samplesPerBlock);
			read += 6;
			break;

		default:
			throw new UnsupportedAudioFileException(
			    "unsupported WAVE file: unknown format code "+formatCode);
		}
		// if frameSize isn't set, calculate it (the default)
		if (frameSize==0) {
			frameSize = calculateFrameSize(sampleSizeInBits, channelCount);
		}

		if (TDebug.TraceAudioFileReader) {
			TDebug.out("WaveAudioFileReader.readFormatChunk():");
			TDebug.out("  read values: wFormatTag="+formatCode
			           +", nChannels="+channelCount
			           +", nSamplesPerSec="+sampleRate
			           +", nAvgBytesPerSec="+avgBytesPerSecond
			           +", nBlockAlign=="+blockAlign
			           +debugAdd);
			TDebug.out("  constructed values: "
			           +"encoding="+encoding
			           +", sampleRate="+((float) sampleRate)
			           +", sampleSizeInBits="+sampleSizeInBits
			           +", channels="+channelCount
			           +", frameSize="+frameSize
			           +", frameRate="+frameRate);
		}

		// go to next chunk
		advanceChunk(dis, chunkLength, read);
		return new AudioFormat(
		           encoding,
		           sampleRate,
		           sampleSizeInBits,
		           channelCount,
		           frameSize,
		           frameRate,
		           false);
	}



	protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileLengthInBytes)
	throws UnsupportedAudioFileException, IOException {
		DataInputStream	dataInputStream = new DataInputStream(inputStream);
		int magic = dataInputStream.readInt();
		if (magic != WaveTool.WAVE_RIFF_MAGIC) {
			throw new UnsupportedAudioFileException(
			    "not a WAVE file: wrong header magic");
		}
		long totalLength = readLittleEndianInt(dataInputStream) & 0xFFFFFFFF; // unsigned
		magic = dataInputStream.readInt();
		if (magic != WaveTool.WAVE_WAVE_MAGIC) {
			throw new UnsupportedAudioFileException("not a WAVE file: wrong header magic");
		}
		// search for "fmt " chunk
		long chunkLength = findChunk(dataInputStream, WaveTool.WAVE_FMT_MAGIC);
		AudioFormat format = readFormatChunk(dataInputStream, chunkLength);

		// search for "data" chunk
		long dataChunkLength = findChunk(dataInputStream, WaveTool.WAVE_DATA_MAGIC);

		long frameLength = dataChunkLength / format.getFrameSize();

		if (TDebug.TraceAudioFileReader) {
			TDebug.out("WaveAudioFileReader.getAudioFileFormat(): total length: "
			           +totalLength+", frame length = "+frameLength);
		}
		return new TAudioFileFormat(AudioFileFormat.Type.WAVE,
		                            format,
		                            (int) frameLength,
		                            (int) (totalLength + WaveTool.CHUNK_HEADER_SIZE));
	}
}



/*** WaveAudioFileReader.java ***/

