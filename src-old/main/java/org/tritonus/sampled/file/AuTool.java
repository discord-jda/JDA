/*
 *	AuTool.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2000,2001 by Florian Bomers <http://www.bomers.de>
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


/** Common constants and methods for handling au files.
 *
 * @author Florian Bomers
 */

public class AuTool {

	public static final int	AU_HEADER_MAGIC = 0x2e736e64;
	public static final int	AUDIO_UNKNOWN_SIZE = -1;

	// length of header in bytes
	public static final int		DATA_OFFSET = 24;

	public static final int	SND_FORMAT_UNSPECIFIED = 0;
	public static final int	SND_FORMAT_MULAW_8 = 1;
	public static final int	SND_FORMAT_LINEAR_8 = 2;
	public static final int	SND_FORMAT_LINEAR_16 = 3;
	public static final int	SND_FORMAT_LINEAR_24 = 4;
	public static final int	SND_FORMAT_LINEAR_32 = 5;
	public static final int	SND_FORMAT_FLOAT = 6;
	public static final int	SND_FORMAT_DOUBLE = 7;
	public static final int	SND_FORMAT_ADPCM_G721 = 23;
	public static final int	SND_FORMAT_ADPCM_G722 = 24;
	public static final int	SND_FORMAT_ADPCM_G723_3 = 25;
	public static final int	SND_FORMAT_ADPCM_G723_5 = 26;
	public static final int	SND_FORMAT_ALAW_8 = 27;

	public static int getFormatCode(AudioFormat format) {
		// endianness is converted in audio output stream
		// sign is converted for 8-bit files
		AudioFormat.Encoding encoding = format.getEncoding();
		int nSampleSize = format.getSampleSizeInBits();
		// $$fb 2000-08-16: check the frame size, too.
		boolean frameSizeOK=(format.getFrameSize()==AudioSystem.NOT_SPECIFIED
		                        || format.getChannels()!=AudioSystem.NOT_SPECIFIED
		                        || format.getFrameSize()==nSampleSize/8*format.getChannels());
		boolean signed = encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
		boolean unsigned = encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);

		if (encoding.equals(AudioFormat.Encoding.ULAW) && nSampleSize == 8 && frameSizeOK) {
			return SND_FORMAT_MULAW_8;
		} else if (nSampleSize == 8 && frameSizeOK && (signed || unsigned)) {
			// support signed and unsigned PCM for 8 bit
			return SND_FORMAT_LINEAR_8;
		} else if (signed && frameSizeOK) {
			// support only signed PCM for > 8 bit
			if (nSampleSize == 16) {
				return SND_FORMAT_LINEAR_16;
			} else if (nSampleSize == 24) {
				return SND_FORMAT_LINEAR_24;
			} else if (nSampleSize == 32) {
				return SND_FORMAT_LINEAR_32;
			}
		} else if (encoding.equals(AudioFormat.Encoding.ALAW) && nSampleSize == 8 && frameSizeOK) {
			return SND_FORMAT_ALAW_8;
		}
		return SND_FORMAT_UNSPECIFIED;
	}
}

/*** AuTool.java ***/
