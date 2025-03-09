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

package net.dv8tion.jda.test.components;

import net.dv8tion.jda.api.components.button.Button;
import net.dv8tion.jda.api.components.button.ButtonStyle;
import net.dv8tion.jda.api.entities.SkuSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.internal.components.button.ButtonImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static net.dv8tion.jda.api.components.button.ButtonStyle.*;
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

    @MethodSource("validButtons")
    @ParameterizedTest
    void testButtonValid(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        ButtonImpl button = new ButtonImpl(id, label, style, url, sku, false, emoji);
        assertDoesNotThrow(button::checkValid);
        assertDoesNotThrow(button::toData);
    }

    static Stream<Arguments> validButtons()
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


    @MethodSource
    @ParameterizedTest
    void testWithId(Button button, boolean shouldThrow)
    {
        if (shouldThrow)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withId("valid-id"));
        else
            assertDoesNotThrow(() -> button.withId("valid-id"));
    }

    static Stream<Arguments> testWithId()
    {
        return Stream.of(
            arguments(Button.primary(EXAMPLE_ID, "Primary"), false),
            arguments(Button.link(EXAMPLE_URL, "Link"), true),
            arguments(Button.premium(EXAMPLE_SKU), true)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testWithUrl(Button button, boolean shouldThrow)
    {
        if (shouldThrow)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withUrl(EXAMPLE_URL));
        else
            assertDoesNotThrow(() -> button.withUrl(EXAMPLE_URL));
    }

    static Stream<Arguments> testWithUrl()
    {
        return Stream.of(
            arguments(Button.primary(EXAMPLE_ID, "Primary"), true),
            arguments(Button.link(EXAMPLE_URL, "Link"), false),
            arguments(Button.premium(EXAMPLE_SKU), true)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testWithSku(Button button, boolean shouldThrow)
    {
        if (shouldThrow)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withSku(EXAMPLE_SKU));
        else
            assertDoesNotThrow(() -> button.withSku(EXAMPLE_SKU));
    }

    static Stream<Arguments> testWithSku()
    {
        return Stream.of(
            arguments(Button.primary(EXAMPLE_ID, "Primary"), true),
            arguments(Button.link(EXAMPLE_URL, "Link"), true),
            arguments(Button.premium(EXAMPLE_SKU), false)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testWithLabel(Button button, boolean shouldThrow)
    {
        if (shouldThrow)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withLabel(EXAMPLE_LABEL));
        else
            assertDoesNotThrow(() -> button.withLabel(EXAMPLE_LABEL));
    }

    static Stream<Arguments> testWithLabel()
    {
        return Stream.of(
            arguments(Button.primary(EXAMPLE_ID, "Primary"), false),
            arguments(Button.link(EXAMPLE_URL, "Link"), false),
            arguments(Button.premium(EXAMPLE_SKU), true)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testWithEmoji(Button button, boolean shouldThrow)
    {
        if (shouldThrow)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withEmoji(EXAMPLE_EMOJI));
        else
            assertDoesNotThrow(() -> button.withEmoji(EXAMPLE_EMOJI));
    }

    static Stream<Arguments> testWithEmoji()
    {
        return Stream.of(
            arguments(Button.primary(EXAMPLE_ID, "Primary"), false),
            arguments(Button.link(EXAMPLE_URL, "Link"), false),
            arguments(Button.premium(EXAMPLE_SKU), true)
        );
    }

    @EnumSource
    @ParameterizedTest
    void testWithStyleLinkToOther(ButtonStyle style)
    {
        Button button = Button.link(EXAMPLE_URL, "Label");
        if (style == LINK)
            assertDoesNotThrow(() -> button.withStyle(style));
        else
            assertThatIllegalArgumentException().isThrownBy(() -> button.withStyle(style));
    }

    @EnumSource
    @ParameterizedTest
    void testWithStylePremiumToOther(ButtonStyle style)
    {
        Button button = Button.premium(EXAMPLE_SKU);
        if (style == PREMIUM)
            assertDoesNotThrow(() -> button.withStyle(style));
        else
            assertThatIllegalArgumentException().isThrownBy(() -> button.withStyle(style));
    }

    @EnumSource
    @ParameterizedTest
    void testWithStyleColorToOther(ButtonStyle style)
    {
        Button button = Button.primary(EXAMPLE_ID, EXAMPLE_LABEL);
        if (style == PREMIUM || style == LINK || style == UNKNOWN)
            assertThatIllegalArgumentException().isThrownBy(() -> button.withStyle(style));
        else
            assertDoesNotThrow(() -> button.withStyle(style));
    }

    @MethodSource("validButtons")
    @ParameterizedTest
    void testWithDisabled(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        Button button = new ButtonImpl(id, label, style, url, sku, false, emoji);
        assertDoesNotThrow(() -> button.withDisabled(true));
        assertDoesNotThrow(() -> button.withDisabled(false));
    }
}
