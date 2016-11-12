/*
 *	LawDecoder.java
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import static javax.sound.sampled.AudioFormat.Encoding.*;

import static org.tritonus.sampled.convert.LawEncoder.*;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
import org.tritonus.share.sampled.convert.TSynchronousFilteredAudioInputStream;

/**
 * This provider supports these conversions:
 * <ul>
 * <li>alaw,ulaw to PCM 8 Signed
 * <li>alaw,ulaw to PCM 8 Unsigned  to alaw
 * <li>alaw,ulaw to PCM 16 signed big endian to alaw
 * <li>alaw,ulaw to PCM 16 signed little endian to alaw
 * </ul>
 * <p>
 * FrameRate, SampleRate, Channels CANNOT be converted.
 * <p>
 * This new provider replaces UlawFormatConversionProvider and AlawFormatConversionProvider. 
 *
 * @author Florian Bomers
 */
public class LawDecoder extends TEncodingFormatConversionProvider {

	private static final AudioFormat[] INPUT_FORMATS = LAW_FORMATS;

	private static final AudioFormat[] OUTPUT_FORMATS = {
			new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, false),
			new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, true),
			new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, false),
			new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, true),
	};

	/**
	 * Constructor.
	 */
	public LawDecoder() {
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
			if (doMatch(sourceFormat.getSampleSizeInBits(), 8)) {
				if (sourceFormat.getEncoding().equals(ULAW)) {
					// convert ULAW to the target format
					return new FromUlawStream(sourceStream, targetFormat);
				} else if (sourceFormat.getEncoding().equals(ALAW)) {
					// convert ALAW to the target format
					return new FromAlawStream(sourceStream, targetFormat);
				}
			}
		}
		throw new IllegalArgumentException("format conversion not supported");
	}

	// protected boolean isSupportedFormat(AudioFormat format) {
	// return getConvertType(format)!=0;
	// }

	private static final AudioFormat createTargetFormat(AudioFormat src, AudioFormat dst) {
		return new AudioFormat(dst.getEncoding(),
			src.getSampleRate(),
			dst.getSampleSizeInBits(),
			src.getChannels(),
			dst.getSampleSizeInBits() * src.getChannels() / 8,
			src.getFrameRate(),
			dst.isBigEndian());
	}

	class FromUlawStream extends TSynchronousFilteredAudioInputStream {
		private int convertType;

		public FromUlawStream(AudioInputStream sourceStream,
				AudioFormat targetFormat) {
			// transform the targetFormat so that
			// FrameRate, SampleRate, and Channels match the sourceFormat
			// we only retain encoding, samplesize and endian of targetFormat.
			super(sourceStream, createTargetFormat(sourceStream.getFormat(), targetFormat));
			convertType = getConvertType(getFormat(), ULAW8);
			if (convertType == 0) {
				throw new IllegalArgumentException("format conversion not supported");
			}
			if (targetFormat.getSampleSizeInBits() == 8) {
				enableConvertInPlace();
			}
		}

		@Override
		protected int convert(byte[] inBuffer, byte[] outBuffer,
				int outByteOffset, int inFrameCount) {
			int sampleCount = inFrameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.ulaw2pcm8(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case SIGNED8:
				TConversionTool.ulaw2pcm8(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case BIG_ENDIAN16:
				TConversionTool.ulaw2pcm16(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case LITTLE_ENDIAN16:
				TConversionTool.ulaw2pcm16(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case ALAW8:
				TConversionTool.ulaw2alaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount);
				break;
			}
			return inFrameCount;
		}

		@Override
		protected void convertInPlace(byte[] buffer, int byteOffset,
				int frameCount) {
			int sampleCount = frameCount * format.getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.ulaw2pcm8(buffer, byteOffset, sampleCount,
						false);
				break;
			case SIGNED8:
				TConversionTool.ulaw2pcm8(buffer, byteOffset, sampleCount, true);
				break;
			case ALAW8:
				TConversionTool.ulaw2alaw(buffer, byteOffset, sampleCount);
				break;
			default:
				throw new RuntimeException("FromUlawStream: Call to convertInPlace, "
					+"but it cannot convert in place. (convertType=" + convertType + ")");
			}
		}
	}

	class FromAlawStream extends TSynchronousFilteredAudioInputStream {
		private int convertType;

		public FromAlawStream(AudioInputStream sourceStream,
				AudioFormat targetFormat) {
			// transform the targetFormat so that
			// FrameRate, SampleRate, and Channels match the sourceFormat
			// we only retain encoding, samplesize and endian of targetFormat.
			super(sourceStream, createTargetFormat(sourceStream.getFormat(), targetFormat));
			convertType = getConvertType(getFormat(), ALAW8);
			if (convertType == 0) {
				throw new IllegalArgumentException(
						"format conversion not supported");
			}
			if (targetFormat.getSampleSizeInBits() == 8) {
				enableConvertInPlace();
			}
		}

		@Override
		protected int convert(byte[] inBuffer, byte[] outBuffer,
				int outByteOffset, int inFrameCount) {
			int sampleCount = inFrameCount * getFormat().getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.alaw2pcm8(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case SIGNED8:
				TConversionTool.alaw2pcm8(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case BIG_ENDIAN16:
				TConversionTool.alaw2pcm16(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, true);
				break;
			case LITTLE_ENDIAN16:
				TConversionTool.alaw2pcm16(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount, false);
				break;
			case ULAW8:
				TConversionTool.alaw2ulaw(inBuffer, 0, outBuffer,
						outByteOffset, sampleCount);
				break;
			}
			return inFrameCount;
		}

		@Override
		protected void convertInPlace(byte[] buffer, int byteOffset,
				int frameCount) {
			int sampleCount = frameCount * format.getChannels();
			switch (convertType) {
			case UNSIGNED8:
				TConversionTool.alaw2pcm8(buffer, byteOffset, sampleCount,
						false);
				break;
			case SIGNED8:
				TConversionTool.alaw2pcm8(buffer, byteOffset, sampleCount, true);
				break;
			case ULAW8:
				TConversionTool.alaw2ulaw(buffer, byteOffset, sampleCount);
				break;
			default:
				throw new RuntimeException("FromAlawStream: Call to convertInPlace, "
					+"but it cannot convert in place. (convertType=" + convertType + ")");
			}
		}
	}

}
