/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;


/**
 * AudioManager deals with creating, managing and severing audio connections to
 * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}. Also controls audio handlers.
 *
 * @see Guild#getAudioManager()
 */
public interface AudioManager
{
    long DEFAULT_CONNECTION_TIMEOUT = 10000;
    Logger LOG = JDALogger.getLog(AudioManager.class);

    /**
     * Starts the process to create an audio connection with an {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}
     * or, if an audio connection is already open, JDA will move the connection to the provided AudioChannel.
     * <br><b>Note</b>: Currently you can only be connected to a single {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}
     * per {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>This method will automatically move the current connection if one connection is already open in this underlying {@link Guild}.
     * <br>Current connections can be closed with {@link #closeAudioConnection()}.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel} to open an audio connection with.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided channel was {@code null}.</li>
     *             <li>If the provided channel is not part of the Guild that the current audio connection is connected to.</li>
     *         </ul>
     * @throws UnsupportedOperationException
     *         If audio is disabled due to an internal JDA error
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If the currently logged in account does not have the Permission {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT VOICE_CONNECT}</li>
     *             <li>If the currently logged in account does not have the Permission {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS VOICE_MOVE_OTHERS}
     *                 and the {@link net.dv8tion.jda.api.entities.VoiceChannel#getUserLimit() user limit} has been exceeded!</li>
     *         </ul>
     */
    void openAudioConnection(AudioChannel channel);

    /**
     * Close down the current audio connection of this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * and disconnects from the {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}.
     * <br>If this is called when JDA doesn't have an audio connection, nothing happens.
     */
    void closeAudioConnection();

    /**
     * The {@link SpeakingMode} that should be used when sending audio via
     * the provided {@link AudioSendHandler} from {@link #setSendingHandler(AudioSendHandler)}.
     * By default this will use {@link SpeakingMode#VOICE}.
     * <br>Example: {@code EnumSet.of(SpeakingMode.PRIORITY_SPEAKER, SpeakingMode.VOICE)}
     *
     * @param  mode
     *         The speaking modes
     *
     * @throws IllegalArgumentException
     *         If the provided collection is null or empty
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see    #getSpeakingMode()
     * @see    #setSpeakingMode(SpeakingMode...)
     */
    @Incubating
    void setSpeakingMode(@Nonnull Collection<SpeakingMode> mode);

    /**
     * The {@link SpeakingMode} that should be used when sending audio via
     * the provided {@link AudioSendHandler} from {@link #setSendingHandler(AudioSendHandler)}.
     * By default this will use {@link SpeakingMode#VOICE}.
     *
     * @param  mode
     *         The speaking modes
     *
     * @throws IllegalArgumentException
     *         If the provided array is null or empty
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see    #getSpeakingMode()
     */
    @Incubating
    default void setSpeakingMode(@Nonnull SpeakingMode... mode)
    {
        Checks.notNull(mode, "Speaking Mode");
        setSpeakingMode(Arrays.asList(mode));
    }

    /**
     * The {@link SpeakingMode} that should be used when sending audio via
     * the provided {@link AudioSendHandler} from {@link #setSendingHandler(AudioSendHandler)}.
     * By default this will use {@link SpeakingMode#VOICE}.
     *
     * @return The current speaking mode, represented in an {@link EnumSet}
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see    #setSpeakingMode(Collection)
     */
    @Nonnull
    @Incubating
    EnumSet<SpeakingMode> getSpeakingMode();

    /**
     * Configures the delay between the last provided frame and removing the speaking indicator.
     * <br>This can be useful for send systems that buffer a certain interval of audio frames that will be sent.
     * By default the delay is 200 milliseconds which is also the minimum delay.
     *
     * <p>If the delay is less than 200 milliseconds it will use the minimum delay. The provided delay
     * will be aligned to the audio frame length of 20 milliseconds by means of integer division. This means
     * it will be rounded down to the next biggest multiple of 20.
     *
     * <p>Note that this delay is not reliable and operates entirely based on the send system polling times
     * which can cause it to be released earlier or later than the provided delay specifies.
     *
     * @param millis
     *        The delay that should be used, in milliseconds
     *
     * @since 4.0.0
     */
    void setSpeakingDelay(int millis);

    /**
     * Gets the {@link net.dv8tion.jda.api.JDA JDA} instance that this AudioManager is a part of.
     *
     * @return The corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.Guild Guild} instance that this AudioManager is used for.
     *
     * @return The Guild that this AudioManager manages.
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel} that JDA currently has an audio connection
     * to. If JDA currently doesn't have an audio connection to an {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}
     * this will return {@code null}.
     *
     * @return The {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel} the audio connection is connected to
     *         or {@code null} if not connected.
     */
    @Nullable
    AudioChannel getConnectedChannel();

    /**
     * This can be used to find out if JDA currently has an active audio connection with a
     * {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel}. If this returns true, then
     * {@link #getConnectedChannel()} will return the {@link net.dv8tion.jda.api.entities.AudioChannel AudioChannel} which
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
     * Sets the {@link net.dv8tion.jda.api.audio.AudioSendHandler}
     * that the manager will use to provide audio data to an audio connection.
     * <br>The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br>Setting this to null will remove the audio handler.
     *
     * <p>JDA recommends <a href="https://github.com/sedmelluq/lavaplayer" target="_blank">LavaPlayer</a>
     * as an {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler}.
     * It provides a <a href="https://github.com/sedmelluq/lavaplayer/tree/master/demo-jda" target="_blank">demo</a> targeted at JDA users.
     *
     * @param handler
     *        The {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler} used to provide audio data.
     */
    void setSendingHandler(@Nullable AudioSendHandler handler);

    /**
     * The currently set {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler}. If there is
     * no sender currently set, this method will return {@code null}.
     *
     * @return The currently active {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler} or {@code null}.
     */
    @Nullable
    AudioSendHandler getSendingHandler();

    /**
     * Sets the {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler}
     * that the manager will use to process audio data received from an audio connection.
     *
     * <p>The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br>Setting this to null will remove the audio handler.
     *
     * @param handler
     *        The {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} used to process
     *        received audio data.
     */
    void setReceivingHandler(@Nullable AudioReceiveHandler handler);

    /**
     * The currently set {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler}.
     * If there is no receiver currently set, this method will return {@code null}.
     *
     * @return The currently active {@link net.dv8tion.jda.api.audio.AudioReceiveHandler AudioReceiveHandler} or {@code null}.
     */
    @Nullable
    AudioReceiveHandler getReceivingHandler();

    /**
     * Sets the {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener} for this AudioManager.
     * It will be informed about meta data of any audio connection established through this AudioManager.
     * Further information can be found in the {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener} documentation!
     *
     * @param listener
     *        A {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener} instance
     */
    void setConnectionListener(@Nullable ConnectionListener listener);

    /**
     * The currently set {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener}
     * or {@code null} if no {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener} has been {@link #setConnectionListener(ConnectionListener) set}.
     *
     * @return The current {@link net.dv8tion.jda.api.audio.hooks.ConnectionListener ConnectionListener} instance
     *         for this AudioManager.
     */
    @Nullable
    ConnectionListener getConnectionListener();

    /**
     * The current {@link net.dv8tion.jda.api.audio.hooks.ConnectionStatus ConnectionStatus}.
     * <br>This status indicates represents the connection status of an audio connection.
     *
     * @return The current {@link net.dv8tion.jda.api.audio.hooks.ConnectionStatus ConnectionStatus}.
     */
    @Nonnull
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
     * this will cause the {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler} packages
     * to not be ignored by Discord!
     *
     * @param muted
     *        Whether the connection should stop sending audio
     *        and display as muted.
     */
    void setSelfMuted(boolean muted);

    /**
     * Whether connections from this AudioManager are muted,
     * if this is {@code true} packages by the registered {@link net.dv8tion.jda.api.audio.AudioSendHandler AudioSendHandler}
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
