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
import org.tritonus.share.midi.TMidiFileFormat;

import javax.sound.midi.*;
import javax.sound.midi.spi.MidiFileReader;
import java.io.*;
import java.net.URL;

public class StandardMidiFileReader
extends MidiFileReader
{
	public static boolean		CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX = true;

	private static final int	STATUS_NONE = 0;
	private static final int	STATUS_ONE_BYTE = 1;
	private static final int	STATUS_TWO_BYTES = 2;
	private static final int	STATUS_SYSEX = 3;
	private static final int	STATUS_META = 4;

	public MidiFileFormat getMidiFileFormat(InputStream inputStream)
		throws InvalidMidiDataException, IOException
	{
		DataInputStream		dataInputStream = new DataInputStream(inputStream);
		int	nHeaderMagic = dataInputStream.readInt();
		if (nHeaderMagic != MidiConstants.HEADER_MAGIC)
		{
			throw new InvalidMidiDataException("not a MIDI file: wrong header magic");
		}
		int	nHeaderLength = dataInputStream.readInt();
		if (nHeaderLength < 6)
		{
			throw new InvalidMidiDataException("corrupt MIDI file: wrong header length");
		}
		int	nType = dataInputStream.readShort();
		if (nType < 0 || nType > 2)
		{
			throw new InvalidMidiDataException("corrupt MIDI file: illegal type");
		}
		if (nType == 2)
		{
			throw new InvalidMidiDataException("this implementation doesn't support type 2 MIDI files");
		}
		int	nNumTracks = dataInputStream.readShort();
		if (nNumTracks <= 0)
		{
			throw new InvalidMidiDataException("corrupt MIDI file: number of tracks must be positive");
		}
		if (nType == 0 && nNumTracks != 1)
		{
			throw new InvalidMidiDataException("corrupt MIDI file:  type 0 files must contain exactely one track");
		}
		int	nDivision = dataInputStream.readUnsignedShort();
		float	fDivisionType = -1.0F;
		int	nResolution = -1;
		if ((nDivision & 0x8000) != 0)	//frame division
		{
			int	nFrameType = -((nDivision >>> 8) & 0xFF);
			switch (nFrameType)
			{
			case 24:
				fDivisionType = Sequence.SMPTE_24;
				break;

			case 25:
				fDivisionType = Sequence.SMPTE_25;
				break;

			case 29:
				fDivisionType = Sequence.SMPTE_30DROP;
				break;

			case 30:
				fDivisionType = Sequence.SMPTE_30;
				break;

			default:
				throw new InvalidMidiDataException("corrupt MIDI file: illegal frame division type");
			}
			nResolution = nDivision & 0xff;
		}
		else				// BPM division
		{
			fDivisionType = Sequence.PPQ;
			nResolution = nDivision & 0x7fff;
		}
		// skip additional bytes in the header
		dataInputStream.skip(nHeaderLength - 6);
		MidiFileFormat	midiFileFormat = new TMidiFileFormat(
			nType,
			fDivisionType,
			nResolution,
			MidiFileFormat.UNKNOWN_LENGTH,
			MidiFileFormat.UNKNOWN_LENGTH,
			nNumTracks);
		return midiFileFormat;
	}


	public MidiFileFormat getMidiFileFormat(URL url)
		throws InvalidMidiDataException, IOException
	{
		InputStream	inputStream = url.openStream();
		try
		{
			return getMidiFileFormat(inputStream);
		}
		finally
		{
			inputStream.close();
		}
	}

	public MidiFileFormat getMidiFileFormat(File file)
		throws InvalidMidiDataException, IOException
	{
		InputStream	inputStream = new FileInputStream(file);
		//inputStream = new BufferedInputStream(inputStream, 1024);
		try
		{
			return getMidiFileFormat(inputStream);
		}
		finally
		{
			inputStream.close();
		}
	}

	public Sequence getSequence(URL url)
		throws InvalidMidiDataException, IOException
	{
		InputStream	inputStream = url.openStream();
		try
		{
			return getSequence(inputStream);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
			inputStream.close();
			throw e;
		}
		catch (IOException e)
		{
			if (TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
			inputStream.close();
			throw e;
		}
	}

	public Sequence getSequence(File file)
		throws InvalidMidiDataException, IOException
	{
		InputStream	inputStream = new FileInputStream(file);
		// inputStream = new BufferedInputStream(inputStream, 1024);
		try
		{
			return getSequence(inputStream);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
			inputStream.close();
			throw e;
		}
		catch (IOException e)
		{
			if (TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
			inputStream.close();
			throw e;
		}
	}

	public Sequence getSequence(InputStream inputStream)
		throws InvalidMidiDataException, IOException
	{
		MidiFileFormat	midiFileFormat = getMidiFileFormat(inputStream);
		Sequence	sequence = new Sequence(
			midiFileFormat.getDivisionType(),
			midiFileFormat.getResolution());
		DataInputStream		dataInputStream = new DataInputStream(inputStream);
		int	nNumTracks = ((TMidiFileFormat) midiFileFormat).getTrackCount();
		for (int nTrack = 0; nTrack < nNumTracks; nTrack++)
		{
			Track	track = sequence.createTrack();
			readTrack(dataInputStream, track);
		}
		return sequence;
	}

	private void readTrack(DataInputStream dataInputStream, Track track)
		throws InvalidMidiDataException, IOException
	{
		// search for a "MTrk" chunk
		while (true)
		{
			int	nMagic = dataInputStream.readInt();
			if (nMagic == MidiConstants.TRACK_MAGIC)
			{
				break;
			}
			int	nChunkLength = dataInputStream.readInt();
			if (nChunkLength % 2 != 0)
			{
				nChunkLength++;
			}
			dataInputStream.skip(nChunkLength);
		}
		int	nTrackChunkLength = dataInputStream.readInt();
		long	lTicks = 0;
		long[]	alRemainingBytes = new long[1];
		alRemainingBytes[0] = nTrackChunkLength;
		int[]	anRunningStatusByte = new int[1];
		// indicates no running status in effect
		anRunningStatusByte[0] = -1;
		while (alRemainingBytes[0] > 0)
		{
			long	lDeltaTicks = readVariableLengthQuantity(dataInputStream, alRemainingBytes);
			// TDebug.out("delta ticks: " + lDeltaTicks);
			lTicks += lDeltaTicks;
			MidiEvent	event = readEvent(dataInputStream, alRemainingBytes, anRunningStatusByte, lTicks);
			track.add(event);
		}
	}

	private static MidiEvent readEvent(DataInputStream dataInputStream, long[] alRemainingBytes, int[] anRunningStatusByte, long lTicks)
		throws InvalidMidiDataException, IOException
	{
		int	nStatusByte = readUnsignedByte(dataInputStream, alRemainingBytes);
		// TDebug.out("status byte: " + nStatusByte);
		MidiMessage	message = null;
		boolean		bRunningStatusApplies = false;
		int		nSavedByte = 0;
		if (nStatusByte < 0x80)
		{
			if (anRunningStatusByte[0] != -1)
			{
				bRunningStatusApplies = true;
				nSavedByte = nStatusByte;
				nStatusByte = anRunningStatusByte[0];
			}
			else
			{
				throw new InvalidMidiDataException("corrupt MIDI file: status byte missing");
			}
		}
		switch (getType(nStatusByte))
		{
		case STATUS_ONE_BYTE:
			int	nByte = 0;
			if (bRunningStatusApplies)
			{
				nByte = nSavedByte;
			}
			else
			{
				nByte = readUnsignedByte(dataInputStream, alRemainingBytes);
				anRunningStatusByte[0] = nStatusByte;
			}
			ShortMessage	shortMessage1 = new ShortMessage();
			shortMessage1.setMessage(nStatusByte, nByte, 0);
			message = shortMessage1;
			break;

		case STATUS_TWO_BYTES:
			int	nByte1 = 0;
			if (bRunningStatusApplies)
			{
				nByte1 = nSavedByte;
			}
			else
			{
				nByte1 = readUnsignedByte(dataInputStream, alRemainingBytes);
				anRunningStatusByte[0] = nStatusByte;
			}
			int	nByte2 = readUnsignedByte(dataInputStream, alRemainingBytes);
			ShortMessage	shortMessage2 = new ShortMessage();
			shortMessage2.setMessage(nStatusByte, nByte1, nByte2);
			message = shortMessage2;
			break;

		case STATUS_SYSEX:
			if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX)
			{
				anRunningStatusByte[0] = -1;
			}
			int	nSysexDataLength = (int) readVariableLengthQuantity(dataInputStream, alRemainingBytes);
			byte[]	abSysexData = new byte[nSysexDataLength];
			for (int i = 0; i < nSysexDataLength; i++)
			{
				int	nDataByte = readUnsignedByte(dataInputStream, alRemainingBytes);
				abSysexData[i] = (byte) nDataByte;
			}
			SysexMessage	sysexMessage = new SysexMessage();
			sysexMessage.setMessage(nStatusByte, abSysexData, nSysexDataLength);
			message = sysexMessage;
			break;

		case STATUS_META:
			if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX)
			{
				anRunningStatusByte[0] = -1;
			}
			int	nTypeByte = readUnsignedByte(dataInputStream, alRemainingBytes);
			int	nMetaDataLength = (int) readVariableLengthQuantity(dataInputStream, alRemainingBytes);
			byte[]	abMetaData = new byte[nMetaDataLength];
			for (int i = 0; i < nMetaDataLength; i++)
			{
				int	nDataByte = readUnsignedByte(dataInputStream, alRemainingBytes);
				abMetaData[i] = (byte) nDataByte;
			}
			MetaMessage	metaMessage = new MetaMessage();
			metaMessage.setMessage(nTypeByte, abMetaData, nMetaDataLength);
			message = metaMessage;
			break;
		default:
		}
		MidiEvent	event = new MidiEvent(message, lTicks);
		return event;
	}

	private static int getType(int nStatusByte)
	{
		if (nStatusByte < 0xf0)	// channel voice or mode command
		{
			int	nCommand = nStatusByte & 0xf0;
			switch (nCommand)
			{
			case 0x80:	// note off
			case 0x90:	// note on
			case 0xa0:	// polyphonic key pressure
			case 0xb0:	// control change
			case 0xe0:	// pitch wheel change
				return STATUS_TWO_BYTES;

			case 0xc0:	// program change
			case 0xd0:	// channel pressure
				return STATUS_ONE_BYTE;

			default:
				return STATUS_NONE;
			}
		}
		else if (nStatusByte == 0xf0 || nStatusByte == 0xf7)
		{
			return STATUS_SYSEX;
		}
		else if (nStatusByte == 0xff)
		{
			return STATUS_META;
		}
		else
		{
			return STATUS_NONE;
		}
	}

	public static long readVariableLengthQuantity(DataInputStream dataInputStream, long[] alRemainingBytes)
		throws InvalidMidiDataException, IOException
	{
		long	lValue = 0;
		int	nByteCount = 0;
		while (nByteCount < 4)
		{
			int	nByte = readUnsignedByte(dataInputStream, alRemainingBytes);
			nByteCount++;
			lValue <<= 7;
			lValue |= (nByte & 0x7f);
			if (nByte < 128)	// MSB is 0: last byte
			{
				return lValue;
			}
		}
		throw new InvalidMidiDataException("not a MIDI file: unterminated variable-length quantity");

	}

	public static int readUnsignedByte(DataInputStream dataInputStream, long[] alRemainingBytes)
		throws IOException
	{
		int	nByte = dataInputStream.readUnsignedByte();
// already done in DataInputStream.readUnsignedByte();
// 		if (nByte < 0)
// 		{
// 			throw new EOFException();
// 		}
		alRemainingBytes[0]--;
		return nByte;
	}
}



/*** StandardMidiFileReader.java ***/
