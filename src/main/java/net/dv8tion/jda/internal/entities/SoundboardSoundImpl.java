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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.SoundboardSoundManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.managers.SoundboardSoundManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SoundboardSoundImpl implements SoundboardSound
{
    private final JDA api;
    private final long id;
    private final String name;
    private final double volume;
    private final EmojiUnion emoji;
    private final Guild guild;
    private final boolean available;
    private final User user;

    public SoundboardSoundImpl(JDA api, long id, String name, double volume, EmojiUnion emoji, Guild guild, boolean available, User user)
    {
        this.api = api;
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.emoji = emoji;
        this.guild = guild;
        this.available = available;
        this.user = user;
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public double getVolume()
    {
        return volume;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    public boolean isAvailable()
    {
        return available;
    }

    @Nullable
    @Override
    public User getUser()
    {
        return user;
    }

    @Nonnull
    @Override
    public RestAction<Void> sendTo(VoiceChannel channel)
    {
        Checks.notNull(channel, "Channel");

        // Check available
        if (!isAvailable())
            throw new IllegalStateException("Cannot send an unavailable sound");

        // Check speak permissions
        final Guild targetGuild = channel.getGuild();
        if (!targetGuild.getSelfMember().hasPermission(channel, Permission.VOICE_SPEAK))
            throw new InsufficientPermissionException(channel, Permission.VOICE_SPEAK);
        if (!targetGuild.getSelfMember().hasPermission(channel, Permission.VOICE_USE_SOUNDBOARD))
            throw new InsufficientPermissionException(channel, Permission.VOICE_USE_SOUNDBOARD);

        if (!targetGuild.equals(getGuild()) && !targetGuild.getSelfMember().hasPermission(channel, Permission.VOICE_USE_EXTERNAL_SOUNDS))
            throw new InsufficientPermissionException(channel, Permission.VOICE_USE_EXTERNAL_SOUNDS);

        // Check voice state if possible
        if (!channel.equals(targetGuild.getAudioManager().getConnectedChannel()))
            throw new IllegalStateException("You must be connected to the voice channel you want to send the sound effect to");
        final GuildVoiceState voiceState = targetGuild.getSelfMember().getVoiceState();
        if (voiceState != null)
        {
            if (voiceState.isSuppressed())
                throw new IllegalStateException("You cannot send sound effects while you are being suppressed");
            if (voiceState.isDeafened())
                throw new IllegalStateException("You cannot send sound effects while you are deafened");
            if (voiceState.isMuted())
                throw new IllegalStateException("You cannot send sound effects while you are muted");
        }

        // Send
        DataObject data = DataObject.empty()
                .put("sound_id", getId());

        if (guild != null)
            data.put("source_guild_id", guild.getId());

        return new RestActionImpl<>(api, Route.SoundboardSounds.SEND_SOUNDBOARD_SOUND.compile(channel.getId()), data);
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        Checks.check(getGuild() != null, "Cannot delete default soundboard sounds");
        checkEditPermissions();

        final Route.CompiledRoute route = Route.SoundboardSounds.DELETE_GUILD_SOUNDBOARD_SOUNDS.compile(guild.getId(), getId());
        return new AuditableRestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public SoundboardSoundManager getManager() {
        Checks.check(getGuild() != null, "Cannot delete default soundboard sounds");
        checkEditPermissions();

        return new SoundboardSoundManagerImpl(this);
    }

    private void checkEditPermissions()
    {
        final Member selfMember = guild.getSelfMember();
        if (Objects.equals(getUser(), selfMember.getUser()))
            PermissionUtil.requireAnyPermission(selfMember, Permission.CREATE_GUILD_EXPRESSIONS, Permission.MANAGE_GUILD_EXPRESSIONS);
        else if (!selfMember.hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_GUILD_EXPRESSIONS);
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof SoundboardSoundImpl))
            return false;
        return ((SoundboardSoundImpl) obj).getIdLong() == this.id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }
}
