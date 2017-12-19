/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests.restaction.order;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

/**
 * Implementation of {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction OrderAction}
 * similar in functionality to {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction},
 * but constrained to the bounds of a single {@link net.dv8tion.jda.core.entities.Category Category}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.core.requests.RestAction RestAction}.
 *
 * <p>Before you can use any of the {@code move} methods
 * you must use either {@link #selectPosition(Object) selectPosition(Channel)} or {@link #selectPosition(int)}!
 *
 * @param  <T>
 *         The type of {@link net.dv8tion.jda.core.entities.Channel Channel} to move
 *         using this CategoryOrderAction, either {@link net.dv8tion.jda.core.entities.TextChannel TextChannel},
 *         or {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
 *
 * @author Kaidan Gustave
 */
public class CategoryOrderAction<T extends Channel> extends OrderAction<T, CategoryOrderAction<T>>
{
    protected final Category category;
    protected final ChannelType type;

    /**
     * Creates a new CategoryOrderAction for the specified {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @param  category
     *         The target {@link net.dv8tion.jda.core.entities.Category Category}
     *         which the new CategoryOrderAction will order channels from.
     * @param  type
     *         The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} that
     *         matches the returning value of {@link net.dv8tion.jda.core.entities.Channel#getType() Channel#getType()}
     *         for the generic {@link net.dv8tion.jda.core.entities.Channel Channel} type {@code T}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the {@code ChannelType} is not one that can be retrieved from a {@code Category}.
     *         Currently the only two allowed are {@link ChannelType#TEXT} and {@link ChannelType#VOICE}.
     */
    public CategoryOrderAction(Category category, ChannelType type)
    {
        super(category.getJDA(), Route.Guilds.MODIFY_CHANNELS.compile(category.getGuild().getId()));
        this.category = category;
        this.type = type;

        // Add all channels of a type in this Category
        switch(type)
        {
            case TEXT:
                this.orderList.addAll((List) category.getTextChannels());
                break;
            case VOICE:
                this.orderList.addAll((List) category.getVoiceChannels());
                break;
            // Categories cannot currently be nested within categories so we just default here.
            // If discord does allow more types of channels to be nested withing categories in
            // the future, adding this functionality is just a matter of adding an additional case.
            default:
                throw new IllegalArgumentException("Cannot order channel type: "+type.name());
        }
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Guild Guild} that
     * the {@link net.dv8tion.jda.core.entities.Category Category}
     * is from.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild} that
     *         the {@link net.dv8tion.jda.core.entities.Category Category}
     *         is from.
     */
    public Guild getGuild()
    {
        return getCategory().getGuild();
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Category Category}
     * controlled by this CategoryOrderAction.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Category Category}
     *         of this CategoryOrderAction.
     */
    public Category getCategory()
    {
        return category;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of
     * the channels that are ordered by this CategoryOrderAction.
     *
     * @return The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType}
     *         of the channels ordered by this CategoryOrderAction.
     */
    public ChannelType getChannelType()
    {
        return type;
    }

    @Override
    protected RequestBody finalizeData()
    {
        final Member self = category.getGuild().getSelfMember();

        // Make sure we have permission to actually do this
        if(!self.hasPermission(category, Permission.MANAGE_CHANNEL))
            throw new InsufficientPermissionException(Permission.MANAGE_CHANNEL);

        JSONArray array = new JSONArray();
        for(int i = 0; i < orderList.size(); i++)
        {
            JSONObject object = new JSONObject().put("id", orderList.get(i).getId())
                                                .put("position", i);
            array.put(object);
        }

        return getRequestBody(array);
    }

    @Override
    protected void validateInput(T entity)
    {
        Checks.check(entity.getParent() != null, "Provided channel must be nested in a Category!");
        Checks.check(entity.getParent().equals(getCategory()), "Provided channel's Category is not this Category!");
        Checks.check(orderList.contains(entity), "Provided channel is not in the list of orderable channels!");
    }
}
