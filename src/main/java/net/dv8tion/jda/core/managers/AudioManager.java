/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.utils.JDALogger;
import org.slf4j.Logger;


/**
 * AudioManager deals with creating, managing and severing audio connections to
 * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}. Also controls audio handlers.
 */
public interface AudioManager
{
    long DEFAULT_CONNECTION_TIMEOUT = 10000;
    Logger LOG = JDALogger.getLog(AudioManager.class);

    /**
     * Starts the process to create an audio connection with a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * or, if an audio connection is already open, JDA will move the connection to the provided VoiceChannel.
     * <br><b>Note</b>: Currently you can only be connected to a single {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * per {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p>This method will automatically move the current connection if one connection is already open in this underlying {@link Guild}.
     * <br>Current connections can be closed with {@link #closeAudioConnection()}.
     *
     * <p>Client accounts can only connect to a single {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * at once. It is an Account-Wide limitation!
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} to open an audio connection with.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided channel was {@code null}.</li>
     *             <li>If the provided channel is not part of the Guild that the current audio connection is connected to.</li>
     *         </ul>
     * @throws UnsupportedOperationException
     *         If audio is disabled due to an internal JDA error
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the Guild is temporarily unavailable
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#VOICE_CONNECT VOICE_CONNECT}</li>
     *             <li>If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#VOICE_MOVE_OTHERS VOICE_MOVE_OTHERS}
     *                 and the {@link net.dv8tion.jda.core.entities.VoiceChannel#getUserLimit() user limit} has been exceeded!</li>
     *         </ul>
     */
    void openAudioConnection(VoiceChannel channel);

    /**
     * Close down the current audio connection of this {@link net.dv8tion.jda.core.entities.Guild Guild}
     * and disconnects from the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * <br>If this is called when JDA doesn't have an audio connection, nothing happens.
     */
    void closeAudioConnection();

    /**
     * Gets the {@link net.dv8tion.jda.core.JDA JDA} instance that this AudioManager is a part of.
     *
     * @return The corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Guild Guild} instance that this AudioManager is used for.
     *
     * @return The Guild that this AudioManager manages.
     */
    Guild getGuild();

    /**
     * This can be used to find out if JDA is currently attempting to setup an audio connection.
     * <br>If this returns true then {@link #getQueuedAudioConnection()} will return the
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA is attempting to setup an audio connection to.
     *
     * @return True, if JDA is currently attempting to create an audio connection.
     */
    boolean isAttemptingToConnect();

    /**
     * This can be used to find out what {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} JDA is currently
     * attempting to setup an audio connection for. If JDA isn't trying to create an audio connection this will return
     * null.
     * <br>In addition, if JDA is already connected, this will return null. To determine if JDA is already has an audio connection
     * with a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} use {@link #isConnected()}
     *
     * @return The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA is attempting to create an
     *         audio connection with, or {@code null} if JDA isn't attempting to create a connection.
     */
    VoiceChannel getQueuedAudioConnection();

    /**
     * The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} that JDA currently has an audio connection
     * to. If JDA currently doesn't have an audio connection to a {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * this will return {@code null}.
     *
     * @return The {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} the audio connection is connected to
     *         or {@code null} if not connected.
     */
    VoiceChannel getConnectedChannel();

    /**
     * This can be used to find out if JDA currently has an active audio connection with a
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}. If this returns true, then
     * {@link #getConnectedChannel()} will return the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which
     * JDA is connected to.
     *
     * @return True, if JDA currently has an active audio connection.
     */
    boolean isConnected();

    /**
     * Sets the amount of time, in milliseconds, that will be used as the timeout when waiting for the audio connection
     * to successfully connect. The default value is 10 second (10,000 milliseconds).
     * <br><b>Note</b>: If you set this value to 0, you can remove timeout functionality and JDA will wait FOREVER for the connection
     * to be established. This is no advised as it is possible that the connection may never be established.
     *
     * @param timeout
     *        The amount of time, in milliseconds, that should be waited when waiting for the audio connection
     *        to be established.
     */
    void setConnectTimeout(long timeout);

    /**
     * The currently set timeout value, in <b>milliseconds</b>, used when waiting for an audio connection to be established.
     *
     * @return The currently set timeout.
     */
    long getConnectTimeout();

    /**
     * Sets the {@link net.dv8tion.jda.core.audio.AudioSendHandler}
     * that the manager will use to provide audio data to an audio connection.
     * <br>The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br>Setting this to null will remove the audio handler.
     *
     * <p>JDA recommends <a href="https://github.com/sedmelluq/lavaplayer" target="_blank">LavaPlayer</a>
     * as an {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler}.
     * It provides a <a href="https://github.com/sedmelluq/lavaplayer/tree/master/demo-jda" target="_blank">demo</a> targeted at JDA users.
     *
     * @param handler
     *        The {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} used to provide audio data.
     */
    void setSendingHandler(AudioSendHandler handler);

    /**
     * The currently set {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler}. If there is
     * no sender currently set, this method will return {@code null}.
     *
     * @return The currently active {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} or {@code null}.
     */
    AudioSendHandler getSendingHandler();

    /**
     * Sets the {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler}
     * that the manager will use to process audio data received from an audio connection.
     *
     * <p>The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br>Setting this to null will remove the audio handler.
     *
     * @param handler
     *        The {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler} used to process
     *        received audio data.
     */
    void setReceivingHandler(AudioReceiveHandler handler);

    /**
     * The currently set {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler}.
     * If there is no receiver currently set, this method will return {@code null}.
     *
     * @return The currently active {@link net.dv8tion.jda.core.audio.AudioReceiveHandler AudioReceiveHandler} or {@code null}.
     */
    AudioReceiveHandler getReceiveHandler();

    /**
     * Sets the {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener} for this AudioManager.
     * It will be informed about meta data of any audio connection established through this AudioManager.
     * Further information can be found in the {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener} documentation!
     *
     * @param listener
     *        A {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener} instance
     */
    void setConnectionListener(ConnectionListener listener);

    /**
     * The currently set {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener}
     * or {@code null} if no {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener} has been {@link #setConnectionListener(ConnectionListener) set}.
     *
     * @return The current {@link net.dv8tion.jda.core.audio.hooks.ConnectionListener ConnectionListener} instance
     *         for this AudioManager.
     */
    ConnectionListener getConnectionListener();

    /**
     * The current {@link net.dv8tion.jda.core.audio.hooks.ConnectionStatus ConnectionStatus}.
     * <br>This status indicates represents the connection status of an audio connection.
     *
     * @return The current {@link net.dv8tion.jda.core.audio.hooks.ConnectionStatus ConnectionStatus}.
     */
    ConnectionStatus getConnectionStatus();

    /**
     * Sets whether audio connections from this AudioManager
     * should automatically reconnect or not. Default {@code true}
     *
     * @param shouldReconnect
     *        Whether audio connections from this AudioManager should automatically reconnect
     */
    void setAutoReconnect(boolean shouldReconnect);

    /**
     * Whether audio connections from this AudioManager automatically reconnect
     *
     * @return Whether audio connections from this AudioManager automatically reconnect
     */
    boolean isAutoReconnect();

    /**
     * Set this to {@code true} if the current connection should be displayed as muted,
     * this will cause the {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler} packages
     * to not be ignored by Discord!
     *
     * @param muted
     *        Whether the connection should stop sending audio
     *        and display as muted.
     */
    void setSelfMuted(boolean muted);

    /**
     * Whether connections from this AudioManager are muted,
     * if this is {@code true} packages by the registered {@link net.dv8tion.jda.core.audio.AudioSendHandler AudioSendHandler}
     * will be ignored by Discord.
     *
     * @return Whether connections from this AudioManager are muted
     */
    boolean isSelfMuted();

    /**
     * Sets whether connections from this AudioManager should be deafened.
     * <br>This does not include being muted, that value can be set individually from {@link #setSelfMuted(boolean)}
     * and checked via {@link #isSelfMuted()}
     *
     * @param deafened
     *        Whether connections from this AudioManager should be deafened.
     */
    void setSelfDeafened(boolean deafened);

    /**
     * Whether connections from this AudioManager are deafened.
     * <br>This does not include being muted, that value can be set individually from {@link #setSelfMuted(boolean)}
     * and checked via {@link #isSelfMuted()}
     *
     * @return True, if connections from this AudioManager are deafened
     */
    boolean isSelfDeafened();
}
