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

package net.dv8tion.jda.entities;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActivityTest
{
    private static DataObject formatActivity(int type, String name, String state)
    {
        DataObject json = DataObject.empty()
                .put("type", type);

        if (state != null)
            json.put("state", state);
        if (name != null)
            json.put("name", name);

        return json;
    }

    @Test
    public void activitySerializationTest()
    {
        Assertions.assertEquals(
            formatActivity(0, "playing test", null),
            PresenceImpl.getGameJson(Activity.playing("playing test"))
        );
        Assertions.assertEquals(
            formatActivity(0, "playing test", "playing state"),
            PresenceImpl.getGameJson(Activity.playing("playing test").withState("playing state"))
        );

        Assertions.assertEquals(
            formatActivity(1, "streaming test", null).put("url", "https://twitch.tv/discord"),
            PresenceImpl.getGameJson(Activity.streaming("streaming test", "https://twitch.tv/discord"))
        );
        Assertions.assertEquals(
            formatActivity(1, "streaming test", "streaming state").put("url", "https://twitch.tv/discord"),
            PresenceImpl.getGameJson(Activity.streaming("streaming test", "https://twitch.tv/discord").withState("streaming state"))
        );

        Assertions.assertEquals(
            formatActivity(2, "listening test", null),
            PresenceImpl.getGameJson(Activity.listening("listening test"))
        );
        Assertions.assertEquals(
            formatActivity(2, "listening test", "listening state"),
            PresenceImpl.getGameJson(Activity.listening("listening test").withState("listening state"))
        );

        Assertions.assertEquals(
            formatActivity(3, "watching test", null),
            PresenceImpl.getGameJson(Activity.watching("watching test"))
        );
        Assertions.assertEquals(
            formatActivity(3, "watching test", "watching state"),
            PresenceImpl.getGameJson(Activity.watching("watching test").withState("watching state"))
        );

        Assertions.assertEquals(
            formatActivity(4, "Custom Status", "custom status test"),
            PresenceImpl.getGameJson(Activity.customStatus("custom status test"))
        );
        Assertions.assertEquals(
            formatActivity(4, "Custom Status", "custom status test"),
            PresenceImpl.getGameJson(Activity.customStatus("custom status test").withState("should be ignored"))
        );

        Assertions.assertEquals(
            formatActivity(5, "competing test", null),
            PresenceImpl.getGameJson(Activity.competing("competing test"))
        );
        Assertions.assertEquals(
            formatActivity(5, "competing test", "competing state"),
            PresenceImpl.getGameJson(Activity.competing("competing test").withState("competing state"))
        );
    }
}
