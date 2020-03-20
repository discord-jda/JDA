/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;

/**
 * Represents the type of account that is logged in.
 * <br>Used to differentiate between Bots and Client accounts.
 */
public enum AccountType
{
    /** An OAuth2 Bot which was created by an application */
    BOT,
    /**
     * A User-Account which can be used via the official Discord Client
     *
     * @deprecated This will be removed in a future version
     */
    @ForRemoval
    @Deprecated
    @DeprecatedSince("4.2.0")
    CLIENT
}
