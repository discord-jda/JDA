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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ApplicationTeam;
import net.dv8tion.jda.api.entities.TeamMember;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ApplicationTeamImpl implements ApplicationTeam
{
    private final JDA jda;
    private final String iconId;
    private final List<TeamMember> members;
    private final long id, ownerId;

    public ApplicationTeamImpl(JDA jda, String iconId, List<TeamMember> members, long id, long ownerId)
    {
        this.jda = jda;
        this.iconId = iconId;
        this.members = Collections.unmodifiableList(members);
        this.id = id;
        this.ownerId = ownerId;
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Override
    public String getIconId()
    {
        return iconId;
    }

	@Nullable
	@Override
	public ImageProxy getIcon() {
        if (iconId == null) return null;

        final String iconUrl = String.format(ICON_URL, getId(), iconId);

        return new ImageProxy(iconUrl, iconId, "png");
    }

	@Nonnull
    @Override
    public List<TeamMember> getMembers()
    {
        return members;
    }

    @NotNull
    @Override
    public JDA getJDA()
    {
        return jda;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ApplicationTeamImpl))
            return false;
        ApplicationTeamImpl app = (ApplicationTeamImpl) obj;
        return app.id == this.id;
    }

    @Override
    public String toString()
    {
        return "ApplicationTeam(" + getId() + ')';
    }
}
