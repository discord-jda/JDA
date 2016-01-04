/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.builders;

import net.dv8tion.jda.Region;
import net.dv8tion.jda.utils.AvatarUtil;

/**
 * Used for storing all values that will be used to create a new {@link net.dv8tion.jda.entities.Guild Guild}
 * using {@link net.dv8tion.jda.JDA#createGuild(GuildBuilder) JDA.createGuild(GuildBuilder)} or
 * {@link net.dv8tion.jda.JDA#createGuildAsync(GuildBuilder, java.util.function.Consumer) JDA.createGuild(GuildBuilder, Consumer)}
 */
public class GuildBuilder
{
    private String name;
    private Region region;
    private AvatarUtil.Avatar icon;

    /**
     * Creates a GuildBuilder with the name set to the provided name.<br>
     * The {@link net.dv8tion.jda.Region Region} defaults to {@link net.dv8tion.jda.Region#US_WEST Region.US_WEST}.<br>
     * The icon defaults to null (blank).
     *
     * @param name
     *          The name you wish to name the new {@link net.dv8tion.jda.entities.Guild Guild}.
     */
    public GuildBuilder(String name)
    {
        setName(name);
        setRegion(Region.US_WEST);
        setIcon(null);
    }

    /**
     * The currently set name that will be used to create the new {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      The currently set name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The currently set {@link net.dv8tion.jda.Region Region} that will be used to create the new
     * {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      The currently set {@link net.dv8tion.jda.Region Region}.
     */
    public Region getRegion()
    {
        return region;
    }

    /**
     * The currently set {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} icon image.<br>
     * If no icon has been set, this will return null.
     *
     * @return
     *      The currently set {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} icon image.
     */
    public AvatarUtil.Avatar getIcon()
    {
        return icon;
    }

    /**
     * Sets the name that will be used to name the new {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     * This should be non-null and non-empty.
     *
     * @param name
     *          The name which will be used to name the new {@link net.dv8tion.jda.entities.Guild Guild}.
     * @return
     *      This {@link net.dv8tion.jda.builders.GuildBuilder GuildBuilder} instance. Useful for chaining.
     */
    public GuildBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.Region Region} of the {@link net.dv8tion.jda.entities.Guild Guild}
     * that will be used for audio communications.
     *
     * @param region
     *          The {@link net.dv8tion.jda.Region Region} of the {@link net.dv8tion.jda.entities.Guild Guild}.
     * @return
     *      This {@link net.dv8tion.jda.builders.GuildBuilder GuildBuilder} instance. Useful for chaining.
     */
    public GuildBuilder setRegion(Region region)
    {
        this.region = region;
        return this;
    }

    /**
     * Sets the image icon of the {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     * This uses the same {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} system that
     * {@link net.dv8tion.jda.entities.User Users} use.<br>
     * Setting this to <code>null</code> will set no icon, thus leaving it blank.
     *
     * @param icon
     *          The {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} image to use when creating the new {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     *          <code>null</code> for a blank Guild icon.
     * @return
     *      This {@link net.dv8tion.jda.builders.GuildBuilder GuildBuilder} instance. Useful for chaining.
     */
    public GuildBuilder setIcon(AvatarUtil.Avatar icon)
    {
        this.icon = icon;
        return this;
    }
}
