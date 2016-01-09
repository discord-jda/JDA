/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

public interface VoiceStatus
{
    /**
     * Returns whether the {@link net.dv8tion.jda.entities.User User} muted himself
     *
     * @return the User's self-mute status
     */
    boolean isMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.entities.User User} got muted by an Admin (server side)
     *
     * @return
     *      the User's server-mute status
     */
    boolean isServerMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.entities.User User} deafened himself
     *
     * @return
     *      the User's self-deaf status
     */
    boolean isDeaf();

    /**
     * Returns whether the {@link net.dv8tion.jda.entities.User User} got deafened by an Admin (server side)
     *
     * @return
     *      the User's server-deaf status
     */
    boolean isServerDeaf();

    /**
     * Returns the current {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} of the {@link net.dv8tion.jda.entities.User User}
     * If the {@link net.dv8tion.jda.entities.User User} is currently not in a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}, this returns null
     *
     * @return
     *      the User's VoiceChannel or null
     */
    VoiceChannel getChannel();

    /**
     * Returns the current {@link net.dv8tion.jda.entities.Guild Guild} of the {@link net.dv8tion.jda.entities.User User's} {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}
     * If the {@link net.dv8tion.jda.entities.User User} is currently not in a {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel}, this returns null
     *
     * @return
     *      the User's VoiceChannel's Guild or null
     */
    Guild getGuild();

    /**
     * Returns the {@link net.dv8tion.jda.entities.User User} corresponding to this VoiceStatus Object
     * (Backreference)
     *
     * @return
     *      the User that holds this VoiceStatus
     */
    User getUser();
}
