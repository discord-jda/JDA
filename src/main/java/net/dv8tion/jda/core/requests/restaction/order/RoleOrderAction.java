/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction OrderAction}
 * designed to modify the order of {@link net.dv8tion.jda.core.entities.Role Roles} of the
 * specified {@link net.dv8tion.jda.core.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 *
 * <p>Before you can use any of the {@code move} methods
 * you must use either {@link #selectPosition(Object) selectPosition(Role)} or {@link #selectPosition(int)}!
 *
 * <p><b>This uses descending order!</b>
 *
 * @since 3.0
 */
public class RoleOrderAction extends OrderAction<Role, RoleOrderAction>
{
    protected final Guild guild;

    /**
     * Creates a new RoleOrderAction instance
     *
     * @param  guild
     *         The target {@link net.dv8tion.jda.core.entities.Guild Guild} of which
     *         to change the {@link net.dv8tion.jda.core.entities.Role Role} order
     * @param  useDiscordOrder
     *         Defines the ordering of the OrderAction. If {@code true}, the OrderAction will be in the ordering
     *         defined by Discord for roles, which is Descending. This means that the highest role appears at index {@code 0}
     *         and the lowest role at index {@code n - 1}. Providing {@code false} will result in the ordering being
     *         in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     *         <br>As a note: {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()}
     *         and {@link net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     */
    public RoleOrderAction(Guild guild, boolean useDiscordOrder)
    {
        super(guild.getJDA(), !useDiscordOrder, Route.Guilds.MODIFY_ROLES.compile(guild.getId()));
        this.guild = guild;

        List<Role> roles = guild.getRoles();
        roles = roles.subList(0, roles.size() - 1); //Don't include the @everyone role.

        if (useDiscordOrder)
        {
            //Add roles to orderList in reverse due to role position ordering being descending
            // Top role starts at roles.size() - 1, bottom is 0.
            for (int i = roles.size() - 1; i >= 0; i--)
                this.orderList.add(roles.get(i));
        }
        else
        {
            //If not using discord ordering, we are ascending, so we add from first to last.
            // We add first to last because the roles provided from getRoles() are in ascending order already
            // with the highest role at index 0.
            this.orderList.addAll(roles);
        }

    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} which holds
     * the roles from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected RequestBody finalizeData()
    {
        final Member self = guild.getSelfMember();
        final boolean isOwner = self.isOwner();

        if (!isOwner)
        {
            if (self.getRoles().isEmpty())
                throw new IllegalStateException("Cannot move roles above your highest role unless you are the guild owner");
            if (!self.hasPermission(Permission.MANAGE_ROLES))
                throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        }

        JSONArray array = new JSONArray();
        List<Role> ordering = new ArrayList<>(orderList);

        //If not in normal discord order, reverse.
        // Normal order is descending, not ascending.
        if (ascendingOrder)
            Collections.reverse(ordering);

        for (int i = 0; i < ordering.size(); i++)
        {
            Role role = ordering.get(i);
            final int initialPos = role.getPosition();
            if (initialPos != i && !isOwner && !self.canInteract(role))
                // If the current role was moved, we are not owner and we can't interact with the role then throw a PermissionException
                throw new IllegalStateException("Cannot change order: One of the roles could not be moved due to hierarchical power!");

            array.put(new JSONObject()
                    .put("id", role.getId())
                    .put("position", i + 1)); //plus 1 because position 0 is the @everyone position.
        }

        return getRequestBody(array);
    }

    @Override
    protected void validateInput(Role entity)
    {
        Checks.check(entity.getGuild().equals(guild), "Provided selected role is not from this Guild!");
        Checks.check(orderList.contains(entity), "Provided role is not in the list of orderable roles!");
    }
}

