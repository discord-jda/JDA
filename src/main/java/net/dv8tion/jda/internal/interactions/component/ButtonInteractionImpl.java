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

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;

public class ButtonInteractionImpl extends ComponentInteractionImpl implements ButtonInteraction
{
    private final Button button;

    public ButtonInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        button = message != null ? this.message.getButtonById(customId) : null;
    }

    @Nonnull
    @Override
    public Component.Type getComponentType()
    {
        return Component.Type.BUTTON;
    }

    @Nonnull
    @Override
    public Button getButton()
    {
        return button;
    }
}
