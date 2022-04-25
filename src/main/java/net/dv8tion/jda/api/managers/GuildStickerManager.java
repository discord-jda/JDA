package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public interface GuildStickerManager extends Manager<GuildStickerManager>
{
    long NAME = 1;
    long DESCRIPTION = 1 << 1;
    long TAGS = 1 << 2;

    @Nonnull
    Guild getGuild();

    @Nonnull
    @CheckReturnValue
    GuildStickerManager setName(@Nonnull String name); // 2-30

    @Nonnull
    @CheckReturnValue
    GuildStickerManager setDescription(@Nonnull String description); // 2-100

    @Nonnull
    @CheckReturnValue
    GuildStickerManager setTags(@Nonnull Collection<String> tags); // 200 chars (csv list -> len(word)+1)

    @Nonnull
    @CheckReturnValue
    default GuildStickerManager setTags(@Nonnull String... tags)
    {
        Checks.noneNull(tags, "Tags");
        return setTags(Arrays.asList(tags));
    }
}
