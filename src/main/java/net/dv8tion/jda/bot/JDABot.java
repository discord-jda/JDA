/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.bot;

import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.RestAction;

public interface JDABot
{
    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this JDABot
     * 
     * @return The corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Retrieves the {@link net.dv8tion.jda.bot.entities.ApplicationInfo ApplicationInfo} for
     * the application that owns the logged in Bot-account.
     * <br>This contains information about the owner of the currently logged in bot account!
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.bot.entities.ApplicationInfo ApplicationInfo}
     *         <br>The {@link net.dv8tion.jda.bot.entities.ApplicationInfo ApplicationInfo} of the bot's application.
     */
    RestAction<ApplicationInfo> getApplicationInfo();
}
