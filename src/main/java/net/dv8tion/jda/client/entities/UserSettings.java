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

package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;

import java.util.List;
import java.util.Locale;

public interface UserSettings
{

    JDA getJDA();

    OnlineStatus getStatus();
    Locale getLocale();
    //getTheme() : ?

    List<Guild> getGuildPositions();
    List<Guild> getRestrictedGuilds();

    boolean isAllowEmailFriendRequest();
    boolean isConvertEmoticons();
    boolean isDetectPlatformAccounts();
    boolean isDeveloperMode();
    boolean isEnableTTS();
    boolean isShowCurrentGame();
    boolean isRenderEmbeds();
    boolean isMessageDisplayCompact();
    boolean isInlineEmbedMedia();
    boolean isInlineAttachmentMedia();
}
