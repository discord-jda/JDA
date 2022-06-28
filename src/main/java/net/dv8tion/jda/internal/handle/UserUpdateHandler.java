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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.events.self.SelfUpdateAvatarEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateMFAEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateNameEvent;
import net.dv8tion.jda.api.events.self.SelfUpdateVerifiedEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;

import java.util.Objects;

public class UserUpdateHandler extends SocketHandler
{
    public UserUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        SelfUserImpl self = (SelfUserImpl) getJDA().getSelfUser();

        String name = content.getString("username");
        String discriminator = content.getString("discriminator");
        String avatarId = content.getString("avatar", null);
        Boolean verified = content.hasKey("verified") ? content.getBoolean("verified") : null;
        Boolean mfaEnabled = content.hasKey("mfa_enabled") ? content.getBoolean("mfa_enabled") : null;

        //Client only
        String email = content.getString("email", null);
        Boolean mobile = content.hasKey("mobile") ? content.getBoolean("mobile") : null; // mobile device
        Boolean nitro = content.hasKey("premium") ? content.getBoolean("premium") : null; // nitro
        String phoneNumber = content.getString("phone", null); // verified phone number (verification level !)

        if (!Objects.equals(name, self.getName()) || !Objects.equals(discriminator, self.getDiscriminator()))
        {
            String oldName = self.getName();
            self.setName(name);
            getJDA().handleEvent(
                new SelfUpdateNameEvent(
                    getJDA(), responseNumber,
                    oldName));
        }

        if (!Objects.equals(avatarId, self.getAvatarId()))
        {
            String oldAvatarId = self.getAvatarId();
            self.setAvatarId(avatarId);
            getJDA().handleEvent(
                new SelfUpdateAvatarEvent(
                    getJDA(), responseNumber,
                    oldAvatarId));
        }

        if (verified != null && verified != self.isVerified())
        {
            boolean wasVerified = self.isVerified();
            self.setVerified(verified);
            getJDA().handleEvent(
                new SelfUpdateVerifiedEvent(
                    getJDA(), responseNumber,
                    wasVerified));
        }

        if (mfaEnabled != null && mfaEnabled != self.isMfaEnabled())
        {
            boolean wasMfaEnabled = self.isMfaEnabled();
            self.setMfaEnabled(mfaEnabled);
            getJDA().handleEvent(
                new SelfUpdateMFAEvent(
                    getJDA(), responseNumber,
                    wasMfaEnabled));
        }
        return null;
    }
}
