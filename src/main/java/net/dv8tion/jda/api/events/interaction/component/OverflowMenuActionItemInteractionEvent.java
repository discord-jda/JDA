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

package net.dv8tion.jda.api.events.interaction.component;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.overflowmenu.OverflowMenuActionItem;
import net.dv8tion.jda.api.interactions.components.overflowmenu.OverflowMenuActionItemInteraction;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link OverflowMenuActionItem} on one of the bots messages (or a webhook owned by the bot) was clicked by a user.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class OverflowMenuActionItemInteractionEvent extends GenericComponentInteractionCreateEvent
        implements OverflowMenuActionItemInteraction {
    private final OverflowMenuActionItemInteraction interaction;

    public OverflowMenuActionItemInteractionEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull OverflowMenuActionItemInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public OverflowMenuActionItemInteraction getInteraction() {
        return interaction;
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem getComponent() {
        return interaction.getComponent();
    }

    @Nonnull
    @Override
    public OverflowMenuActionItem getItem() {
        return interaction.getItem();
    }
}
