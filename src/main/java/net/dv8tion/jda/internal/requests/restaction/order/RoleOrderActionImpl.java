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

package net.dv8tion.jda.internal.requests.restaction.order;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleOrderActionImpl
    extends OrderActionImpl<Role, RoleOrderAction>
    implements RoleOrderAction
{
    protected final Guild guild;

    /**
     * Creates a new RoleOrderAction instance
     *
     * @param  guild
     *         The target {@link net.dv8tion.jda.api.entities.Guild Guild} of which
     *         to change the {@link net.dv8tion.jda.api.entities.Role Role} order
     * @param  useAscendingOrder
     *         Defines the ordering of the OrderAction. If {@code false}, the OrderAction will be in the ordering
     *         defined by Discord for roles, which is Descending. This means that the highest role appears at index {@code 0}
     *         and the lowest role at index {@code n - 1}. Providing {@code true} will result in the ordering being
     *         in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     *         <br>As a note: {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}
     *         and {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     */
    public RoleOrderActionImpl(Guild guild, boolean useAscendingOrder)
    {
        super(guild.getJDA(), !useAscendingOrder, Route.Guilds.MODIFY_ROLES.compile(guild.getId()));
        this.guild = guild;

        List<Role> roles = guild.getRoles();
        roles = roles.subList(0, roles.size() - 1); //Don't include the @everyone role.

        if (useAscendingOrder)
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

    @Nonnull
    @Override
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
                throw new InsufficientPermissionException(guild, Permission.MANAGE_ROLES);
        }

        DataArray array = DataArray.empty();
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

            array.add(DataObject.empty()
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

