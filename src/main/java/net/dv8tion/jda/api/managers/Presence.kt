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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * The Presence associated with the provided JDA instance
 *
 * @since  3.0
 */
interface Presence {
    @get:Nonnull
    val jDA: JDA?

    /**
     * Sets the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for this session
     *
     * @throws IllegalArgumentException
     * if the provided OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     *
     * @param  status
     * the [OnlineStatus][net.dv8tion.jda.api.OnlineStatus]
     * to be used (OFFLINE/null -&gt; INVISIBLE)
     */
    @JvmField
    @get:Nonnull
    var status: OnlineStatus?
    /**
     * The current Activity for this session.
     * <br></br>This might not be what the Discord Client displays due to session clashing!
     *
     * @return The [Activity][net.dv8tion.jda.api.entities.Activity]
     * of the current session or null if no activity is set
     */
    /**
     * Sets the [Activity][net.dv8tion.jda.api.entities.Activity] for this session.
     * <br></br>An Activity can be retrieved via [net.dv8tion.jda.api.entities.Activity.playing].
     * For streams you provide a valid streaming url as second parameter
     *
     *
     * Examples:
     * <br></br>`presence.setActivity(Activity.playing("Thrones"));`
     * <br></br>`presence.setActivity(Activity.streaming("Thrones", "https://twitch.tv/EasterEggs"));`
     *
     * @param  activity
     * An [Activity][net.dv8tion.jda.api.entities.Activity] instance or null to reset
     *
     * @see net.dv8tion.jda.api.entities.Activity.playing
     * @see net.dv8tion.jda.api.entities.Activity.streaming
     */
    @JvmField
    var activity: Activity?
    /**
     * Whether the current session is marked as afk or not.
     *
     *
     * This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @return True if this session is marked as afk
     */
    /**
     * Sets whether this session should be marked as afk or not
     *
     *
     * This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @param idle
     * boolean
     */
    @JvmField
    var isIdle: Boolean

    /**
     * Sets all presence fields of this session.
     *
     * @param  status
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for this session
     * (See [.setStatus])
     * @param  activity
     * The [Activity][net.dv8tion.jda.api.entities.Activity] for this session
     * (See [.setActivity] for more info)
     * @param  idle
     * Whether to mark this session as idle (useful for client accounts [.setIdle])
     *
     * @throws java.lang.IllegalArgumentException
     * If the specified OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     */
    fun setPresence(status: OnlineStatus?, activity: Activity?, idle: Boolean)

    /**
     * Sets two presence fields of this session.
     * <br></br>The third field stays untouched.
     *
     * @param  status
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for this session
     * (See [.setStatus])
     * @param  activity
     * The [Activity][net.dv8tion.jda.api.entities.Activity] for this session
     * (See [.setActivity] for more info)
     *
     * @throws java.lang.IllegalArgumentException
     * If the specified OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     */
    fun setPresence(status: OnlineStatus?, activity: Activity?)

    /**
     * Sets two presence fields of this session.
     * <br></br>The third field stays untouched.
     *
     * @param  status
     * The [OnlineStatus][net.dv8tion.jda.api.OnlineStatus] for this session
     * (See [.setStatus])
     * @param  idle
     * Whether to mark this session as idle (useful for client accounts [.setIdle])
     *
     * @throws java.lang.IllegalArgumentException
     * If the specified OnlineStatus is [UNKNOWN][net.dv8tion.jda.api.OnlineStatus.UNKNOWN]
     */
    fun setPresence(status: OnlineStatus?, idle: Boolean)

    /**
     * Sets two presence fields of this session.
     * <br></br>The third field stays untouched.
     *
     * @param  activity
     * The [Activity][net.dv8tion.jda.api.entities.Activity] for this session
     * (See [.setActivity] for more info)
     * @param  idle
     * Whether to mark this session as idle (useful for client accounts [.setIdle])
     */
    fun setPresence(activity: Activity?, idle: Boolean)
}
