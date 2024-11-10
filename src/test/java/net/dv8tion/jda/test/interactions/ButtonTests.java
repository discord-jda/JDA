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

package net.dv8tion.jda.test.interactions;

import net.dv8tion.jda.api.entities.SkuSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.*;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ButtonTests
{
    private static final String EXAMPLE_ID = "id";
    private static final String EXAMPLE_URL = "https://example.com";
    private static final UnicodeEmoji EXAMPLE_EMOJI = Emoji.fromUnicode("🤔");
    private static final String EXAMPLE_LABEL = "Label";
    private static final SkuSnowflake EXAMPLE_SKU = SkuSnowflake.fromId(1234);

    @MethodSource("testButtonValid")
    @ParameterizedTest
    void testButtonValid(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        ButtonImpl button = new ButtonImpl(id, label, style, url, sku, false, emoji);
        assertDoesNotThrow(button::checkValid);
        assertDoesNotThrow(button::toData);
    }

    static Stream<Arguments> testButtonValid()
    {
        // The following button configurations are valid:
        return Stream.of(
                // Normal button; id, either label, emoji, label+emoji
                arguments(PRIMARY, "id", EXAMPLE_LABEL, null, null, null),
                arguments(PRIMARY, "id", EXAMPLE_LABEL, null, null, EXAMPLE_EMOJI),
                arguments(PRIMARY, "id", null, null, null, EXAMPLE_EMOJI),
                // Link button; url, either label, emoji, label+emoji
                arguments(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                arguments(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, null, EXAMPLE_EMOJI),
                arguments(LINK, null, null, EXAMPLE_URL, null, EXAMPLE_EMOJI),
                // Premium button doesn't have anything
                arguments(PREMIUM, null, null, null, EXAMPLE_SKU, null)
        );
    }

    @MethodSource("testButtonInvalid")
    @ParameterizedTest
    void testButtonInvalid(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        ButtonImpl button = new ButtonImpl(id, label, style, url, sku, false, emoji);
        assertThatIllegalArgumentException().isThrownBy(button::checkValid);
    }

    static Stream<Arguments> testButtonInvalid()
    {
        // The following button configuration will fail when:
        return Stream.of(
                // Normal button; has no id, has neither label/emoji, has url, has sku
                arguments(PRIMARY, null, EXAMPLE_LABEL, null, null, null),
                arguments(PRIMARY, "id", "", null, null, null),
                arguments(PRIMARY, "id", EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                arguments(PRIMARY, "id", EXAMPLE_LABEL, null, EXAMPLE_SKU, null),
                // Link button; has no url, has id, has sku
                arguments(LINK, null, EXAMPLE_LABEL, null, null, null),
                arguments(LINK, "id", EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                arguments(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, EXAMPLE_SKU, null),
                // Premium button; has no sku, has id, has url, has label, has emoji
                arguments(PREMIUM, null, "", null, null, null),
                arguments(PREMIUM, "id", "", null, EXAMPLE_SKU, null),
                arguments(PREMIUM, null, "", "url", EXAMPLE_SKU, null),
                arguments(PREMIUM, null, EXAMPLE_LABEL, null, EXAMPLE_SKU, null),
                arguments(PREMIUM, null, "", null, EXAMPLE_SKU, EXAMPLE_EMOJI)
        );
    }

    @Test
    void testPrimaryWithSku()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.primary("id", EXAMPLE_LABEL).withSku(EXAMPLE_SKU));
    }

    @Test
    void testPrimaryWithUrl()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.primary("id", EXAMPLE_LABEL).withUrl(EXAMPLE_URL));
    }

    @Test
    void linkWithId()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.link(EXAMPLE_URL, EXAMPLE_LABEL).withId(EXAMPLE_ID));
    }
    
    @Test
    void linkWithSku()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.link(EXAMPLE_URL, EXAMPLE_LABEL).withSku(EXAMPLE_SKU));
    }

    @Test
    void testPremiumWithId()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.premium(EXAMPLE_SKU).withLabel(EXAMPLE_ID));
    }

    @Test
    void testPremiumWithLabel()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.premium(EXAMPLE_SKU).withLabel(EXAMPLE_LABEL));
    }


    @Test
    void testPremiumWithEmoji()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.premium(EXAMPLE_SKU).withEmoji(EXAMPLE_EMOJI));
    }


    @Test
    void testPremiumWithUrl()
    {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Button.premium(EXAMPLE_SKU).withUrl(EXAMPLE_URL));
    }
}
