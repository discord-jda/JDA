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

package net.dv8tion.jda.api.interactions.components.text_display;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;

public interface TextDisplay extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    String getContentRaw();
    // TODO : do we actually want to do this? If we start sending text in modals, we won't have
    // TODO : the data to resolve stuff, will we? Maybe it should accept a JDA to do it? Maybe a
    // TODO : 'context' of some kind? idk
    String getContentDisplay(Message message);

    String getContentStripped();
}
