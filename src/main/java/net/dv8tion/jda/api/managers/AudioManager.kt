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
package net.dv8tion.jda.api.managers

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.annotations.Incubating
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.SpeakingMode
import net.dv8tion.jda.api.audio.hooks.ConnectionListener
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.JDALogger
import java.util.*
import javax.annotation.Nonnull

/**
 * AudioManager deals with creating, managing and severing audio connections to
 * [VoiceChannels][VoiceChannel]. Also controls audio handlers.
 *
 * @see Guild.getAudioManager
 */
interface AudioManager {
    /**
     * Starts the process to create an audio connection with an [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel]
     * or, if an audio connection is already open, JDA will move the connection to the provided AudioChannel.
     * <br></br>**Note**: Currently you can only be connected to a single [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel]
     * per [Guild][net.dv8tion.jda.api.entities.Guild].
     *
     *
     * This method will automatically move the current connection if one connection is already open in this underlying [Guild].
     * <br></br>Current connections can be closed with [.closeAudioConnection].
     *
     * @param  channel
     * The [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel] to open an audio connection with.
     *
     * @throws IllegalArgumentException
     *
     *  * If the provided channel was `null`.
     *  * If the provided channel is not part of the Guild that the current audio connection is connected to.
     *
     * @throws UnsupportedOperationException
     * If audio is disabled due to an internal JDA error
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *
     *  * If the currently logged in account does not have the Permission [VOICE_CONNECT][net.dv8tion.jda.api.Permission.VOICE_CONNECT]
     *  * If the currently logged in account does not have the Permission [VOICE_MOVE_OTHERS][net.dv8tion.jda.api.Permission.VOICE_MOVE_OTHERS]
     * and the [user limit][VoiceChannel.getUserLimit] has been exceeded!
     *
     */
    fun openAudioConnection(channel: AudioChannel?)

    /**
     * Close down the current audio connection of this [Guild][net.dv8tion.jda.api.entities.Guild]
     * and disconnects from the [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel].
     * <br></br>If this is called when JDA doesn't have an audio connection, nothing happens.
     */
    fun closeAudioConnection()

    /**
     * The [SpeakingMode] that should be used when sending audio via
     * the provided [AudioSendHandler] from [.setSendingHandler].
     * By default this will use [SpeakingMode.VOICE].
     * <br></br>Example: `EnumSet.of(SpeakingMode.PRIORITY_SPEAKER, SpeakingMode.VOICE)`
     *
     * @param  mode
     * The speaking modes
     *
     * @throws IllegalArgumentException
     * If the provided collection is null or empty
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see .getSpeakingMode
     * @see .setSpeakingMode
     */
    @Incubating
    fun setSpeakingMode(@Nonnull mode: Collection<SpeakingMode?>?)

    /**
     * The [SpeakingMode] that should be used when sending audio via
     * the provided [AudioSendHandler] from [.setSendingHandler].
     * By default this will use [SpeakingMode.VOICE].
     *
     * @param  mode
     * The speaking modes
     *
     * @throws IllegalArgumentException
     * If the provided array is null or empty
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see .getSpeakingMode
     */
    @Incubating
    fun setSpeakingMode(@Nonnull vararg mode: SpeakingMode?) {
        Checks.notNull(mode, "Speaking Mode")
        setSpeakingMode(Arrays.asList(*mode))
    }

    /**
     * The [SpeakingMode] that should be used when sending audio via
     * the provided [AudioSendHandler] from [.setSendingHandler].
     * By default this will use [SpeakingMode.VOICE].
     *
     * @return The current speaking mode, represented in an [EnumSet]
     *
     * @incubating Discord has not officially confirmed that this feature will be available to bots
     *
     * @see .setSpeakingMode
     */
    @Nonnull
    @Incubating
    fun getSpeakingMode(): EnumSet<SpeakingMode?>?

    /**
     * Configures the delay between the last provided frame and removing the speaking indicator.
     * <br></br>This can be useful for send systems that buffer a certain interval of audio frames that will be sent.
     * By default the delay is 200 milliseconds which is also the minimum delay.
     *
     *
     * If the delay is less than 200 milliseconds it will use the minimum delay. The provided delay
     * will be aligned to the audio frame length of 20 milliseconds by means of integer division. This means
     * it will be rounded down to the next biggest multiple of 20.
     *
     *
     * Note that this delay is not reliable and operates entirely based on the send system polling times
     * which can cause it to be released earlier or later than the provided delay specifies.
     *
     * @param millis
     * The delay that should be used, in milliseconds
     *
     * @since 4.0.0
     */
    @Deprecated("")
    @ForRemoval
    fun setSpeakingDelay(millis: Int)

    @get:Nonnull
    val jDA: JDA?

    @JvmField
    @get:Nonnull
    val guild: Guild?

    /**
     * The [AudioChannelUnion] that JDA currently has an audio connection to.
     * <br></br>If JDA currently doesn't have an active audio connection, this will return `null`.
     *
     * @return The [AudioChannelUnion] the audio connection is connected to, or `null` if not connected.
     */
    val connectedChannel: AudioChannelUnion?

    /**
     * This can be used to find out if JDA currently has an active audio connection with a
     * [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel]. If this returns true, then
     * [.getConnectedChannel] will return the [AudioChannel][net.dv8tion.jda.api.entities.channel.middleman.AudioChannel] which
     * JDA is connected to.
     *
     * @return True, if JDA currently has an active audio connection.
     */
    val isConnected: Boolean
    /**
     * The currently set timeout value, in **milliseconds**, used when waiting for an audio connection to be established.
     *
     * @return The currently set timeout.
     */
    /**
     * Sets the amount of time, in milliseconds, that will be used as the timeout when waiting for the audio connection
     * to successfully connect. The default value is 10 second (10,000 milliseconds).
     * <br></br>**Note**: If you set this value to 0, you can remove timeout functionality and JDA will wait FOREVER for the connection
     * to be established. This is no advised as it is possible that the connection may never be established.
     *
     * @param timeout
     * The amount of time, in milliseconds, that should be waited when waiting for the audio connection
     * to be established.
     */
    var connectTimeout: Long
    /**
     * The currently set [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler]. If there is
     * no sender currently set, this method will return `null`.
     *
     * @return The currently active [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler] or `null`.
     */
    /**
     * Sets the [net.dv8tion.jda.api.audio.AudioSendHandler]
     * that the manager will use to provide audio data to an audio connection.
     * <br></br>The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br></br>Setting this to null will remove the audio handler.
     *
     *
     * JDA recommends [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
     * as an [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler].
     * It provides a [demo](https://github.com/sedmelluq/lavaplayer/tree/master/demo-jda) targeted at JDA users.
     *
     * @param handler
     * The [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler] used to provide audio data.
     */
    @JvmField
    var sendingHandler: AudioSendHandler?
    /**
     * The currently set [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler].
     * If there is no receiver currently set, this method will return `null`.
     *
     * @return The currently active [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler] or `null`.
     */
    /**
     * Sets the [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler]
     * that the manager will use to process audio data received from an audio connection.
     *
     *
     * The handler provided here will persist between audio connection connect and disconnects.
     * Furthermore, you don't need to have an audio connection to set a handler.
     * When JDA sets up a new audio connection it will use the handler provided here.
     * <br></br>Setting this to null will remove the audio handler.
     *
     * @param handler
     * The [AudioReceiveHandler][net.dv8tion.jda.api.audio.AudioReceiveHandler] used to process
     * received audio data.
     */
    @JvmField
    var receivingHandler: AudioReceiveHandler?
    /**
     * The currently set [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener]
     * or `null` if no [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener] has been [set][.setConnectionListener].
     *
     * @return The current [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener] instance
     * for this AudioManager.
     */
    /**
     * Sets the [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener] for this AudioManager.
     * It will be informed about meta data of any audio connection established through this AudioManager.
     * Further information can be found in the [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener] documentation!
     *
     * @param listener
     * A [ConnectionListener][net.dv8tion.jda.api.audio.hooks.ConnectionListener] instance
     */
    var connectionListener: ConnectionListener?

    @get:Nonnull
    val connectionStatus: ConnectionStatus?
    /**
     * Whether audio connections from this AudioManager automatically reconnect
     *
     * @return Whether audio connections from this AudioManager automatically reconnect
     */
    /**
     * Sets whether audio connections from this AudioManager
     * should automatically reconnect or not. Default `true`
     *
     * @param shouldReconnect
     * Whether audio connections from this AudioManager should automatically reconnect
     */
    var isAutoReconnect: Boolean
    /**
     * Whether connections from this AudioManager are muted,
     * if this is `true` packages by the registered [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler]
     * will be ignored by Discord.
     *
     * @return Whether connections from this AudioManager are muted
     */
    /**
     * Set this to `true` if the current connection should be displayed as muted,
     * this will cause the [AudioSendHandler][net.dv8tion.jda.api.audio.AudioSendHandler] packages
     * to not be ignored by Discord!
     *
     * @param muted
     * Whether the connection should stop sending audio
     * and display as muted.
     */
    @JvmField
    var isSelfMuted: Boolean
    /**
     * Whether connections from this AudioManager are deafened.
     * <br></br>This does not include being muted, that value can be set individually from [.setSelfMuted]
     * and checked via [.isSelfMuted]
     *
     * @return True, if connections from this AudioManager are deafened
     */
    /**
     * Sets whether connections from this AudioManager should be deafened.
     * <br></br>This does not include being muted, that value can be set individually from [.setSelfMuted]
     * and checked via [.isSelfMuted]
     *
     * @param deafened
     * Whether connections from this AudioManager should be deafened.
     */
    @JvmField
    var isSelfDeafened: Boolean

    companion object {
        const val DEFAULT_CONNECTION_TIMEOUT: Long = 10000
        @JvmField
        val LOG = JDALogger.getLog(AudioManager::class.java)
    }
}
