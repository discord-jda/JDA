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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;

public interface VoiceState
{
    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} muted themselves.
     *
     * @return
     *      The Member's self-mute status
     */
    boolean isSelfMuted();

    /**
     * Returns whether the {@link net.dv8tion.jda.core.entities.Member Member} deafened themselves.
     *
     * @return
     *      the Member's self-deaf status
     */
    boolean isSelfDeafened();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this VoiceState
     *
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    AudioChannel getAudioChannel();

    String getSessionId();
}
