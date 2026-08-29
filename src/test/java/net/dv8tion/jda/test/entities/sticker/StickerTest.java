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

package net.dv8tion.jda.test.entities.sticker;

import net.dv8tion.jda.api.entities.sticker.Sticker.StickerFormat;
import net.dv8tion.jda.internal.entities.sticker.StickerItemImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StickerTest {
    private static final long STICKER_ID = 749054660769218631L;

    @Test
    void getIconUrlUsesCdnForNonGifFormats() {
        assertThat(createSticker(StickerFormat.PNG).getIconUrl())
                .isEqualTo("https://cdn.discordapp.com/stickers/749054660769218631.png");
        assertThat(createSticker(StickerFormat.APNG).getIconUrl())
                .isEqualTo("https://cdn.discordapp.com/stickers/749054660769218631.png");
        assertThat(createSticker(StickerFormat.LOTTIE).getIconUrl())
                .isEqualTo("https://cdn.discordapp.com/stickers/749054660769218631.json");
    }

    @Test
    void getIconUrlUsesMediaCdnForGifFormat() {
        assertThat(createSticker(StickerFormat.GIF).getIconUrl())
                .isEqualTo("https://media.discordapp.net/stickers/749054660769218631.gif");
    }

    private StickerItemImpl createSticker(StickerFormat format) {
        return new StickerItemImpl(STICKER_ID, format, "test-sticker");
    }
}
