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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.ISoundboardSoundChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.SoundboardSoundManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.managers.SoundboardSoundManagerImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundboardSoundImpl extends SoundboardSoundSnowflakeImpl implements SoundboardSound {
    private final JDA api;
    private final String name;
    private final double volume;
    private final EmojiUnion emoji;
    private final Guild guild;
    private final boolean available;
    private final User user;

    public SoundboardSoundImpl(
            JDA api, long id, String name, double volume, EmojiUnion emoji, Guild guild, boolean available, User user) {
        super(id);
        this.api = api;
        this.name = name;
        this.volume = volume;
        this.emoji = emoji;
        this.guild = guild;
        this.available = available;
        this.user = user;
    }

    @Nonnull
    @Override
    public JDA getJDA() {
        return api;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji() {
        return emoji;
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return guild;
    }

    public boolean isAvailable() {
        return available;
    }

    @Nullable
    @Override
    public User getUser() {
        return user;
    }

    @Nonnull
    @Override
    public RestAction<Void> sendTo(@Nonnull ISoundboardSoundChannel channel) {
        Checks.notNull(channel, "Channel");

        // Check available
        if (!isAvailable()) {
            throw new IllegalStateException("Cannot send an unavailable sound");
        }

        // Check additional speak permissions
        // This isn't done in IVoiceStatusChannel as it could throw false positives
        Guild targetGuild = channel.getGuild();
        if (!targetGuild.equals(getGuild())
                && !targetGuild.getSelfMember().hasPermission(channel, Permission.VOICE_USE_EXTERNAL_SOUNDS)) {
            throw new InsufficientPermissionException(channel, Permission.VOICE_USE_EXTERNAL_SOUNDS);
        }

        return channel.sendSoundboardSound(this, this.getGuild().getIdLong());
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete() {
        Checks.check(getGuild() != null, "Cannot delete default soundboard sounds");
        checkEditPermissions();

        return guild.deleteSoundboardSound(this);
    }

    @Nonnull
    @Override
    public SoundboardSoundManager getManager() {
        Checks.check(getGuild() != null, "Cannot edit default soundboard sounds");
        checkEditPermissions();

        return new SoundboardSoundManagerImpl(guild, this);
    }

    private void checkEditPermissions() {
        Member selfMember = guild.getSelfMember();
        if (Objects.equals(getUser(), selfMember.getUser())) {
            PermissionUtil.requireAnyPermission(
                    selfMember, Permission.CREATE_GUILD_EXPRESSIONS, Permission.MANAGE_GUILD_EXPRESSIONS);
        } else if (!selfMember.hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(guild, Permission.MANAGE_GUILD_EXPRESSIONS);
        }
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(name).toString();
    }
}
