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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.ActivityFlag;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;

public class RichPresenceImpl extends ActivityImpl implements RichPresence
{
    protected final long applicationId;

    protected final Party party;
    protected final String details;
    protected final Image largeImage;
    protected final Image smallImage;
    protected final String sessionId;
    protected final String syncId;
    protected final int flags;

    protected RichPresenceImpl(
            ActivityType type, String name, String url, long applicationId,
            EmojiUnion emoji, Party party, String details, String state, Timestamps timestamps, String syncId, String sessionId,
            int flags, String largeImageKey, String largeImageText, String smallImageKey, String smallImageText)
    {
        super(name, state, url, type, timestamps, emoji);
        this.applicationId = applicationId;
        this.party = party;
        this.details = details;
        this.sessionId = sessionId;
        this.syncId = syncId;
        this.flags = flags;
        this.largeImage = largeImageKey != null ? new Image(applicationId, largeImageKey, largeImageText) : null;
        this.smallImage = smallImageKey != null ? new Image(applicationId, smallImageKey, smallImageText) : null;
    }

    @Override
    public boolean isRich()
    {
        return true;
    }

    @Override
    public RichPresence asRichPresence()
    {
        return this;
    }

    @Override
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    @Nonnull
    @Override
    public String getApplicationId()
    {
        return Long.toUnsignedString(applicationId);
    }

    @Nullable
    @Override
    public String getSessionId()
    {
        return sessionId;
    }

    @Nullable
    @Override
    public String getSyncId()
    {
        return syncId;
    }

    @Override
    public int getFlags()
    {
        return flags;
    }

    @Override
    public EnumSet<ActivityFlag> getFlagSet()
    {
        return ActivityFlag.getFlags(getFlags());
    }

    @Nullable
    @Override
    public String getDetails()
    {
        return details;
    }

    @Nullable
    @Override
    public Party getParty()
    {
        return party;
    }

    @Nullable
    @Override
    public Image getLargeImage()
    {
        return largeImage;
    }

    @Nullable
    @Override
    public Image getSmallImage()
    {
        return smallImage;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .addMetadata("applicationId", applicationId)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(applicationId, state, details, party, sessionId, syncId, flags, timestamps, largeImage, smallImage);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof RichPresenceImpl))
            return false;
        RichPresenceImpl p = (RichPresenceImpl) o;
        return applicationId == p.applicationId
               && Objects.equals(name, p.name)
               && Objects.equals(url, p.url)
               && Objects.equals(type, p.type)
               && Objects.equals(state, p.state)
               && Objects.equals(details, p.details)
               && Objects.equals(party, p.party)
               && Objects.equals(sessionId, p.sessionId)
               && Objects.equals(syncId, p.syncId)
               && Objects.equals(flags, p.flags)
               && Objects.equals(timestamps, p.timestamps)
               && Objects.equals(largeImage, p.largeImage)
               && Objects.equals(smallImage, p.smallImage);
    }
}
