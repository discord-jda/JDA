package net.dv8tion.jda.test.interactions;

import net.dv8tion.jda.api.entities.SkuSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ButtonTests
{
    @MethodSource("testButtonValid")
    @ParameterizedTest
    void testButtonValid(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        Assertions.assertDoesNotThrow(() -> new ButtonImpl(id, label, style, url, sku, false, emoji).checkValid());
    }

    static Stream<Arguments> testButtonValid()
    {
        // The following button configurations are valid:
        return Stream.of(
                // Normal button; id, either label, emoji, label+emoji
                Arguments.of(ButtonStyle.PRIMARY, "id", "label", null, null, null),
                Arguments.of(ButtonStyle.PRIMARY, "id", "label", null, null, Emoji.fromUnicode("\uD83C\uDF82")),
                Arguments.of(ButtonStyle.PRIMARY, "id", "", null, null, Emoji.fromUnicode("\uD83C\uDF82")),
                // Link button; url, either label, emoji, label+emoji
                Arguments.of(ButtonStyle.LINK, null, "label", "http://localhost:8080", null, null),
                Arguments.of(ButtonStyle.LINK, null, "label", "http://localhost:8080", null, Emoji.fromUnicode("\uD83C\uDF82")),
                Arguments.of(ButtonStyle.LINK, null, "", "http://localhost:8080", null, Emoji.fromUnicode("\uD83C\uDF82")),
                // Premium button doesn't have anything
                Arguments.of(ButtonStyle.PREMIUM, null, "", null, SkuSnowflake.fromId(1234), null)
        );
    }

    @MethodSource("testButtonInvalid")
    @ParameterizedTest
    void testButtonInvalid(ButtonStyle style, String id, String label, String url, SkuSnowflake sku, Emoji emoji)
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ButtonImpl(id, label, style, url, sku, false, emoji).checkValid());
    }

    static Stream<Arguments> testButtonInvalid()
    {
        // The following button configuration will fail when:
        return Stream.of(
                // Normal button; has no id, has neither label/emoji, has url, has sku
                Arguments.of(ButtonStyle.PRIMARY, null, "label", null, null, null),
                Arguments.of(ButtonStyle.PRIMARY, "id", "", null, null, null),
                Arguments.of(ButtonStyle.PRIMARY, "id", "label", "url", null, null),
                Arguments.of(ButtonStyle.PRIMARY, "id", "label", null, SkuSnowflake.fromId(1234), null),
                // Link button; has no url, has id, has sku
                Arguments.of(ButtonStyle.LINK, null, "label", null, null, null),
                Arguments.of(ButtonStyle.LINK, "id", "label", "http://localhost:8080", null, null),
                Arguments.of(ButtonStyle.LINK, null, "label", "http://localhost:8080", SkuSnowflake.fromId(1234), null),
                // Premium button; has no sku, has id, has url, has label, has emoji
                Arguments.of(ButtonStyle.PREMIUM, null, "", null, null, null),
                Arguments.of(ButtonStyle.PREMIUM, "id", "", null, SkuSnowflake.fromId(1234), null),
                Arguments.of(ButtonStyle.PREMIUM, null, "", "url", SkuSnowflake.fromId(1234), null),
                Arguments.of(ButtonStyle.PREMIUM, null, "label", null, SkuSnowflake.fromId(1234), null),
                Arguments.of(ButtonStyle.PREMIUM, null, "", null, SkuSnowflake.fromId(1234), Emoji.fromUnicode("\uD83C\uDF82"))
        );
    }
}
