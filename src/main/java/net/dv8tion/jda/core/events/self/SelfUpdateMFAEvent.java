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
 * Indicates that the mfa level of the current user changed.
 * <br>This is relevant for elevated permissions (guild moderating/managing).
 *
 * <p>Can be used to retrieve the old mfa level.
 *
 * <p>Identifier: {@code mfa_enabled}
 */
public class SelfUpdateMFAEvent extends GenericSelfUpdateEvent<Boolean>
{
    public static final String IDENTIFIER = "mfa_enabled";

    public SelfUpdateMFAEvent(JDA api, long responseNumber, boolean wasMfaEnabled)
    {
        super(api, responseNumber, wasMfaEnabled, !wasMfaEnabled, IDENTIFIER);
    }

    /**
     * Whether MFA was previously enabled or not
     *
     * @return True, if the account had MFA enabled prior to this event
     */
    public boolean wasMfaEnabled()
    {
        return getOldValue();
    }
}
