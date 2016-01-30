/*
 *	AiffAudioFileReader.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2000 by Florian Bomers <http://www.bomers.de>
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;
import org.tritonus.share.TDebug;


/** Class for reading AIFF and AIFF-C files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class AiffAudioFileReader extends TAudioFileReader
{
	private static final int	READ_LIMIT = 1000;



	public AiffAudioFileReader()
	{
		super(READ_LIMIT);
	}



	private void skipChunk(DataInputStream dataInputStream, int chunkLength, int chunkRead)
	throws IOException {
		chunkLength-=chunkRead;
		if (chunkLength>0) {
			dataInputStream.skip(chunkLength + (chunkLength % 2));
		}
	}

	private AudioFormat readCommChunk(DataInputStream dataInputStream, int chunkLength)
	throws IOException, UnsupportedAudioFileException {

		int		nNumChannels = dataInputStream.readShort();
		if (nNumChannels <= 0) {
			throw new UnsupportedAudioFileException(
			    "not an AIFF file: number of channels must be positive");
		}
		if (TDebug.TraceAudioFileReader) {
			TDebug.out("Found "+nNumChannels+" channels.");
		}
		// ignored: frame count
		dataInputStream.readInt();
		int nSampleSize = dataInputStream.readShort();
		float fSampleRate = (float) readIeeeExtended(dataInputStream);
		if (fSampleRate <= 0.0) {
			throw new UnsupportedAudioFileException(
			    "not an AIFF file: sample rate must be positive");
		}
		if (TDebug.TraceAudioFileReader) {
			TDebug.out("Found framerate "+fSampleRate);
		}
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int nRead=18;
		if (chunkLength>nRead) {
			int nEncoding=dataInputStream.readInt();
			nRead+=4;
			if (nEncoding==AiffTool.AIFF_COMM_PCM) {
				// PCM - nothing to do
			}
			else if (nEncoding==AiffTool.AIFF_COMM_ULAW) {
				// ULAW
				encoding=AudioFormat.Encoding.ULAW;
				nSampleSize=8;
			}
			else if (nEncoding==AiffTool.AIFF_COMM_IMA_ADPCM) {
				encoding = new AudioFormat.Encoding("IMA_ADPCM");
				nSampleSize=4;
			}
			else {
				throw new UnsupportedAudioFileException(
					"Encoding 0x"+Integer.toHexString(nEncoding)
					+" of AIFF file not supported");
			}
		}
		/* In case of IMA ADPCM, frame size is 0.5 bytes (since it is
		   always mono). A value of 1 as frame size would be wrong.
		   Handling of frame size 0 in defined nowhere. So the best
		   solution is to set the frame size to unspecified (-1).
		*/
		int nFrameSize = (nSampleSize == 4) ?
			AudioSystem.NOT_SPECIFIED :
			calculateFrameSize(nSampleSize, nNumChannels);
		if (TDebug.TraceAudioFileReader) { TDebug.out("calculated frame size: " + nFrameSize); }
		skipChunk(dataInputStream, chunkLength, nRead);
		AudioFormat format = new AudioFormat(encoding,
		                                     fSampleRate,
		                                     nSampleSize,
		                                     nNumChannels,
		                                     nFrameSize,
		                                     fSampleRate,
		                                     nSampleSize > 8);
		return format;
	}

	private void readVerChunk(DataInputStream dataInputStream, int chunkLength)
	throws IOException, UnsupportedAudioFileException {
		if (chunkLength<4) {
			throw new UnsupportedAudioFileException(
			    "Corrput AIFF file: FVER chunk too small.");
		}
		int nVer=dataInputStream.readInt();
		if (nVer!=AiffTool.AIFF_FVER_TIME_STAMP) {
			throw new UnsupportedAudioFileException(
			    "Unsupported AIFF file: version not known.");
		}
		skipChunk(dataInputStream, chunkLength, 4);
	}



	protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileSizeInBytes)
	throws UnsupportedAudioFileException, IOException
	{
		if (TDebug.TraceAudioFileReader) {TDebug.out("AiffAudioFileReader.getAudioFileFormat(InputStream, long): begin"); }
		DataInputStream	dataInputStream = new DataInputStream(inputStream);
		int	nMagic = dataInputStream.readInt();
		if (nMagic != AiffTool.AIFF_FORM_MAGIC) {
			throw new UnsupportedAudioFileException(
			    "not an AIFF file: header magic is not FORM");
		}
		int nTotalLength = dataInputStream.readInt();
		nMagic = dataInputStream.readInt();
		boolean	bIsAifc;
		if (nMagic == AiffTool.AIFF_AIFF_MAGIC) {
			bIsAifc = false;
		} else if (nMagic == AiffTool.AIFF_AIFC_MAGIC) {
			bIsAifc = true;
		} else {
			throw new UnsupportedAudioFileException(
			    "unsupported IFF file: header magic neither AIFF nor AIFC");
		}
		boolean bFVerFound=!bIsAifc;
		boolean bCommFound=false;
		boolean bSSndFound=false;
		AudioFormat format=null;
		int nDataChunkLength=0;

		// walk through the chunks
		// chunks may be in any order. However, in this implementation, SSND must be last
		while (!bFVerFound || !bCommFound || !bSSndFound) {
			nMagic = dataInputStream.readInt();
			int nChunkLength = dataInputStream.readInt();
			switch (nMagic) {
			case AiffTool.AIFF_COMM_MAGIC:
				format=readCommChunk(dataInputStream, nChunkLength);
				if (TDebug.TraceAudioFileReader) {
					TDebug.out("Read COMM chunk with length "+nChunkLength);
				}
				bCommFound=true;
				break;
			case AiffTool.AIFF_FVER_MAGIC:
				if (!bFVerFound) {
					readVerChunk(dataInputStream, nChunkLength);
					if (TDebug.TraceAudioFileReader) {
						TDebug.out("Read FVER chunk with length "+nChunkLength);
					}
					bFVerFound=true;
				} else {
					skipChunk(dataInputStream, nChunkLength, 0);
				}
				break;
			case AiffTool.AIFF_SSND_MAGIC:
				if (!bCommFound || !bFVerFound) {
					throw new UnsupportedAudioFileException(
					    "cannot handle AIFF file: SSND not last chunk");
				}
				bSSndFound=true;
				nDataChunkLength=nChunkLength-8;
				// 8 information bytes of no interest
				dataInputStream.skip(8);
				if (TDebug.TraceAudioFileReader) {
					TDebug.out("Found SSND chunk with length "+nChunkLength);
				}
				break;
			default:
				if (TDebug.TraceAudioFileReader) {
					TDebug.out("Skipping unknown chunk: "
					           +Integer.toHexString(nMagic));
				}
				skipChunk(dataInputStream, nChunkLength, 0);
				break;
			}
		}

		// TODO: length argument has to be in frames
		AudioFileFormat	audioFileFormat = new TAudioFileFormat(
			bIsAifc ? AudioFileFormat.Type.AIFC : AudioFileFormat.Type.AIFF,
			format,
			nDataChunkLength / format.getFrameSize(),
			nTotalLength + 8);
		if (TDebug.TraceAudioFileReader) {TDebug.out("AiffAudioFileReader.getAudioFileFormat(InputStream, long): end"); }
		return audioFileFormat;
	}
}



/*** AiffAudioFileReader.java ***/
