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

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nonnull;

/**
 * Indicates that you login to your discord account with a mobile device for the first time. (client-only)
 *
 * <p>Can be used to detect that your account was used with a mobile device.
 *
 * <p>Identifier: {@code mobile}
 */
public class SelfUpdateMobileEvent extends GenericSelfUpdateEvent<Boolean>
{
    public static final String IDENTIFIER = "mobile";

    public SelfUpdateMobileEvent(@Nonnull JDA api, long responseNumber, boolean wasMobile)
    {
        super(api, responseNumber, wasMobile, !wasMobile, IDENTIFIER);
    }

    /**
     * The old mobile status. <i>Should</i> always be {@code false}.
     *
     * @return The mobile status.
     */
    public boolean wasMobile()
    {
        return getOldValue();
    }

    @Nonnull
    @Override
    public Boolean getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Boolean getNewValue()
    {
        return super.getNewValue();
    }
}
