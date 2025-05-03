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

package net.dv8tion.jda.test.restaction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.internal.requests.restaction.ForumPostActionImpl;
import net.dv8tion.jda.internal.requests.restaction.ThreadChannelActionImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ThreadCreateActionTest extends IntegrationTest
{
    @Mock
    private Guild guild;
    @Mock
    private IPostContainer forum;
    @Mock
    private TextChannel textChannel;
    @Mock
    private Member selfMember;

    @BeforeEach
    void setupMocks()
    {
        mockChannel(forum);
        mockChannel(textChannel);
        when(guild.getSelfMember()).thenReturn(selfMember);
        when(selfMember.hasPermission(any(GuildChannel.class), any(Permission.class))).thenReturn(true);
    }

    private void mockChannel(GuildChannel channel)
    {
        when(channel.getId()).thenReturn(Long.toUnsignedString(Constants.CHANNEL_ID));
        when(channel.getJDA()).thenReturn(jda);
        when(channel.getGuild()).thenReturn(guild);
    }

    @Test
    void testMinimalForumPost()
    {
        ForumPostActionImpl action = new ForumPostActionImpl(forum, "post title", new MessageCreateBuilder().setContent("test content"));

        assertThatRequestFrom(action)
            .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/threads")
            .hasMethod(Method.POST)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testFullForumPost()
    {
        ForumPostActionImpl action = new ForumPostActionImpl(forum, "post title", new MessageCreateBuilder());

        String tagId = randomSnowflake();

        action
            .setName("post title by setter")
            .setTags(ForumTagSnowflake.fromId(tagId))
            .setContent("test message content")
            .setSlowmode(1337);

        verify(selfMember, times(1)).hasPermission(eq(forum), eq(Permission.MANAGE_THREADS));

        assertThatRequestFrom(action)
            .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/threads")
            .hasMethod(Method.POST)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testMinimalStartThread()
    {
        ThreadChannelActionImpl action = new ThreadChannelActionImpl(textChannel, "thread title", ChannelType.TEXT);

        assertThatRequestFrom(action)
            .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/threads")
            .hasMethod(Method.POST)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }

    @Test
    void testFullStartThread()
    {
        ThreadChannelActionImpl action = new ThreadChannelActionImpl(textChannel, "thread title", ChannelType.TEXT);

        action
            .setName("post title by setter")
            .setSlowmode(1337);

        verify(selfMember, times(1)).hasPermission(eq(textChannel), eq(Permission.MANAGE_THREADS));

        assertThatRequestFrom(action)
            .hasCompiledRoute("channels/" + Constants.CHANNEL_ID + "/threads")
            .hasMethod(Method.POST)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }
}
