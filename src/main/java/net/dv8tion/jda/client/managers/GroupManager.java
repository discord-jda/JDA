/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.client.managers;

import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.requests.RestAction;

public class GroupManager {
	
	protected final GroupManagerUpdatable updatable;
	
	public GroupManager(GroupImpl group){
		updatable = new GroupManagerUpdatable(group);
	}
	
	/**
     * Sets the Group DM's name
     * 
     * @param  name
     *         The new name, must be between 1 and 100 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link GroupManagerUpdatable#update() #update()}
     */
	public RestAction<Void> setName(String name){
		return updatable.getNameField().setValue(name).update();
	}
	
	/**
	 * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.client.entities.Group Group}.
     * 
     * @param  new icon, or null to remove the icon
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link GroupManagerUpdatable#update() #update()}
     */
	public RestAction<Void> setIcon(Icon icon){
		return updatable.getIconField().setValue(icon).update();
	}
}
