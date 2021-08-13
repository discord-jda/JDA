package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import java.util.List;

//TODO-v5: Need Docs
//TODO-v5: Should this actually be extending GuildChannel?
public interface IMemberContainer extends GuildChannel
{
    /**
     * A List of all {@link net.dv8tion.jda.api.entities.Member Members} that are in this GuildChannel
     * <br>For {@link net.dv8tion.jda.api.entities.TextChannel TextChannels},
     * this returns all Members with the {@link net.dv8tion.jda.api.Permission#MESSAGE_READ} Permission.
     * <br>For {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels},
     * this returns all Members that joined that VoiceChannel.
     * <br>For {@link net.dv8tion.jda.api.entities.Category Categories},
     * this returns all Members who are in its child channels.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Member Members} that are in this GuildChannel.
     */
    @Nonnull
    List<Member> getMembers();
}
