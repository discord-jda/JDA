/*
 *	StandardMidiFileReader.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package org.tritonus.midi.file;

import org.tritonus.share.TDebug;

import javax.sound.midi.*;
import javax.sound.midi.spi.MidiFileWriter;
import java.io.*;



/**	Writer for Standard Midi Files.
	This writer can write type 0 and type 1 files. It cannot write type
	2 files.
 */
public class StandardMidiFileWriter
extends MidiFileWriter
{
	public static boolean		USE_RUNNING_STATUS = true;

	public static boolean		CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX = true;



	/**	Return supported MIDI file types.
		This writer supports Standard MIDI File (SMF) types 0 and 1.
		So these numbers are returned here.

		@return an array of supported SMF types.
	*/
	public int[] getMidiFileTypes()
	{
		return new int[]{0, 1};
	}



	/**	Return the supported MIDI file types for a given Sequence.
		This writer supports Standard MIDI File (SMF) types 0 and 1.
		Depending on the Sequence, either 0 or 1 is returned.

		@return and array of supported SMF types. It contains 0 if
		the Sequence has one track, 1 otherwise.
	 */
	public int[] getMidiFileTypes(Sequence sequence)
	{
		Track[]	tracks = sequence.getTracks();
		if (tracks.length == 1)
		{
			return new int[]{0};
		}
		else
		{
			return new int[]{1};
		}
	}



	/**	Write a Sequence as Standard MIDI File (SMF) to an OutputStream.
		A byte stream representing the passed Sequence is written
		to the output stream in the given file type.

		@return The number of bytes written to the output stream.
	 */
	public int write(Sequence sequence,
			 int nFileType,
			 OutputStream outputStream)
		throws IOException
	{
		if (! isFileTypeSupported(nFileType, sequence))
		{
			throw new IllegalArgumentException("file type is not supported for this sequence");
		}
		Track[]	aTracks = sequence.getTracks();
		DataOutputStream	dataOutputStream = new DataOutputStream(outputStream);
		dataOutputStream.writeInt(MidiConstants.HEADER_MAGIC);
		dataOutputStream.writeInt(6);	// header length
		dataOutputStream.writeShort(nFileType);
		dataOutputStream.writeShort(aTracks.length);
		float	fDivisionType = sequence.getDivisionType();
		int	nResolution = sequence.getResolution();
		int	nDivision = 0;
		if (fDivisionType == Sequence.PPQ)
		{
			nDivision = nResolution & 0x7fff;
		}
		else
		{
			// TODO:
		}
		dataOutputStream.writeShort(nDivision);	// unsigned?
		int	nBytesWritten = 14;
		for (int nTrack = 0; nTrack < aTracks.length; nTrack++)
		{
			nBytesWritten += writeTrack(aTracks[nTrack],
						    dataOutputStream);
		}

		return nBytesWritten;
	}



	/**	Write a Sequence as Standard MIDI File (SMF) to a File.
		A byte stream representing the passed Sequence is written
		to the file in the given file type.

		@return The number of bytes written to the file.
	 */
	public int write(Sequence sequence,
			 int nFileType,
			 File file)
		throws IOException
	{
		OutputStream	outputStream = new FileOutputStream(file);
		int	nBytes = write(sequence,
				       nFileType,
				       outputStream);
		outputStream.close();
		return nBytes;
	}



	/**	Write a Track to a DataOutputStream.

	@return The number of bytes written.
	*/
	private static int writeTrack(Track track,
				      DataOutputStream dataOutputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		if (dataOutputStream != null)
		{
			dataOutputStream.writeInt(MidiConstants.TRACK_MAGIC);
		}
		/*
		 *	This is a recursive call!
		 *	It is to find out the length of the track without
		 *	actually writing. Having the second parameter as
		 *	null tells writeTrack() and its subordinate
		 *	methods to not write out data bytes.
		 */
		int	nTrackLength = 0;
		if (dataOutputStream != null)
		{
			nTrackLength = writeTrack(track, null);
		}
		if (dataOutputStream != null)
		{
			dataOutputStream.writeInt(nTrackLength);
		}
		MidiEvent	previousEvent = null;
		int[]	anRunningStatusByte = new int[1];
		anRunningStatusByte[0] = -1;
		for (int nEvent = 0; nEvent < track.size(); nEvent++)
		{
			MidiEvent	event = track.get(nEvent);
			nLength += writeEvent(event,
					      previousEvent,
					      anRunningStatusByte,
					      dataOutputStream);
			previousEvent = event;
		}
		return nLength;
	}



	private static int writeEvent(MidiEvent event,
				      MidiEvent previousEvent,
				      int[] anRunningStatusByte,
				      DataOutputStream dataOutputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		long	lTickDelta = 0;
		if (previousEvent != null)
		{
			lTickDelta = event.getTick() - previousEvent.getTick();
		}
		if (lTickDelta < 0)
		{
			TDebug.out("StandardMidiFileWriter.writeEvent(): warning: events not in order");
		}
		// add bytes according to coded length of delta
		nLength += writeVariableLengthQuantity(lTickDelta, dataOutputStream);
		MidiMessage	message = event.getMessage();
		// int		nDataLength = message.getLength();
		if (message instanceof ShortMessage)
		{
			nLength += writeShortMessage((ShortMessage) message,
						     anRunningStatusByte,
						     dataOutputStream);
		}
		else if (message instanceof SysexMessage)
		{
			nLength += writeSysexMessage((SysexMessage) message,
						     anRunningStatusByte,
						     dataOutputStream);
		}
		else if (message instanceof MetaMessage)
		{
			nLength += writeMetaMessage((MetaMessage) message,
						     anRunningStatusByte,
						     dataOutputStream);
		}
		else
		{
			TDebug.out("StandardMidiFileWriter.writeEvent(): warning: unknown message class");
		}
		return nLength;
	}



	private static int writeShortMessage(ShortMessage message,
					     int[] anRunningStatusByte,
					     DataOutputStream dataOutputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		int	nDataLength = message.getLength();
		if (USE_RUNNING_STATUS && anRunningStatusByte[0] == message.getStatus())
		{
			/*
			 *	Write without status byte.
			 */
			if (dataOutputStream != null)
			{
				dataOutputStream.write(
					message.getMessage(),
					1, nDataLength - 1);
			}
			nLength += nDataLength - 1;
		}
		else
		{
			/*
			 *	Write with status byte.
			 */
			if (dataOutputStream != null)
			{
				dataOutputStream.write(
					message.getMessage(),
					0, nDataLength);
			}
			nLength += nDataLength;
			anRunningStatusByte[0] = message.getStatus();
		}
		return nLength;
	}



	private static int writeSysexMessage(SysexMessage message,
					     int[] anRunningStatusByte,
					     DataOutputStream dataOutputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		int	nDataLength = message.getLength();
		if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX)
		{
			anRunningStatusByte[0] = -1;
		}
		if (dataOutputStream != null)
		{
			dataOutputStream.write(message.getStatus());
		}
		nLength++;
		nLength += writeVariableLengthQuantity(
			nDataLength - 1,
			dataOutputStream);
		if (dataOutputStream != null)
		{
			dataOutputStream.write(
				message.getData(),
				0, nDataLength - 1);
		}
		nLength += nDataLength - 1;
		return nLength;
	}



	private static int writeMetaMessage(MetaMessage message,
					    int[] anRunningStatusByte,
					    DataOutputStream dataOutputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		byte[]	abData = message.getData();
		int	nDataLength = abData.length;
		if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX)
		{
			anRunningStatusByte[0] = -1;
		}
		if (dataOutputStream != null)
		{
			dataOutputStream.write(message.getStatus());
			dataOutputStream.write(message.getType());
		}
		nLength += 2;
		nLength += writeVariableLengthQuantity(
			nDataLength,
			dataOutputStream);
		if (dataOutputStream != null)
		{
			dataOutputStream.write(abData);
		}
		nLength += nDataLength;
		return nLength;
	}



	/**	TODO:
		outputStream == 0 signals to only calculate the number of
		needed to represent the value.
	*/
	private static int writeVariableLengthQuantity(long lValue, OutputStream outputStream)
		throws IOException
	{
		/**	The number of bytes written. This is used as return
			value for this method.
		*/
		int	nLength = 0;
		// IDEA: use a loop
		boolean	bWritingStarted = false;
		int	nByte = (int) ((lValue >> 21) & 0x7f);
		if (nByte != 0)
		{
			if (outputStream != null)
			{
				outputStream.write(nByte | 0x80);
			}
			nLength++;
			bWritingStarted = true;
		}
		nByte = (int) ((lValue >> 14) & 0x7f);
		if (nByte != 0 || bWritingStarted)
		{
			if (outputStream != null)
			{
				outputStream.write(nByte | 0x80);
			}
			nLength++;
			bWritingStarted = true;
		}
		nByte = (int) ((lValue >> 7) & 0x7f);
		if (nByte != 0 || bWritingStarted)
		{
			if (outputStream != null)
			{
				outputStream.write(nByte | 0x80);
			}
			nLength++;
		}
		nByte = (int) (lValue & 0x7f);
		if (outputStream != null)
		{
			outputStream.write(nByte);
		}
		nLength++;
		return nLength;
	}
}



/*** StandardMidiFileWriter.java ***/

