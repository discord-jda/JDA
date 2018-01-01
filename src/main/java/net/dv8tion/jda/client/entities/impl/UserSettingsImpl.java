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

package net.dv8tion.jda.client.entities.impl;

import net.dv8tion.jda.client.entities.UserSettings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;

import java.util.List;
import java.util.Locale;

public class UserSettingsImpl implements UserSettings
{

    private final JDA api;

    private OnlineStatus status = OnlineStatus.UNKNOWN;

    public UserSettingsImpl(JDA api)
    {
        this.api = api;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }


    @Override
    public OnlineStatus getStatus()
    {
        return status;
    }

    @Override
    public Locale getLocale()
    {
        return null;
    }

    @Override
    public List<Guild> getGuildPositions()
    {
        return null;
    }

    @Override
    public List<Guild> getRestrictedGuilds()
    {
        return null;
    }

    @Override
    public boolean isAllowEmailFriendRequest()
    {
        return false;
    }

    @Override
    public boolean isConvertEmoticons()
    {
        return false;
    }

    @Override
    public boolean isDetectPlatformAccounts()
    {
        return false;
    }

    @Override
    public boolean isDeveloperMode()
    {
        return false;
    }

    @Override
    public boolean isEnableTTS()
    {
        return false;
    }

    @Override
    public boolean isShowCurrentGame()
    {
        return false;
    }

    @Override
    public boolean isRenderEmbeds()
    {
        return false;
    }

    @Override
    public boolean isMessageDisplayCompact()
    {
        return false;
    }

    @Override
    public boolean isInlineEmbedMedia()
    {
        return false;
    }

    @Override
    public boolean isInlineAttachmentMedia()
    {
        return false;
    }

    /* -- Setters -- */

    public UserSettingsImpl setStatus(OnlineStatus status)
    {
        this.status = status;
        return this;
    }

    /* -- Object overrides -- */

    @Override
    public int hashCode()
    {
        return Long.hashCode(getJDA().getSelfUser().getIdLong());
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof UserSettingsImpl && getJDA().equals(((UserSettingsImpl) obj).getJDA());
    }

    @Override
    public String toString()
    {
        return "UserSettings(" + getJDA().getSelfUser() + ")";
    }
}
