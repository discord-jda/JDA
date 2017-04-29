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

import org.apache.http.util.Args;
import org.json.JSONObject;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.managers.fields.GroupField;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;

public class GroupManagerUpdatable {
	
	protected final GroupImpl group;
	protected GroupField<String> name;
	protected GroupField<Icon> icon;

	public GroupManagerUpdatable(GroupImpl group)
	{
		this.group = group;
		setupFields();
	}
	
	/**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
	public JDA getJDA() //why...?
	{
		return this.group.getJDA();
	}
	
	/**
     * The {@link net.dv8tion.jda.client.entities.Group Group} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.client.entities.Application Application}
     */
	public Group getGroup()
	{
		return this.group;
	}
	
    /**
     * A {@link net.dv8tion.jda.client.managers.fields.GroupField GroupField}
     * for the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} instance.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.GroupField GroupField} - Type: {@code String}
     */
	public final GroupField<String> getNameField()
	{
		return this.name;
	}
	
	 /**
     * A {@link net.dv8tion.jda.client.managers.fields.GroupField GroupField}
     * for the <b><u>icon</u></b> of the selected {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * <p>To set the value use {@link net.dv8tion.jda.core.managers.fields.Field#setValue(Object) setValue(String)}
     * on the returned {@link net.dv8tion.jda.client.managers.fields.EmoteField EmoteField} instance.
     *
     *  @return {@link net.dv8tion.jda.client.managers.fields.GroupField GroupField} - Type: {@link net.dv8tion.jda.core.entities.Icon Icon}
     */
	public final GroupField<Icon> getIconField()
	{
		return this.icon;
	}
	
	 /**
     * Resets all {@link net.dv8tion.jda.client.managers.fields.GroupField Fields}
     * for this manager instance by calling {@link net.dv8tion.jda.core.managers.fields.Field#reset() Field.reset()} sequentially
     * <br>This is automatically called by {@link #update()}
     */
	public void reset()
    {
        name.reset();
        icon.reset();
    }
	
    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.RestAction RestAction} instance
     * that will apply <b>all</b> changes that have been made to this manager instance.
     * <br>If no changes have been made this will simply return {@link net.dv8tion.jda.core.requests.RestAction.EmptyRestAction EmptyRestAction}.
     *
     * <p>Before applying new changes it is recommended to call {@link #reset()} to reset previous changes.
     * <br>This is automatically called if this method returns successfully.
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Applies all changes that have been made in a single api-call.
     */
	public RestAction<Void> update()
    {
		if (!needsUpdate())
            return new RestAction.EmptyRestAction<>(null);
		
		JSONObject body = new JSONObject();

        if (name.shouldUpdate())
            body.put("name", name.getValue());
        if (icon.shouldUpdate())
        	if (icon.getValue() == null) body.put("icon", "null"); else body.put("icon", icon.getValue().getEncoding());

        reset(); //reset because we built the JSONObject needed to update ya flumpta
        
        Route.CompiledRoute route = Route.Channels.MODIFY_CHANNEL.compile(getGroup().getId());
        return new RestAction<Void>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }
	
	protected boolean needsUpdate()
    {
        return name.shouldUpdate()
                || icon.shouldUpdate();
    }
	
	protected void setupFields(){
		name = new GroupField<String>(this, group::getName)
		{
			@Override public void checkValue(String value)
			{
				Args.notNull(value, "channel name");
				if (value.length() < 1 || value.length() > 32)
                    throw new IllegalArgumentException("Group DM name must be between 1 and 100 characters in length!");
			}
		};
		
		icon = new GroupField<Icon>(this, null)
		{
			@Override
	         public void checkValue(Icon value) { }

	         @Override
	         public Icon getOriginalValue()
	         {
	             throw new UnsupportedOperationException("You'll have to look into this yourself");
	         }

	         @Override
	         public boolean shouldUpdate()
	         {
	             return isSet();
	         }
		};
		
	}
}
