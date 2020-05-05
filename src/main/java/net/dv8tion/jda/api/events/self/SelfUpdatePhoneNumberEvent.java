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

package net.dv8tion.jda.api.events.self;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the phone number associated with your account changed. (client-only)
 *
 * <p>Can be used to retrieve the old phone number.
 *
 * <p>Identifier: {@code phone}
 *
 * @deprecated This is no longer supported
 */
@Deprecated
@ForRemoval
@DeprecatedSince("4.2.0")
public class SelfUpdatePhoneNumberEvent extends GenericSelfUpdateEvent<String>
{
    public static final String IDENTIFIER = "phone";

    public SelfUpdatePhoneNumberEvent(@Nonnull JDA api, long responseNumber, @Nullable String oldPhoneNumber)
    {
        super(api, responseNumber, oldPhoneNumber, api.getSelfUser().getPhoneNumber(), IDENTIFIER);
    }

    /**
     * The old phone number or {@code null} if no phone number was previously set.
     *
     * @return The old phone number or {@code null}.
     */
    @Nullable
    public String getOldPhoneNumber()
    {
        return getOldValue();
    }

    /**
     * The new phone number.
     *
     * @return The new phone number
     */
    @Nullable
    public String getNewPhoneNumber()
    {
        return getNewValue();
    }
}
