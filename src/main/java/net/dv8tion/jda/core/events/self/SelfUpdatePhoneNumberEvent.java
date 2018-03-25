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

package net.dv8tion.jda.core.events.self;

import net.dv8tion.jda.core.JDA;

/**
 * Indicates that the phone number associated with your account changed. (client-only)
 *
 * <p>Can be used to retrieve the old phone number.
 *
 * <p>Identifier: {@code phone}
 */
public class SelfUpdatePhoneNumberEvent extends GenericSelfUpdateEvent<String>
{
    public static final String IDENTIFIER = "phone";

    public SelfUpdatePhoneNumberEvent(JDA api, long responseNumber, String oldPhoneNumber)
    {
        super(api, responseNumber, oldPhoneNumber, api.getSelfUser().getPhoneNumber(), IDENTIFIER);
    }

    /**
     * The old phone number or {@code null} if no phone number was previously set.
     *
     * @return The old phone number or {@code null}.
     */
    public String getOldPhoneNumber()
    {
        return getOldValue();
    }

    /**
     * The new phone number.
     *
     * @return The new phone number
     */
    public String getNewPhoneNumber()
    {
        return getNewValue();
    }
}
