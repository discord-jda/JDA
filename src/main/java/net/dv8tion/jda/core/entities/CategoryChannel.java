package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;

import java.util.List;

public interface CategoryChannel extends Channel, Comparable<CategoryChannel>
{
    /**
     * Gets all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} in this {@link net.dv8tion.jda.core.entities.CategoryChannel Category}.
     * <br>The channels returned will be sorted according to their position.
     *
     * @return An immutable List of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} in this Category.
     */
    List<TextChannel> getTextChannels();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this CategoryChannel
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();
}
