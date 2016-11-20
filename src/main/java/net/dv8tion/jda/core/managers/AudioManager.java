/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.audio.hooks.ConnectionListener;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * AudioManager deals with creating, managing and severing audio connections to
 * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}. Also controls audio handlers.
 */
public interface AudioManager
{
    long DEFAULT_CONNECTION_TIMEOUT = 10000;
    SimpleLog LOG = SimpleLog.getLog("JDAAudioManager");

    /**
     * Starts the process to create an audio connection with a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * or, if an audio connection is already open, JDA will move the connection to the provided VoiceChannel.<br>
     * Note: Currently you can only be connected to a single {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * per {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} to open an audio connection with.
     *
     * @throws java.lang.IllegalArgumentException
     *          <ul>
     *              <li>If the provided channel was <code>null</code>.</li>
     *              <li>If the provided channel is not part of the Guild that the current audio connection is connected to.</li>
     *          </ul>
     * @throws java.lang.IllegalStateException
     *          This will  be thrown if JDA is currently attempting to setup an audio connection.<br>
     *          For this situation, first checking {@link #isAttemptingToConnect()} is advised.
     *
     */
    void openAudioConnection(VoiceChannel channel);

    /**
     * Used to close down the audio connection and disconnect from the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.<br>
     * As a note, if this is called when JDA doesn't have an audio connection, nothing happens.
     */
    void closeAudioConnection();

    /**
     * Gets the {@link net.dv8tion.jda.core.JDA JDA} instance that this AudioManager is a part of.
     *
     * @return
     *      This AudioManager's JDA instance.
     */
    JDA getJDA();

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Guild Guild} instance that this AudioManager is used for.
     *
     * @return
     *      The Guild that this AudioManager manages.
     */
    Guild getGuild();

    /**
     * This can be used to find out if JDA is currently attempting to setup an audio connection.<br>
     * If this returns true then {@link #getQueuedAudioConnection()} will return the
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA is attempting to setup an audio connection to.
     *
     * @return
     *      True if JDA is currently attempting to create an audio connection.
     */
    boolean isAttemptingToConnect();

    /**
     * This can be used to find out what {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} JDA is currently
     * attempting to setup an audio connection for. If JDA isn't trying to create an audio connection this will return
     * null.<br>
     * Also, if JDA is already connected, this will return null. To determine if JDA is already has an audio connection
     * with a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} use {@link #isConnected()}
     *
     * @return
     *      The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA is attempting to create an
     *      audio connection with, or null if JDA isn't attempting to create a connection.
     */
    VoiceChannel getQueuedAudioConnection();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA currently has an audio connection
     * to. If JDA currently doesn't have an audio connection to a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * this will return null.
     *
     * @return
     *      The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} the audio connection is connected to
     *      or <code>null</code> if not connected.
     */
    VoiceChannel getConnectedChannel();

    /**
     * This can be used to find out if JDA currently has an active audio connection with a
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}. If this returns true, then
     * {@link #getConnectedChannel()} will return the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which
     * JDA is connected to.
     *
     * @return
     *      True if JDA currently has an active audio connection.
     */
    boolean isConnected();

    /**
     * Sets the amount of time, in milliseconds, that will be used as the timeout when waiting for the audio connection
     * to successfully connect. The default value is 10 second (10,000 milliseconds).<br>
     * NOTE: If you set this value to 0, you can remove timeout functionality and JDA will wait FOREVER for the connection
     * to be established. This is no advised as it is possible that the connection may never be established.
     *
     * @param timeout
     *          The amount of time, in milliseconds, that should be waited when waiting for the audio connection
     *          to be established.
     */
    void setConnectTimeout(long timeout);

    /**
     * Returns the currently set timeout value, in milliseconds, used when waiting for an audio connection to be established.
     *
     * @return
     *      The currently set timeout.
     */
    long getConnectTimeout();

    /**
     * Sets the {@link net.dv8tion.jda.core.audio.AudioSendHandler}
     * that the manager will use to provide audio data to an audio connection.<br>
     * The handler provided here will persist between audio connection connect and disconnects. Furthermore, you don't
     * need to have an audio connection to set a handler. When JDA sets up a new audio connection it will use the
     * handler provided here.<br>
     * Setting this to null will remove the audio handler.
     * <p>
     * Example implementations of an {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} can be seen in the
     * abstract class {@link net.dv8tion.jda.core.audio.player.Player Player} and its subclasses
     * {@link net.dv8tion.jda.core.audio.player.FilePlayer FilePlayer} and {@link net.dv8tion.jda.core.audio.player.URLPlayer URLPlayer}.
     *
     * @param handler
     *          The {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} used to provide audio data.
     */
    void setSendingHandler(AudioSendHandler handler);

    /**
     * Returns the currently set {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler}. If there is
     * no sender currently set, this method will return null.
     *
     * @return
     *      The currently active {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} or <code>null</code>.
     */
    AudioSendHandler getSendingHandler();

    /**
     * Sets the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler}
     * that the manager will use to process audio data received from an audio connection.
     * <p>
     * The handler provided here will persist between audio connection connect and disconnects. Furthermore, you don't
     * need to have an audio connection to set a handler. When JDA sets up a new audio connection it will use the
     * handler provided here.<br>
     * Setting this to null will remove the audio handler.
     *
     * @param handler
     *          The {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler} used to process
     *          received audio data.
     */
    void setReceivingHandler(AudioReceiveHandler handler);

    /**
     * Returns the currently set {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler}. If there is
     * no receiver currently set, this method will return null.
     *
     * @return
     *      The currently active {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler} or <code>null</code>.
     */
    AudioReceiveHandler getReceiveHandler();

    void setConnectionListener(ConnectionListener listener);

    ConnectionListener getConnectionListener();

    ConnectionStatus getConnectionStatus();
}
