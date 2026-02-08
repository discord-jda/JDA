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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.api.utils.DiscordAssets;
import net.dv8tion.jda.api.utils.ImageFormat;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static net.dv8tion.jda.api.utils.DiscordAssets.*;
import static net.dv8tion.jda.test.util.MockitoVerifyUtils.getPublicMethods;
import static org.assertj.core.api.Assertions.assertThat;

public class DiscordAssetsTest extends AbstractSnapshotTest {

    private static final ImageFormat EXAMPLE_FORMAT = ImageFormat.ANIMATED_WEBP;
    private static final String EXAMPLE_SNOWFLAKE = "222046562543468545";
    private static final String EXAMPLE_HASH = "86185a18d168f88b91c";

    @Test
    void testDiscordAssetsOutputIsChecked() {
        DataObject data = DataObject.empty();

        data.put(
                "applicationIcon",
                applicationIcon(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "applicationCover",
                applicationCover(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH)
                        .getUrl());
        data.put(
                "applicationTeamIcon",
                applicationTeamIcon(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH)
                        .getUrl());
        data.put(
                "channelIcon",
                channelIcon(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put("customEmoji", customEmoji(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE).getUrl());
        data.put(
                "guildBanner",
                guildBanner(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "guildIcon",
                guildIcon(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "guildSplash",
                guildSplash(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "memberAvatar",
                memberAvatar(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH)
                        .getUrl());
        data.put(
                "roleIcon",
                roleIcon(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "scheduledEventCoverImage",
                scheduledEventCoverImage(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH)
                        .getUrl());
        data.put(
                "stickerPackBanner",
                stickerPackBanner(EXAMPLE_FORMAT, EXAMPLE_HASH).getUrl());
        data.put(
                "userAvatar",
                userAvatar(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "userBanner",
                userBanner(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());
        data.put(
                "userDefaultAvatar",
                userDefaultAvatar(EXAMPLE_FORMAT, EXAMPLE_HASH).getUrl());
        data.put(
                "userTagBadge",
                userTagBadge(EXAMPLE_FORMAT, EXAMPLE_SNOWFLAKE, EXAMPLE_HASH).getUrl());

        assertThat(data.keys())
                .as("All DiscordAssets methods should be tested")
                .containsAll(getPublicMethods(DiscordAssets.class));

        assertWithSnapshot(data);
    }
}
