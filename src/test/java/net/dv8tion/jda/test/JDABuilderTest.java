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

package net.dv8tion.jda.test;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

import static net.dv8tion.jda.api.requests.GatewayIntent.ALL_INTENTS;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class JDABuilderTest extends AbstractSnapshotTest
{
    private static final String TOKEN = "invalid.token.here";

    @Test
    void testCreateWithAllIntents()
    {
        TestJDABuilder builder = new TestJDABuilder(ALL_INTENTS);
        builder.applyIntents();

        assertThatLoggingFrom(builder::checkIntents).isEmpty();
    }

    @Test
    void testCreateWithMinimalIntents()
    {
        TestJDABuilder builder = new TestJDABuilder(0);
        builder.applyIntents();

        assertThatLoggingFrom(builder::checkIntents).matchesSnapshot();
    }

    @Test
    void testCreateWithAllCacheAllIntents()
    {
        TestJDABuilder builder = new TestJDABuilder(ALL_INTENTS);
        builder.applyIntents();
        builder.enableCache(EnumSet.allOf(CacheFlag.class));

        assertThatLoggingFrom(builder::checkIntents).isEmpty();
    }

    @Test
    void testCreateWithAllCacheNoIntents()
    {
        TestJDABuilder builder = new TestJDABuilder(0);
        builder.applyIntents();
        builder.enableCache(EnumSet.allOf(CacheFlag.class));

        assertThatIllegalArgumentException()
            .isThrownBy(builder::checkIntents)
            .withMessage("Cannot use CacheFlag.ACTIVITY without GatewayIntent.GUILD_PRESENCES!");
    }

    @ParameterizedTest
    @EnumSource(CacheFlag.class)
    void testRequiredIntentForCacheFlagMissing(CacheFlag cacheFlag)
    {
        TestJDABuilder builder = new TestJDABuilder(0);
        builder.applyIntents();
        builder.enableCache(cacheFlag);

        if (cacheFlag.getRequiredIntent() != null)
        {
            assertThatIllegalArgumentException()
                    .isThrownBy(builder::checkIntents)
                    .withMessage(String.format("Cannot use CacheFlag.%s without GatewayIntent.%s!", cacheFlag, cacheFlag.getRequiredIntent()));
        }
        else
        {
            assertThatNoException().isThrownBy(builder::checkIntents);
        }
    }

    @ParameterizedTest
    @EnumSource(CacheFlag.class)
    void testRequiredIntentForCacheFlagEnabled(CacheFlag cacheFlag)
    {
        GatewayIntent requiredIntent = cacheFlag.getRequiredIntent();
        TestJDABuilder builder = new TestJDABuilder(requiredIntent != null ? requiredIntent.getRawValue() : 0);
        builder.applyIntents();
        builder.enableCache(cacheFlag);

        assertThatNoException().isThrownBy(builder::checkIntents);

        builder = new TestJDABuilder(0);
        builder.applyIntents();
        if (requiredIntent != null)
            builder.setEnabledIntents(requiredIntent);
        builder.enableCache(cacheFlag);

        assertThatNoException().isThrownBy(builder::checkIntents);
    }

    @Test
    void testDefaultMinimal()
    {
        TestJDABuilder builder = new TestJDABuilder(0);
        builder.applyIntents();
        builder.applyDefault();

        EnumSet<CacheFlag> defaultDisabled = CacheFlag.getPrivileged();
        EnumSet<CacheFlag> flags = EnumSet.allOf(CacheFlag.class);
        flags.removeIf(flag -> flag.getRequiredIntent() == null || defaultDisabled.contains(flag));

        assertThatLoggingFrom(builder::checkIntents).matchesSnapshot();
    }

    @Test
    void testChunkingWithMissingIntent()
    {
        TestJDABuilder builder = new TestJDABuilder(0);
        builder.applyIntents();
        builder.setChunkingFilter(ChunkingFilter.ALL);

        assertThatLoggingFrom(builder::checkIntents)
            .containsLine("Member chunking is disabled due to missing GUILD_MEMBERS intent.")
            .matchesSnapshot();
    }

    @EnumSource
    @ParameterizedTest
    @SuppressWarnings("deprecation")
    void testDeprecatedIntentDoesNotDisableCache(IntentTestCase testCase)
    {
        TestJDABuilder builder;

        switch (testCase)
        {
        case PASSED_TO_FACTORY:
            builder = new TestJDABuilder(GatewayIntent.GUILD_EMOJIS_AND_STICKERS.getRawValue());
            builder.applyIntents();
            break;
        case PASSED_TO_RELATIVE:
            builder = new TestJDABuilder(0);
            builder.applyLight();
            builder.enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
            builder.enableCache(CacheFlag.EMOJI, CacheFlag.STICKER);
            break;
        case PASSED_TO_SETTER:
            builder = new TestJDABuilder(0);
            builder.applyLight();
            builder.setEnabledIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
            builder.enableCache(CacheFlag.EMOJI, CacheFlag.STICKER);
            break;
        default:
            throw new AssertionError("Unexpected test case " + testCase);
        }


        assertThatLoggingFrom(
            () -> assertThatNoException().isThrownBy(builder::checkIntents)
        )
            .doesNotContainLineMatching(log -> log.contains("CacheFlag." + CacheFlag.EMOJI))
            .doesNotContainLineMatching(log -> log.contains("CacheFlag." + CacheFlag.STICKER));
    }

    static class TestJDABuilder extends JDABuilder
    {
        public TestJDABuilder(int intents)
        {
            super(TOKEN, intents);
        }

        @Override
        public JDABuilder applyDefault()
        {
            return super.applyDefault();
        }

        @Override
        public JDABuilder applyLight()
        {
            return super.applyLight();
        }

        @Override
        public JDABuilder applyIntents()
        {
            return super.applyIntents();
        }

        @Override
        public void checkIntents()
        {
            super.checkIntents();
        }
    }

    enum IntentTestCase
    {
        PASSED_TO_FACTORY,
        PASSED_TO_RELATIVE,
        PASSED_TO_SETTER;
    }
}
