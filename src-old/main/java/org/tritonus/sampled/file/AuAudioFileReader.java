/*
 *	AuAudioFileReader.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999,2000,2001 by Florian Bomers <http://www.bomers.de>
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

import java.util.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;


/** Class for reading Sun/Next AU files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class AuAudioFileReader extends TAudioFileReader
{
	private static final int	READ_LIMIT = 1000;



	public AuAudioFileReader()
	{
		super(READ_LIMIT);
	}



	private static String readDescription(DataInputStream dis, int len) throws IOException {
		byte c=-1;
		String ret="";
		while (len>0 && (c=dis.readByte())!=0) {
			ret=ret+(char) c;
			len--;
		}
		if (len>1 && c==0) {
			dis.skip(len-1);
		}
		return ret;
	}



	protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long lFileSizeInBytes)
		throws UnsupportedAudioFileException, IOException
	{
		if (TDebug.TraceAudioFileReader) {TDebug.out("AuAudioFileReader.getAudioFileFormat(InputStream, long): begin"); }
		DataInputStream	dataInputStream = new DataInputStream(inputStream);
		int	nMagic = dataInputStream.readInt();
		if (nMagic != AuTool.AU_HEADER_MAGIC) {
			throw new UnsupportedAudioFileException(
			    "not an AU file: wrong header magic");
		}
		int nDataOffset = dataInputStream.readInt();
		if (TDebug.TraceAudioFileReader) {
			TDebug.out("AuAudioFileReader.getAudioFileFormat(): data offset: " + nDataOffset);
		}
		if (nDataOffset < AuTool.DATA_OFFSET) {
			throw new UnsupportedAudioFileException(
			    "not an AU file: data offset must be 24 or greater");
		}
		int nDataLength = dataInputStream.readInt();
		if (TDebug.TraceAudioFileReader) {
			TDebug.out("AuAudioFileReader.getAudioFileFormat(): data length: " + nDataLength);
		}
		if (nDataLength < 0 && nDataLength!=AuTool.AUDIO_UNKNOWN_SIZE) {
			throw new UnsupportedAudioFileException(
			    "not an AU file: data length must be positive, 0 or -1 for unknown");
		}
		AudioFormat.Encoding encoding = null;
		int nSampleSize = 0;
		int nEncoding = dataInputStream.readInt();
		switch (nEncoding) {
		case AuTool.SND_FORMAT_MULAW_8:		// 8-bit uLaw G.711
			encoding = AudioFormat.Encoding.ULAW;
			nSampleSize = 8;
			break;

		case AuTool.SND_FORMAT_LINEAR_8:
			encoding = AudioFormat.Encoding.PCM_SIGNED;
			nSampleSize = 8;
			break;

		case AuTool.SND_FORMAT_LINEAR_16:
			encoding = AudioFormat.Encoding.PCM_SIGNED;
			nSampleSize = 16;
			break;

		case AuTool.SND_FORMAT_LINEAR_24:
			encoding = AudioFormat.Encoding.PCM_SIGNED;
			nSampleSize = 24;
			break;

		case AuTool.SND_FORMAT_LINEAR_32:
			encoding = AudioFormat.Encoding.PCM_SIGNED;
			nSampleSize = 32;
			break;

		case AuTool.SND_FORMAT_ALAW_8:	// 8-bit aLaw G.711
			encoding = AudioFormat.Encoding.ALAW;
			nSampleSize = 8;
			break;
		}
		if (nSampleSize == 0) {
			throw new UnsupportedAudioFileException(
			    "unsupported AU file: unknown encoding " + nEncoding);
		}
		int nSampleRate = dataInputStream.readInt();
		if (nSampleRate <= 0) {
			throw new UnsupportedAudioFileException(
			    "corrupt AU file: sample rate must be positive");
		}
		int nNumChannels = dataInputStream.readInt();
		if (nNumChannels <= 0) {
			throw new UnsupportedAudioFileException(
			    "corrupt AU file: number of channels must be positive");
		}
		// skip header information field
		//inputStream.skip(nDataOffset - AuTool.DATA_OFFSET);
		// read header info field
		String desc = readDescription(dataInputStream, nDataOffset - AuTool.DATA_OFFSET);
		// add the description to the file format's properties
		Map<String,Object> properties = new HashMap<String, Object>();
		if (desc!="") {
			properties.put("title", desc);
		}

		AudioFormat format = new AudioFormat(encoding,
		                                     nSampleRate,
		                                     nSampleSize,
		                                     nNumChannels,
		                                     calculateFrameSize(nSampleSize, nNumChannels),
		                                     nSampleRate,
		                                     nSampleSize > 8);
		AudioFileFormat	audioFileFormat = new TAudioFileFormat(
			AudioFileFormat.Type.AU,
			format,
			(nDataLength==AuTool.AUDIO_UNKNOWN_SIZE)?
			AudioSystem.NOT_SPECIFIED:(nDataLength / format.getFrameSize()),
			(nDataLength==AuTool.AUDIO_UNKNOWN_SIZE)?
			AudioSystem.NOT_SPECIFIED:(nDataLength + nDataOffset),
			properties);
		if (TDebug.TraceAudioFileReader) { TDebug.out("AuAudioFileReader.getAudioFileFormat(InputStream, long): begin"); }
		return audioFileFormat;
	}
}



/*** AuAudioFileReader.java ***/

