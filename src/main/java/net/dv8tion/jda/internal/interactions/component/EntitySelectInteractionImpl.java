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

package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.SelectMenuMentions;

import javax.annotation.Nonnull;
import java.util.List;

public class EntitySelectInteractionImpl extends SelectMenuInteractionImpl<IMentionable, EntitySelectMenu> implements EntitySelectInteraction
{
    private final Mentions mentions;

    public EntitySelectInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, EntitySelectMenu.class, data);
        DataObject content = data.getObject("data");
        this.mentions = new SelectMenuMentions(
                jda,
                getGuild(),
                content.optObject("resolved").orElseGet(DataObject::empty),
                content.optArray("values").orElseGet(DataArray::empty)
        );
    }

    @Nonnull
    @Override
    public Mentions getMentions()
    {
        return mentions;
    }

    @Nonnull
    @Override
    public List<IMentionable> getValues()
    {
        return mentions.getMentions();
    }
}
