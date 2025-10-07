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

package net.dv8tion.jda.test.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.managers.GuildManagerImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.dv8tion.jda.api.requests.Method.PATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GuildManagerTest extends IntegrationTest
{
    @Mock
    private Guild guild;
    @Mock
    private Member selfMember;
    @Mock
    private TextChannel textChannel;
    @Mock
    private VoiceChannel voiceChannel;

    @BeforeEach
    void setupMocks()
    {
        when(guild.getJDA()).thenReturn(jda);
        when(guild.getId()).thenReturn(Long.toUnsignedString(Constants.GUILD_ID));
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(selfMember.hasPermission(any(Permission[].class))).thenReturn(true);
        when(textChannel.getGuild()).thenReturn(guild);
        when(textChannel.getId()).thenReturn(Long.toUnsignedString(Constants.CHANNEL_ID));
        when(voiceChannel.getGuild()).thenReturn(guild);
        when(voiceChannel.getId()).thenReturn(Long.toUnsignedString(Constants.CHANNEL_ID));
    }

    @Test
    void callNoSetters()
    {
        GuildManagerImpl manager = new GuildManagerImpl(guild);
        manager.queue();
        assertThatNoRequestsWereSent();
    }

    @Test
    void callEverySetter()
    {
        Set<String> features = new HashSet<>(Arrays.asList("BANNER", "VERIFIED", "INVITE_SPLASH"));
        when(guild.getFeatures()).thenReturn(features);

        Set<String> ignoredSetters = new HashSet<>(Arrays.asList(
            "setFeatures", "setSystemChannelFlags", "setInvitesDisabled"
        ));

        AtomicInteger calledSetters = new AtomicInteger(0);
        GuildManagerImpl manager = new GuildManagerImpl(guild);
        for (Method method : GuildManager.class.getDeclaredMethods())
        {
            if (ignoredSetters.contains(method.getName()))
                continue;

            if (method.getName().startsWith("set") && method.getParameterCount() == 1)
            {
                assertThatNoException().describedAs("call " + method.getName()).isThrownBy(() -> {
                    Object mocked = getParameterForSetter(method);
                    method.invoke(manager, mocked);
                    calledSetters.incrementAndGet();
                });
            }
        }

        DataObject expectedBody = DataObject.empty()
            .put("afk_channel_id", "125227483518861312")
            .put("afk_timeout", 0)
            .put("banner", null)
            .put("default_message_notifications", 0)
            .put("description", "test")
            .put("explicit_content_filter", 0)
            .put("icon", null)
            .put("name", "test")
            .put("premium_progress_bar_enabled", true)
            .put("public_updates_channel_id", "125227483518861312")
            .put("safety_alerts_channel_id", "125227483518861312")
            .put("rules_channel_id", "125227483518861312")
            .put("splash", null)
            .put("system_channel_id", "125227483518861312")
            .put("verification_level", 0);

        assertThatRequestFrom(manager)
            .hasMethod(PATCH)
            .hasBodyEqualTo(expectedBody)
            .whenQueueCalled();

        // Every setter should result in a new key in the body, make sure you updated finalizeData
        assertThat(expectedBody.keys()).hasSize(calledSetters.get());
    }

    private Object getParameterForSetter(Method setter)
    {
        Class<?> paramType = setter.getParameters()[0].getType();
        if (paramType == String.class)
            return "test";
        if (paramType == Boolean.TYPE)
            return true;
        if (paramType == Integer.TYPE)
            return 42;
        if (TextChannel.class.isAssignableFrom(paramType))
            return textChannel;
        if (VoiceChannel.class.isAssignableFrom(paramType))
            return voiceChannel;
        return mock(paramType);
    }
}
