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
    }

    static Stream<Arguments> testButtonValid()
    {
        // The following button configurations are valid:
        return Stream.of(
                // Normal button; id, either label, emoji, label+emoji
                Arguments.of(PRIMARY, "id", EXAMPLE_LABEL, null, null, null),
                Arguments.of(PRIMARY, "id", EXAMPLE_LABEL, null, null, EXAMPLE_EMOJI),
                Arguments.of(PRIMARY, "id", "", null, null, EXAMPLE_EMOJI),
                // Link button; url, either label, emoji, label+emoji
                Arguments.of(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                Arguments.of(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, null, EXAMPLE_EMOJI),
                Arguments.of(LINK, null, "", EXAMPLE_URL, null, EXAMPLE_EMOJI),
                // Premium button doesn't have anything
                Arguments.of(PREMIUM, null, "", null, EXAMPLE_SKU, null)
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
                Arguments.of(PRIMARY, null, EXAMPLE_LABEL, null, null, null),
                Arguments.of(PRIMARY, "id", "", null, null, null),
                Arguments.of(PRIMARY, "id", EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                Arguments.of(PRIMARY, "id", EXAMPLE_LABEL, null, EXAMPLE_SKU, null),
                // Link button; has no url, has id, has sku
                Arguments.of(LINK, null, EXAMPLE_LABEL, null, null, null),
                Arguments.of(LINK, "id", EXAMPLE_LABEL, EXAMPLE_URL, null, null),
                Arguments.of(LINK, null, EXAMPLE_LABEL, EXAMPLE_URL, EXAMPLE_SKU, null),
                // Premium button; has no sku, has id, has url, has label, has emoji
                Arguments.of(PREMIUM, null, "", null, null, null),
                Arguments.of(PREMIUM, "id", "", null, EXAMPLE_SKU, null),
                Arguments.of(PREMIUM, null, "", "url", EXAMPLE_SKU, null),
                Arguments.of(PREMIUM, null, EXAMPLE_LABEL, null, EXAMPLE_SKU, null),
                Arguments.of(PREMIUM, null, "", null, EXAMPLE_SKU, EXAMPLE_EMOJI)
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
