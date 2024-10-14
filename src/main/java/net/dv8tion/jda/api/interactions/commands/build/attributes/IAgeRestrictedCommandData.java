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

package net.dv8tion.jda.api.interactions.commands.build.attributes;

import javax.annotation.Nonnull;

//TODO docs
public interface IAgeRestrictedCommandData extends INamedCommandData
{
    /**
     * Sets whether this command should only be usable in NSFW (age-restricted) channels.
     * <br>Default: false
     *
     * <p>Note: Age-restricted commands will not show up in direct messages by default unless the user enables them in their settings.
     *
     * @param  nsfw
     *         True, to make this command nsfw
     *
     * @return The builder instance, for chaining
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    @Nonnull
    IAgeRestrictedCommandData setNSFW(boolean nsfw);

    /**
     * Whether this command should only be usable in NSFW (age-restricted) channels
     *
     * @return True, if this command is restricted to NSFW channels
     *
     * @see <a href="https://support.discord.com/hc/en-us/articles/10123937946007" target="_blank">Age-Restricted Commands FAQ</a>
     */
    boolean isNSFW();
}
