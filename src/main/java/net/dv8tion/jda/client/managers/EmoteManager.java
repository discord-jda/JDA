/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Set;

/**
 * Used to modify or delete an Emote.<p>
 * <b>This is a <u>client only</u> function!</b>
 */
public class EmoteManager
{

    private EmoteManagerUpdatable updatable;

    public EmoteManager(EmoteImpl emote)
    {
        this.updatable = new EmoteManagerUpdatable(emote);
    }

    /**
     * Sets the name of this Emote.<p>
     * <b>This is a <u>client only</u> function!</b>
     *
     * @param name
     *      The name to set for this Emote (null to keep current name)
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Void}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws AccountTypeException
     *      if the current AccountType is not Client
     * @throws PermissionException
     *      if either the Emote trying to update is fake or we do not have the required Permissions to update this emote
     * @throws IllegalArgumentException
     *      if the specified name has less than 2 chars or more than 32 chars.
     */
    public RestAction<Void> setName(String name)
    {
        return updatable.setName(name).update();
    }

    /**
     * Set roles this emote is active for.<p>
     * <b>This is a <u>client only</u> function!</b>
     *
     * @param roles
     *      A set of roles (all within the same guild the emote is in) / null to keep current roles
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Void}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws AccountTypeException
     *      if the current AccountType is not Client
     * @throws PermissionException
     *      if either the Emote trying to update is fake or we do not have the required Permissions to update this emote
     */
    public RestAction<Void> setRoles(Set<Role> roles)
    {
        return updatable.setRoles(roles).update();
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Emote
     *
     * @return
     *      The JDA instance of this Emote
     */
    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this emote is in
     *
     * @return
     *      The Guild of the respected Emote
     */
    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this Manager.
     *
     * @return
     *      The Emote
     */
    public Emote getEmote()
    {
        return updatable.getEmote();
    }

}
