/*
 *	AiffTool.java
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;


/**
 * Common constants and methods for handling aiff and aiff-c files.
 *
 * @author Florian Bomers
 */

public class AiffTool {

	public static final int	AIFF_FORM_MAGIC = 0x464F524D;
	public static final int	AIFF_AIFF_MAGIC = 0x41494646;
	public static final int	AIFF_AIFC_MAGIC = 0x41494643;
	public static final int	AIFF_COMM_MAGIC = 0x434F4D4D;
	public static final int	AIFF_SSND_MAGIC = 0x53534E44;
	public static final int	AIFF_FVER_MAGIC = 0x46564552;
	public static final int	AIFF_COMM_UNSPECIFIED = 0x00000000; // "0000"
	public static final int	AIFF_COMM_PCM   = 0x4E4F4E45;       // "NONE"
	public static final int	AIFF_COMM_ULAW  = 0x756C6177;       // "ulaw"
	public static final int	AIFF_COMM_IMA_ADPCM = 0x696D6134;   // "ima4"
	public static final int	AIFF_FVER_TIME_STAMP = 0xA2805140;  // May 23, 1990, 2:40pm

	public static int getFormatCode(AudioFormat format) {
		// endianness is converted in audio output stream
		// sign is converted for 8-bit files
		AudioFormat.Encoding encoding = format.getEncoding();
		int nSampleSize = format.getSampleSizeInBits();
		// $$fb 2000-08-16: check the frame size, too.
		boolean frameSizeOK=format.getFrameSize()==AudioSystem.NOT_SPECIFIED
		                    || format.getChannels()!=AudioSystem.NOT_SPECIFIED
		                    || format.getFrameSize()==nSampleSize/8*format.getChannels();
		boolean signed = encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
		boolean unsigned = encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
		if (nSampleSize == 8 && frameSizeOK && (signed || unsigned)) {
			// support signed and unsigned PCM for 8 bit
			return AIFF_COMM_PCM;
		} else if (nSampleSize > 8 && nSampleSize <= 32 && frameSizeOK && signed) {
			// support only signed PCM for > 8 bit
			return AIFF_COMM_PCM;
		} else if (encoding.equals(AudioFormat.Encoding.ULAW) && nSampleSize == 8 && frameSizeOK) {
			return AIFF_COMM_ULAW;
		} else if (encoding.equals(new AudioFormat.Encoding("IMA_ADPCM")) && nSampleSize == 4) {
			return AIFF_COMM_IMA_ADPCM;
		} else {
			return AIFF_COMM_UNSPECIFIED;
		}
	}

}

/*** AiffTool.java ***/
