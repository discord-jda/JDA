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

package net.dv8tion.jda.client.handle;

import net.dv8tion.jda.client.entities.UserSettings;
import net.dv8tion.jda.client.entities.impl.UserSettingsImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Set;

public class UserSettingsUpdateHandler extends SocketHandler
{

    public UserSettingsUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        // ignore if not client account
        if (api.getAccountType() != AccountType.CLIENT)
            return null;
        UserSettingsImpl settingsObj = (UserSettingsImpl) api.asClient().getSettings();

        if (!content.isNull("status"))
            settingsObj.setStatus(OnlineStatus.fromKey(content.getString("status")));
        if (!content.isNull("locale"))
            settingsObj.setLocale(Locale.forLanguageTag(content.getString("locale")));
        if (!content.isNull("theme"))
            settingsObj.setTheme(UserSettings.DiscordTheme.fromKey(content.getString("theme")));

        if (!content.isNull("guild_positions"))
        {
            JSONArray arr = content.getJSONArray("guild_positions");
            handleGuildIds("guild_positions", arr, settingsObj.getGuildPositionSet());
        }
        if (!content.isNull("restricted_guilds"))
        {
            JSONArray arr = content.getJSONArray("restricted_guilds");
            handleGuildIds("restricted_guilds", arr, settingsObj.getRestrictedGuildsSet());
        }

        if (!content.isNull("allow_email_friend_request"))
            settingsObj.setAllowEmailFriendRequest(content.getBoolean("allow_email_friend_request"));
        if (!content.isNull("convert_emoticons"))
            settingsObj.setConvertEmoticons(content.getBoolean("convert_emoticons"));
        if (!content.isNull("detect_platform_accounts"))
            settingsObj.setDetectPlatformAccounts(content.getBoolean("detect_platform_accounts"));
        if (!content.isNull("developer_mode"))
            settingsObj.setDeveloperMode(content.getBoolean("developer_mode"));
        if (!content.isNull("enable_tts_command"))
            settingsObj.setEnableTTS(content.getBoolean("enable_tts_command"));
        if (!content.isNull("show_current_game"))
            settingsObj.setShowCurrentGame(content.getBoolean("show_current_game"));
        if (!content.isNull("render_embeds"))
            settingsObj.setRenderEmbeds(content.getBoolean("render_embeds"));
        if (!content.isNull("message_display_compact"))
            settingsObj.setMessageDisplayCompact(content.getBoolean("message_display_compact"));
        if (!content.isNull("inline_embed_media"))
            settingsObj.setInlineEmbedMedia(content.getBoolean("inline_embed_media"));
        if (!content.isNull("inline_attachment_media"))
            settingsObj.setInlineAttachmentMedia(content.getBoolean("inline_attachment_media"));
        return null;
    }

    private void handleGuildIds(String name, JSONArray array, Set<Guild> guilds)
    {
        for (int i = 0; i < array.length(); i++)
        {
            String id = array.getString(i);
            Guild guild = api.getGuildById(id);
            if (guild == null)
            {
                WebSocketClient.LOG.debug(String.format("Received %s id for non existing guild: %s", name, id));
                continue;
            }
            guilds.add(guild);
        }
    }
}
