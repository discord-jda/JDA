/*
 *	WaveTool.java
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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;


/**
 * Common constants and methods for handling wave files.
 *
 * @author Florian Bomers
 */

public class WaveTool {

	public static final int WAVE_RIFF_MAGIC = 0x52494646; // "RIFF"
	public static final int WAVE_WAVE_MAGIC = 0x57415645; // "WAVE"
	public static final int WAVE_FMT_MAGIC  = 0x666D7420; // "fmt "
	public static final int WAVE_DATA_MAGIC = 0x64617461; // "DATA"
	public static final int WAVE_FACT_MAGIC = 0x66616374; // "fact"

	public static final short WAVE_FORMAT_UNSPECIFIED = 0;
	public static final short WAVE_FORMAT_PCM = 1;
	public static final short WAVE_FORMAT_MS_ADPCM = 2;
	public static final short WAVE_FORMAT_ALAW = 6;
	public static final short WAVE_FORMAT_ULAW = 7;
	public static final short WAVE_FORMAT_IMA_ADPCM = 17; // same as DVI_ADPCM
	public static final short WAVE_FORMAT_G723_ADPCM = 20;
	public static final short WAVE_FORMAT_GSM610 = 49;
	public static final short WAVE_FORMAT_G721_ADPCM = 64;
	public static final short WAVE_FORMAT_MPEG = 80;

	public static final int MIN_FMT_CHUNK_LENGTH=14;
	public static final int MIN_DATA_OFFSET=12+8+MIN_FMT_CHUNK_LENGTH+8;
	public static final int MIN_FACT_CHUNK_LENGTH = 4;

	// we always write the sample size in bits and the length of extra bytes.
	// There are programs (CoolEdit) that rely on the
	// additional entry for sample size in bits.
	public static final int FMT_CHUNK_SIZE=18;
	public static final int RIFF_CONTAINER_CHUNK_SIZE=12;
	public static final int CHUNK_HEADER_SIZE=8;
	public static final int DATA_OFFSET=RIFF_CONTAINER_CHUNK_SIZE
	                                    +CHUNK_HEADER_SIZE+FMT_CHUNK_SIZE+CHUNK_HEADER_SIZE;

	public static AudioFormat.Encoding GSM0610 = new AudioFormat.Encoding("MS GSM0610");
	public static AudioFormat.Encoding IMA_ADPCM = new AudioFormat.Encoding("IMA_ADPCM");

	public static short getFormatCode(AudioFormat format) {
		// endianness is converted in audio output stream
		// sign is converted for 8-bit files
		AudioFormat.Encoding encoding = format.getEncoding();
		int nSampleSize = format.getSampleSizeInBits();
		boolean frameSizeOK = format.getFrameSize() == AudioSystem.NOT_SPECIFIED
				|| format.getChannels() != AudioSystem.NOT_SPECIFIED
				|| format.getFrameSize() == (nSampleSize + 7) / 8
						* format.getChannels();
		boolean signed = encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
		boolean unsigned = encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED);
		if (nSampleSize == 8 && frameSizeOK && (signed || unsigned)) {
			// support signed and unsigned PCM for 8 bit
			return WAVE_FORMAT_PCM;
		} else if (nSampleSize > 8 && nSampleSize <= 32 && frameSizeOK && signed) {
			// support only signed PCM for > 8 bit
			return WAVE_FORMAT_PCM;
		} else if (encoding.equals(AudioFormat.Encoding.ULAW)
				&& (nSampleSize == AudioSystem.NOT_SPECIFIED || nSampleSize == 8)
				&& frameSizeOK) {
			return WAVE_FORMAT_ULAW;
		} else if (encoding.equals(AudioFormat.Encoding.ALAW)
				&& (nSampleSize == AudioSystem.NOT_SPECIFIED || nSampleSize == 8)
				&& frameSizeOK) {
			return WAVE_FORMAT_ALAW;
		} else if (encoding.equals(new AudioFormat.Encoding("IMA_ADPCM"))
				&& nSampleSize == 4) {
			return WAVE_FORMAT_IMA_ADPCM;
		} else if (encoding.equals(GSM0610)) {
			return WAVE_FORMAT_GSM610;
		}
		return WAVE_FORMAT_UNSPECIFIED;
	}

}

/** * WaveTool.java ** */
