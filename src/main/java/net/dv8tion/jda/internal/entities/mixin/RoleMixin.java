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

package net.dv8tion.jda.internal.entities.mixin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public interface RoleMixin<T extends RoleMixin<T>>
    extends Role
{
    @Nonnull
    @Override
    default RoleAction createCopy(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        return guild.createRole()
                    .setColor(getColorRaw())
                    .setHoisted(isHoisted())
                    .setMentionable(isMentionable())
                    .setName(getName())
                    .setPermissions(getPermissionsRaw())
                    .setIcon(getIcon() == null ? null : getIcon().getEmoji()); // we can only copy the emoji as we don't have access to the Icon instance
    }

    T setName(String name);

    T setColor(int color);

    T setManaged(boolean managed);

    T setHoisted(boolean hoisted);

    T setMentionable(boolean mentionable);

    T setRawPermissions(long rawPermissions);

    T setRawPosition(int rawPosition);

    T setTags(DataObject tags);

    T setIcon(RoleIcon icon);
}
