/*
 *	AuAudioOutputStream.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2000,2001 by Florian Bomers <http://www.bomers.de>
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

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;



/**
 * AudioOutputStream for AU files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */

public class AuAudioOutputStream extends TAudioOutputStream {

	private static String description="Created by Tritonus";

	/**
	* Writes a null-terminated ascii string s to f.
	* The total number of bytes written is aligned on a 2byte boundary.
	* @param dos data output stream
	* @param s string to write
	* @exception IOException Write error.
	*/
	protected static void writeText(TDataOutputStream dos, String s) throws IOException {
		if (s.length()>0) {
			dos.writeBytes(s);
			dos.writeByte(0);  // pour terminer le texte
			if ((s.length() % 2)==0) {
				// ajout d'un zero pour faire la longeur pair
				dos.writeByte(0);
			}
		}
	}

	/**
	* Returns number of bytes that have to written for string s (with alignment)
	* @param s string to find the length of with alignment
	* @return number of bytes for string with alignment
	*/
	protected static int getTextLength(String s) {
		if (s.length()==0) {
			return 0;
		} else {
			return (s.length()+2) & 0xFFFFFFFE;
		}
	}

	public AuAudioOutputStream(AudioFormat audioFormat,
	                           long lLength,
	                           TDataOutputStream dataOutputStream) {
		// always do backpatching if the stream supports seeking, in case the 
		// reported stream length is longer than the actual data
		// if length exceeds 2GB, set the length field to NOT_SPECIFIED
		super(audioFormat,
		      lLength>0x7FFFFFFFl?AudioSystem.NOT_SPECIFIED:lLength,
		      dataOutputStream,
		      dataOutputStream.supportsSeek());
		// double-check that we can write this audio format
		if (AuTool.getFormatCode(audioFormat) == AuTool.SND_FORMAT_UNSPECIFIED) {
			throw new IllegalArgumentException("Unknown encoding/format for AU file: "+audioFormat);
		}
		// AU requires signed 8-bit data
		requireSign8bit(true);
		// AU requires big endian
		requireEndianness(true);
		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("Writing AU: " + audioFormat.getSampleSizeInBits()
					+ " bits, " + audioFormat.getEncoding());
		}
	}

	protected void writeHeader() throws IOException {
		if (TDebug.TraceAudioOutputStream) {
			TDebug.out("AuAudioOutputStream.writeHeader(): called.");
		}
		AudioFormat		format = getFormat();
		long			lLength = getLength();
		TDataOutputStream	dos = getDataOutputStream();
		if (TDebug.TraceAudioOutputStream) {
		    TDebug.out("AuAudioOutputStream.writeHeader(): AudioFormat: " + format);
		    TDebug.out("AuAudioOutputStream.writeHeader(): length: " + lLength);
		}

		dos.writeInt(AuTool.AU_HEADER_MAGIC);
		dos.writeInt(AuTool.DATA_OFFSET+getTextLength(description));
		dos.writeInt((lLength!=AudioSystem.NOT_SPECIFIED)?((int) lLength):AuTool.AUDIO_UNKNOWN_SIZE);
		dos.writeInt(AuTool.getFormatCode(format));
		dos.writeInt((int) format.getSampleRate());
		dos.writeInt(format.getChannels());
		writeText(dos, description);
	}

	protected void patchHeader() throws IOException {
		TDataOutputStream	tdos = getDataOutputStream();
		tdos.seek(0);
		setLengthFromCalculatedLength();
		writeHeader();
	}
}

/*** AuAudioOutputStream.java ***/
