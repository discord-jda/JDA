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

package net.dv8tion.jda.test.entities.guild;

import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GuildLevelCacheTest extends IntegrationTest
{
    @Mock
    UserImpl user;
    @Mock
    SelfUserImpl selfUser;
    @Mock
    VoiceChannelImpl channel;

    @BeforeEach
    void setupMocks()
    {
        when(user.getJDA()).thenReturn(jda);
        when(user.getIdLong()).thenReturn(Constants.MINN_USER_ID);

        when(jda.getSelfUser()).thenReturn(selfUser);
        when(selfUser.getJDA()).thenReturn(jda);
        when(selfUser.getIdLong()).thenReturn(Constants.BUTLER_USER_ID);
    }

    private GuildImpl getGuild()
    {
        GuildImpl guild = new GuildImpl(jda, random.nextLong());
        MemberCacheViewImpl membersView = guild.getMembersView();
        try (UnlockHook hook = membersView.writeLock())
        {
            membersView.getMap().put(Constants.BUTLER_USER_ID, new MemberImpl(guild, selfUser));
        }

        return guild;
    }

    private void assertThatVoiceStateIsCached(GuildImpl guild, MemberImpl member)
    {
        GuildVoiceStateImpl voiceState = member.getVoiceState();

        assertThat(voiceState).isNotNull();
        assertThat(guild.getVoiceStateView().get(member.getIdLong())).isSameAs(voiceState);

        if (voiceState.getChannel() != null)
            assertThat(guild.getConnectedMembers(channel)).contains(member);
    }

    private void assertThatVoiceStateIsNotCached(GuildImpl guild, MemberImpl member)
    {
        GuildVoiceStateImpl voiceState = member.getVoiceState();
        assertThat(guild.getVoiceStateView().get(member.getIdLong())).isNull();

        if (voiceState != null && voiceState.getChannel() != null)
            assertThat(guild.getConnectedMembers(channel)).isEmpty();
    }

    @Nested
    class VoiceStateCacheDisabledTest
    {
        @BeforeEach
        void setupCacheFlags()
        {
            withCacheFlags(EnumSet.noneOf(CacheFlag.class));
        }


        @Test
        void shouldReturnNullForOtherMembers()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, user);

            assertThat(member.getVoiceState()).isNull();
        }

        @Test
        void shouldReturnNonNullForSelfUser()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, selfUser);

            assertThat(member.getVoiceState()).isNotNull();
        }

        @Test
        void shouldNotCacheVoiceStateForConnectedMembers()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, user);

            GuildVoiceStateImpl voiceState = new GuildVoiceStateImpl(member);
            voiceState.setConnectedChannel(channel);

            guild.handleVoiceStateUpdate(voiceState);
            assertThatVoiceStateIsNotCached(guild, member);
        }

        @Test
        void shouldCacheVoiceStateForSelfMember()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = (MemberImpl) guild.getSelfMember();

            GuildVoiceStateImpl voiceState = member.getVoiceState();
            assertThat(voiceState).isNotNull();
            voiceState.setConnectedChannel(channel);

            guild.handleVoiceStateUpdate(voiceState);
            assertThatVoiceStateIsCached(guild, member);
        }
    }

    @Nested
    class VoiceStateCacheEnabledTest
    {
        @BeforeEach
        void setupCacheFlags()
        {
            withCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE));
        }


        @Test
        void shouldReturnNonNullForOtherMembers()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, user);

            assertThat(member.getVoiceState()).isNotNull();
        }

        @Test
        void shouldReturnNonNullForSelfUser()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, selfUser);

            assertThat(member.getVoiceState()).isNotNull();
        }

        @Test
        void shouldCacheVoiceStateForConnectedMembers()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, user);

            GuildVoiceStateImpl voiceState = member.getVoiceState();
            assertThat(voiceState).isNotNull();
            voiceState.setConnectedChannel(channel);

            guild.handleVoiceStateUpdate(voiceState);
            assertThatVoiceStateIsCached(guild, member);
        }

        @Test
        void shouldUncacheVoiceStateForDisconnectedMembers()
        {
            GuildImpl guild = getGuild();
            MemberImpl member = new MemberImpl(guild, user);

            GuildVoiceStateImpl voiceState = member.getVoiceState();
            assertThat(voiceState).isNotNull();

            voiceState.setConnectedChannel(channel);

            guild.handleVoiceStateUpdate(voiceState);
            assertThatVoiceStateIsCached(guild, member);

            voiceState.setConnectedChannel(null);

            guild.handleVoiceStateUpdate(voiceState);
            assertThatVoiceStateIsNotCached(guild, member);
        }
    }
}
