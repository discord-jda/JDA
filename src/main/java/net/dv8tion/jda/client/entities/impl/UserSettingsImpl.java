/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

import java.util.*;

public class UserSettingsImpl implements UserSettings
{

    private final JDA api;

    private final Set<Guild> guildPositions = new LinkedHashSet<>(); //linked to keep order
    private final Set<Guild> restrictedGuilds = new LinkedHashSet<>();

    private OnlineStatus status = OnlineStatus.ONLINE;
    private Locale locale = Locale.getDefault();
    private DiscordTheme theme = DiscordTheme.UNKNOWN;

    private boolean allowEmailFriendRequests = false;
    private boolean convertEmoticons =         false;
    private boolean detectPlatformAccounts =   false;
    private boolean developerMode =            false;
    private boolean enableTTS =                false;
    private boolean showCurrentGame =          false;
    private boolean renderEmbeds =             false;
    private boolean messageDisplayCompact =    false;
    private boolean inlineEmbedMedia =         false;
    private boolean inlineAttachmentMedia =    false;

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
        return locale;
    }

    @Override
    public DiscordTheme getTheme()
    {
        return theme;
    }

    @Override
    public List<Guild> getGuildPositions()
    {
        return Collections.unmodifiableList(new LinkedList<>(guildPositions));
    }

    @Override
    public List<Guild> getRestrictedGuilds()
    {
        return Collections.unmodifiableList(new LinkedList<>(restrictedGuilds));
    }

    @Override
    public boolean isAllowEmailFriendRequest()
    {
        return allowEmailFriendRequests;
    }

    @Override
    public boolean isConvertEmoticons()
    {
        return convertEmoticons;
    }

    @Override
    public boolean isDetectPlatformAccounts()
    {
        return detectPlatformAccounts;
    }

    @Override
    public boolean isDeveloperMode()
    {
        return developerMode;
    }

    @Override
    public boolean isEnableTTS()
    {
        return enableTTS;
    }

    @Override
    public boolean isShowCurrentGame()
    {
        return showCurrentGame;
    }

    @Override
    public boolean isRenderEmbeds()
    {
        return renderEmbeds;
    }

    @Override
    public boolean isMessageDisplayCompact()
    {
        return messageDisplayCompact;
    }

    @Override
    public boolean isInlineEmbedMedia()
    {
        return inlineEmbedMedia;
    }

    @Override
    public boolean isInlineAttachmentMedia()
    {
        return inlineAttachmentMedia;
    }

    /* -- Setters -- */

    public UserSettingsImpl setStatus(OnlineStatus status)
    {
        this.status = status;
        return this;
    }

    public UserSettingsImpl setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    public UserSettingsImpl setTheme(DiscordTheme theme)
    {
        this.theme = theme;
        return this;
    }

    public UserSettingsImpl setAllowEmailFriendRequest(boolean allow)
    {
        this.allowEmailFriendRequests = allow;
        return this;
    }

    public UserSettingsImpl setConvertEmoticons(boolean convert)
    {
        this.convertEmoticons = convert;
        return this;
    }

    public UserSettingsImpl setDetectPlatformAccounts(boolean detectPlatformAccounts)
    {
        this.detectPlatformAccounts = detectPlatformAccounts;
        return this;
    }

    public UserSettingsImpl setDeveloperMode(boolean developerMode)
    {
        this.developerMode = developerMode;
        return this;
    }

    public UserSettingsImpl setEnableTTS(boolean enableTTS)
    {
        this.enableTTS = enableTTS;
        return this;
    }

    public UserSettingsImpl setShowCurrentGame(boolean showCurrentGame)
    {
        this.showCurrentGame = showCurrentGame;
        return this;
    }

    public UserSettingsImpl setRenderEmbeds(boolean renderEmbeds)
    {
        this.renderEmbeds = renderEmbeds;
        return this;
    }

    public UserSettingsImpl setMessageDisplayCompact(boolean messageDisplayCompact)
    {
        this.messageDisplayCompact = messageDisplayCompact;
        return this;
    }

    public UserSettingsImpl setInlineEmbedMedia(boolean inlineEmbedMedia)
    {
        this.inlineEmbedMedia = inlineEmbedMedia;
        return this;
    }

    public UserSettingsImpl setInlineAttachmentMedia(boolean inlineAttachmentMedia)
    {
        this.inlineAttachmentMedia = inlineAttachmentMedia;
        return this;
    }

    /* -- Set Getters -- */

    public Set<Guild> getGuildPositionSet()
    {
        return guildPositions;
    }

    public Set<Guild> getRestrictedGuildsSet()
    {
        return restrictedGuilds;
    }

    /* -- Object overrides -- */

    @Override
    public int hashCode()
    {
        return getJDA().getSelfUser().getId().hashCode();
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
