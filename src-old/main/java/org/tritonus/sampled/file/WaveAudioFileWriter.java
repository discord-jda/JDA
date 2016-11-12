/*
 *	WaveAudioFileWriter.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999,2000 by Florian Bomers <http://www.bomers.de>
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
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;


/**
 * Class for writing Microsoft(tm) WAVE files
 *
 * @author Florian Bomers
 */
public class WaveAudioFileWriter
extends TAudioFileWriter {

	private static final AudioFileFormat.Type[]	FILE_TYPES =
	    {
	        AudioFileFormat.Type.WAVE
	    };


	// IMPORTANT: this array depends on the AudioFormat.match() algorithm which takes
	//            AudioSystem.NOT_SPECIFIED into account !
	private static final AudioFormat[]	AUDIO_FORMATS =
	    {
	        // Encoding, SampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian
	        new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, true),
	        new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, false),
	        new AudioFormat(AudioFormat.Encoding.ULAW, ALL, 8, ALL, ALL, ALL, false),
	        new AudioFormat(AudioFormat.Encoding.ULAW, ALL, 8, ALL, ALL, ALL, true),
	        new AudioFormat(AudioFormat.Encoding.ALAW, ALL, 8, ALL, ALL, ALL, false),
	        new AudioFormat(AudioFormat.Encoding.ALAW, ALL, 8, ALL, ALL, ALL, true),
	        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, ALL, 16, ALL, ALL, ALL, false),
	        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, ALL, 24, ALL, ALL, ALL, false),
	        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, ALL, 32, ALL, ALL, ALL, false),
	        new AudioFormat(WaveTool.GSM0610, ALL, ALL, ALL, ALL, ALL, false),
	        new AudioFormat(WaveTool.GSM0610, ALL, ALL, ALL, ALL, ALL, true),
	    };

	public WaveAudioFileWriter() {
		super(Arrays.asList(FILE_TYPES),
		      Arrays.asList(AUDIO_FORMATS));
	}

	// overwritten for quicker and more accurate check
	protected boolean isAudioFormatSupportedImpl(AudioFormat format,
	        AudioFileFormat.Type fileType) {
		return WaveTool.getFormatCode(format) != WaveTool.WAVE_FORMAT_UNSPECIFIED;
	}


	protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat,
	        long lLengthInBytes,
	        AudioFileFormat.Type fileType,
	        TDataOutputStream dataOutputStream)	throws IOException {
	            return new WaveAudioOutputStream(audioFormat,
	                                             lLengthInBytes,
	                                             dataOutputStream);
	        }

}

/*** WaveAudioFileWriter.java ***/
