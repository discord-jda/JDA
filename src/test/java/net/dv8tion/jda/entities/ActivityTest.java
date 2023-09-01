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
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.managers.PresenceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(
            formatActivity(0, "playing test", null),
            PresenceImpl.getGameJson(Activity.playing("playing test"))
        );
        assertEquals(
            formatActivity(0, "playing test", "playing state"),
            PresenceImpl.getGameJson(Activity.playing("playing test").withState("playing state"))
        );

        assertEquals(
            formatActivity(1, "streaming test", null).put("url", "https://twitch.tv/discord"),
            PresenceImpl.getGameJson(Activity.streaming("streaming test", "https://twitch.tv/discord"))
        );
        assertEquals(
            formatActivity(1, "streaming test", "streaming state").put("url", "https://twitch.tv/discord"),
            PresenceImpl.getGameJson(Activity.streaming("streaming test", "https://twitch.tv/discord").withState("streaming state"))
        );

        assertEquals(
            formatActivity(2, "listening test", null),
            PresenceImpl.getGameJson(Activity.listening("listening test"))
        );
        assertEquals(
            formatActivity(2, "listening test", "listening state"),
            PresenceImpl.getGameJson(Activity.listening("listening test").withState("listening state"))
        );

        assertEquals(
            formatActivity(3, "watching test", null),
            PresenceImpl.getGameJson(Activity.watching("watching test"))
        );
        assertEquals(
            formatActivity(3, "watching test", "watching state"),
            PresenceImpl.getGameJson(Activity.watching("watching test").withState("watching state"))
        );

        assertEquals(
            formatActivity(4, "Custom Status", "custom status test"),
            PresenceImpl.getGameJson(Activity.customStatus("custom status test"))
        );
        assertEquals(
            formatActivity(4, "Custom Status", "custom status test"),
            PresenceImpl.getGameJson(Activity.customStatus("custom status test").withState("should be ignored"))
        );

        assertEquals(
            formatActivity(5, "competing test", null),
            PresenceImpl.getGameJson(Activity.competing("competing test"))
        );
        assertEquals(
            formatActivity(5, "competing test", "competing state"),
            PresenceImpl.getGameJson(Activity.competing("competing test").withState("competing state"))
        );
    }

    @Test
    public void activityBasicDeserializationTest()
    {
        Activity activity = EntityBuilder.createActivity(
            DataObject.empty()
                .put("type", 0) // playing
                .put("name", "Games")
        );

        assertFalse(activity.isRich());
        assertEquals(Activity.ActivityType.PLAYING, activity.getType());
        assertEquals("Games", activity.getName());;
        assertNull(activity.getState());
        assertNull(activity.getUrl());
    }

    @Test
    public void activityRichDeserializationTest()
    {
        Activity activity = EntityBuilder.createActivity(
            DataObject.empty()
                .put("type", 0) // playing
                .put("name", "Games")
                .put("state", "Active")
        );

        assertFalse(activity.isRich(), "isRich()");
        assertEquals(Activity.ActivityType.PLAYING, activity.getType());
        assertEquals("Games", activity.getName());;
        assertEquals("Active", activity.getState());
        assertNull(activity.getUrl(), "url");

        activity = EntityBuilder.createActivity(
            DataObject.empty()
                .put("type", 0) // playing
                .put("name", "The Best Game Ever")
                .put("state", "In a Group")
                .put("details", "Playing 3v3 Control Point")
                .put("party", DataObject.empty()
                    .put("id", "1234")
                    .put("size", DataArray.fromCollection(Arrays.asList(3, 6))))
                .put("timestamps", DataObject.empty()
                    .put("start", 1507665886)
                    .put("end", 1507666000))
                .put("assets", DataObject.empty()
                    .put("large_image", "canary-large")
                    .put("small_text", "Small icon")
                    .put("small_image", "ptb-large"))
                .put("instance", true)
                .put("session_id", "4b2fdce12f639de8bfa7e3591b71a0d679d7c93f")
                .put("sync_id", "e7eb30d2ee025ed05c71ea495f770b76454ee4e0")
        );

        RichPresence rich = activity.asRichPresence();
        assertEquals(activity, rich);

        assertEquals(Activity.ActivityType.PLAYING, rich.getType());
        assertEquals("The Best Game Ever", rich.getName());
        assertEquals("In a Group", rich.getState());

        assertNotNull(rich.getParty(), "party");
        assertEquals("1234", rich.getParty().getId());
        assertEquals(3, rich.getParty().getSize());
        assertEquals(6, rich.getParty().getMax());

        assertNotNull(rich.getTimestamps());
        assertEquals(1507665886, rich.getTimestamps().getStart());
        assertEquals(1507666000, rich.getTimestamps().getEnd());

        assertNotNull(rich.getLargeImage(), "assets.large_image");
        assertEquals("canary-large", rich.getLargeImage().getKey());
        assertNull(rich.getLargeImage().getText(), "assets.large_text");

        assertNotNull(rich.getSmallImage(), "assets.small_image");
        assertEquals("ptb-large", rich.getSmallImage().getKey());
        assertEquals("Small icon", rich.getSmallImage().getText());

        assertEquals("4b2fdce12f639de8bfa7e3591b71a0d679d7c93f", rich.getSessionId());
        assertEquals("e7eb30d2ee025ed05c71ea495f770b76454ee4e0", rich.getSyncId());
    }
}
