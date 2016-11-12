/*
 *	LawEncoder.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2007 by Florian Bomers <http://www.bomers.de>
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

package org.tritonus.sampled.convert;

import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import static javax.sound.sampled.AudioFormat.Encoding.*;

import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
import org.tritonus.share.sampled.convert.TSynchronousFilteredAudioInputStream;

/**
 * This provider supports these conversions:
 * <ul>
 * <li>PCM 8 Signed to ulaw or alaw
 * <li>PCM 8 Unsigned to ulaw or alaw
 * <li>PCM 16 signed big endian to ulaw or alaw
 * <li>PCM 16 signed little endian to ulaw or alaw
 * <li>alaw to ulaw
 * <li>ulaw to alaw
 * </ul>
 * <p>
 * FrameRate, SampleRate, Channels CANNOT be converted.
 * <p>
 * This new provider replaces UlawFormatConversionProvider and AlawFormatConversionProvider. 
 *
 * @author Florian Bomers
 */
public class LawEncoder extends TEncodingFormatConversionProvider {

	// abbreviations, also used in LawDecoder
    // $$MP: move this definition to base class TFormatConversionProvider
	static final int ALL = AudioSystem.NOT_SPECIFIED;

	static final AudioFormat[] LAW_FORMATS = {
			new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, true),
	};

	private static final AudioFormat[] INPUT_FORMATS = {
			new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, false),
			new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, true),
			new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, true),
	};

	private static final AudioFormat[] OUTPUT_FORMATS = LAW_FORMATS;

	/**
	 * Constructor.
	 */
	public LawEncoder() {
		super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS));
	}

	@Override
	public AudioInputStream getAudioInputStream(AudioFormat targetFormat,
			AudioInputStream sourceStream) {
		AudioFormat sourceFormat = sourceStream.getFormat();
		// the non-conversion case
		// TODO: does this work OK when some fields are
		// AudioSystem.NOT_SPECIFIED ?
		if (AudioFormats.matches(sourceFormat, targetFormat)) {
			return sourceStream;
		}
		if (doMatch(targetFormat.getFrameRate(), sourceFormat.getFrameRate())
				&& doMatch(targetFormat.getChannels(),
						sourceFormat.getChannels())) {
			if (doMatch(targetFormat.getSampleSizeInBits(), 8)) {
				if (targetFormat.getEncoding().equals(ULAW)) {
					// OK, the targetFormat seems fine, so we convert it to ULAW
					// let the remaining checks be done by ToUlawStream
					return new ToUlawStream(sourceStream);
				} else if (targetFormat.getEncoding().equals(ALAW)) {
					// OK, the targetFormat seems fine, so we convert it to ALAW
					// let the remaining checks be done by ToAlawStream
					return new ToAlawStream(sourceStream);
				}
			}
		}
		throw new IllegalArgumentException("format conversion not supported");
	}

	static final int UNSIGNED8 = 1;
	static final int SIGNED8 = 2;
	static final int BIG_ENDIAN16 = 3;
	static final int LITTLE_ENDIAN16 = 4;
	static final int ALAW8 = 5;
	static final int ULAW8 = 6;

	// protected boolean isSupportedFormat(AudioFormat format) {
	// return getConvertType(format)!=0;
	// }

	static int getConvertType(AudioFormat af, int unAllowed) {
		int result = 0;
		AudioFormat.Encoding encoding = af.getEncoding();
		boolean bigEndian = af.isBigEndian();
		int ssib = af.getSampleSizeInBits();
		// now set up the convert type
		if (encoding.equals(PCM_SIGNED)) {
			if (ssib == 16) {
				if (bigEndian) {
					result = BIG_ENDIAN16;
				} else {
					result = LITTLE_ENDIAN16;
				}
			} else if (ssib == 8) {
				result = SIGNED8;
			}
		} else if (encoding.equals(PCM_UNSIGNED)) {
			if (ssib == 8) {
				result = UNSIGNED8;
			}
		} else if (encoding.equals(ALAW)) {
			result = ALAW8;
		} else if (encoding.equals(ULAW)) {
			result = ULAW8;
		}
		if (result == unAllowed) {
			result = 0;
		}
		return result;
	}

	private static final AudioFormat createTargetFormat(AudioFormat src, AudioFormat.Encoding dst) {
		return new AudioFormat(dst,
			src.getSampleRate(),
			8,
			src.getChannels(),
			src.getChannels(), // sample size
			src.getSampleRate(),
			false);
	}


	class ToUlawStream extends TSynchronousFilteredAudioInputStream {
		private int convertType;

		public ToUlawStream(AudioInputStream sourceStream) {
			// transform the targetFormat so that
			// FrameRate, SampleRate, and Channels match the sourceFormat
			// we only retain encoding, samplesize and endian of targetFormat.
			super(sourceStream, createTargetFormat(sourceStream.getFormat(), ULAW));
			convertType = getConvertType(sourceStream.getFormat(), ULAW8);
			if (convertType == 0) {
				throw new IllegalArgumentException(
						"format conversion not supported");
			}
			if (sourceStream.getFormat().getSampleSizeInBits() == 8) {
				enableConvertInPlace();
			}
		}

		@Override
		protected int convert(byte[] inBuffer, byte[] outBuffer,
				int outByteOffset, int inFrameCount) {
			int sampleCount = inFrameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.pcm82ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case SIGNED8:
				TConversionTool.pcm82ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case BIG_ENDIAN16:
				TConversionTool.pcm162ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case LITTLE_ENDIAN16:
				TConversionTool.pcm162ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case ALAW8:
				TConversionTool.alaw2ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount);
				break;
			}
			return inFrameCount;
		}

		@Override
		protected void convertInPlace(byte[] buffer, int byteOffset,
				int frameCount) {
			int sampleCount = frameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.pcm82ulaw(buffer, byteOffset, sampleCount,
						false);
				break;
			case SIGNED8:
				TConversionTool.pcm82ulaw(buffer, byteOffset, sampleCount, true);
				break;
			case ALAW8:
				TConversionTool.alaw2ulaw(buffer, byteOffset, sampleCount);
				break;
			default:
				throw new RuntimeException(
						"ToUlawStream: Call to convertInPlace, but it cannot convert in place.");
			}
		}
	}

	class ToAlawStream extends TSynchronousFilteredAudioInputStream {
		private int convertType;

		public ToAlawStream(AudioInputStream sourceStream) {
			// transform the targetFormat so that
			// FrameRate, SampleRate, and Channels match the sourceFormat
			// we only retain encoding, samplesize and endian of targetFormat.
			super(sourceStream, createTargetFormat(sourceStream.getFormat(), ALAW));
			convertType = getConvertType(sourceStream.getFormat(), ALAW8);
			if (convertType == 0) {
				throw new IllegalArgumentException(
						"format conversion not supported");
			}
			if (sourceStream.getFormat().getSampleSizeInBits() == 8) {
				enableConvertInPlace();
			}
		}

		@Override
		protected int convert(byte[] inBuffer, byte[] outBuffer,
				int outByteOffset, int inFrameCount) {
			int sampleCount = inFrameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.pcm82alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case SIGNED8:
				TConversionTool.pcm82alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case BIG_ENDIAN16:
				TConversionTool.pcm162alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case LITTLE_ENDIAN16:
				TConversionTool.pcm162alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case ULAW8:
				TConversionTool.ulaw2alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount);
				break;
			}
			return inFrameCount;
		}

		@Override
		protected void convertInPlace(byte[] buffer, int byteOffset,
				int frameCount) {
			int sampleCount = frameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.pcm82alaw(buffer, byteOffset, sampleCount,
						false);
				break;
			case SIGNED8:
				TConversionTool.pcm82alaw(buffer, byteOffset, sampleCount, true);
				break;
			case ULAW8:
				TConversionTool.ulaw2alaw(buffer, byteOffset, sampleCount);
				break;
			default:
				throw new RuntimeException(
						"ToAlawStream: Call to convertInPlace, but it cannot convert in place.");
			}
		}
	}

}
