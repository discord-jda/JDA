/*
 *	AiffAudioOutputStream.java
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
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;


/**
 * AudioOutputStream for AIFF and AIFF-C files.
 *
 * @author Florian Bomers
 */
public class AiffAudioOutputStream extends TAudioOutputStream {

	// this constant is used for chunk lengths when the length is not known yet
	private static final int LENGTH_NOT_KNOWN=-1;

	private AudioFileFormat.Type m_FileType;

	public AiffAudioOutputStream(AudioFormat audioFormat,
	                             AudioFileFormat.Type fileType,
	                             long lLength,
	                             TDataOutputStream dataOutputStream) {
		// always do backpatching if the stream supports seeking, in case the 
		// reported stream length is longer than the actual data
		super(audioFormat,
		      lLength,
		      dataOutputStream,
		      dataOutputStream.supportsSeek());
		// AIFF files cannot exceed 2GB
		if (lLength != AudioSystem.NOT_SPECIFIED && lLength>0x7FFFFFFFl) {
			throw new IllegalArgumentException(
			    "AIFF files cannot be larger than 2GB.");
		}
		// IDEA: write AIFF file instead of AIFC when encoding=PCM ?
		m_FileType=fileType;
		if (!audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
		        && !audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
			// only AIFC files can handle non-pcm data
			m_FileType=AudioFileFormat.Type.AIFC;
		}
		// double-check that we can write this audio format
		if (AiffTool.getFormatCode(audioFormat) == AiffTool.AIFF_COMM_UNSPECIFIED) {
			throw new IllegalArgumentException("Unknown encoding/format for AIFF file: "+audioFormat);
		}
		// AIFF requires signed 8-bit data
		requireSign8bit(true);
		// AIFF requires big endian
		requireEndianness(true);

		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("Writing " + m_FileType + ": " + audioFormat.getSampleSizeInBits()
					+ " bits, " + audioFormat.getEncoding());
		}
	}

	protected void writeHeader()
	throws IOException {
		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("AiffAudioOutputStream.writeHeader(): called.");
		}
		AudioFormat format = getFormat();
		boolean	bIsAifc = m_FileType.equals(AudioFileFormat.Type.AIFC);
		long lLength = getLength();
		TDataOutputStream dos = getDataOutputStream();
		int nCommChunkSize=18;
		int nFormatCode=AiffTool.getFormatCode(format);
		if (bIsAifc) {
			// encoding takes 4 bytes
			// encoding name takes at minimum 2 bytes
			nCommChunkSize+=6;
		}
		int nHeaderSize=4          // magic
		                +8+nCommChunkSize  // COMM chunk
		                +8;                // header of SSND chunk
		if (bIsAifc) {
			// add length for FVER chunk
			nHeaderSize+=12;
		}
		// if patching the header, and the length has not been known at first
		// writing of the header, just truncate the size fields, don't throw an exception
		if (lLength != AudioSystem.NOT_SPECIFIED && lLength+nHeaderSize>0x7FFFFFFFl) {
			lLength=0x7FFFFFFFl-nHeaderSize;
		}
		// chunks must be on word-boundaries
		long lSSndChunkSize=(lLength!=AudioSystem.NOT_SPECIFIED)?
		                    (lLength+(lLength%2)+8):AudioSystem.NOT_SPECIFIED;

		// write IFF container chunk
		dos.writeInt(AiffTool.AIFF_FORM_MAGIC);
		dos.writeInt((lLength!=AudioSystem.NOT_SPECIFIED)?
		             ((int) (lSSndChunkSize+nHeaderSize)):LENGTH_NOT_KNOWN);
		if (bIsAifc) {
			dos.writeInt(AiffTool.AIFF_AIFC_MAGIC);
			// write FVER chunk
			dos.writeInt(AiffTool.AIFF_FVER_MAGIC);
			dos.writeInt(4);
			dos.writeInt(AiffTool.AIFF_FVER_TIME_STAMP);
		} else {
			dos.writeInt(AiffTool.AIFF_AIFF_MAGIC);
		}

		// write COMM chunk
		dos.writeInt(AiffTool.AIFF_COMM_MAGIC);
		dos.writeInt(nCommChunkSize);
		dos.writeShort((short) format.getChannels());
		dos.writeInt((lLength!=AudioSystem.NOT_SPECIFIED)?
		             ((int) (lLength / format.getFrameSize())):LENGTH_NOT_KNOWN);
		if (nFormatCode==AiffTool.AIFF_COMM_ULAW) {
			// AIFF ulaw states 16 bits for ulaw data
			dos.writeShort(16);
		} else {
			dos.writeShort((short) format.getSampleSizeInBits());
		}
		writeIeeeExtended(dos, format.getSampleRate());
		if (bIsAifc) {
			dos.writeInt(nFormatCode);
			dos.writeShort(0); // no encoding name
			// TODO: write encoding.toString() ??
		}

		// write header of SSND chunk
		dos.writeInt(AiffTool.AIFF_SSND_MAGIC);
		// don't use lSSndChunkSize here !
		dos.writeInt((lLength!=AudioSystem.NOT_SPECIFIED)
		             ?((int) (lLength+8)):LENGTH_NOT_KNOWN);
		// 8 information bytes of no interest
		dos.writeInt(0); // offset
		dos.writeInt(0); // blocksize
	}




	protected void patchHeader()
	throws IOException {
		TDataOutputStream	tdos = getDataOutputStream();
		tdos.seek(0);
		setLengthFromCalculatedLength();
		writeHeader();
	}

	public void close() throws IOException {
		long nBytesWritten=getCalculatedLength();

		if ((nBytesWritten % 2)==1) {
			if (TDebug.TraceAudioOutputStream) {
				TDebug.out("AiffOutputStream.close(): adding padding byte");
			}
			// extra byte for to align on word boundaries
			TDataOutputStream tdos = getDataOutputStream();
			tdos.writeByte(0);
			// DON'T adjust calculated length !
		}
		super.close();
	}

	public void writeIeeeExtended(TDataOutputStream	dos, float sampleRate) throws IOException {
		// currently, only integer sample rates are written
		// TODO: real conversion
		// I don't know exactly how much I have to shift left the mantisse for normalisation
		// now I do it so that there are any bits set in the first 5 bits
		int nSampleRate=(int) sampleRate;
		short ieeeExponent=0;
		while ((nSampleRate!=0) && (nSampleRate & 0x80000000)==0) {
			ieeeExponent++;
			nSampleRate<<=1;
		}
		dos.writeShort(16414-ieeeExponent); // exponent
		dos.writeInt(nSampleRate);          // mantisse high double word
		dos.writeInt(0);                    // mantisse low double word
	}




}

/*** AiffAudioOutputStream.java ***/
