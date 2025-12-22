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

package net.dv8tion.jda.test.compliance;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.WebhookType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.internal.generated.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EnumComplianceTest {
    abstract static class AbstractEnumComplianceTest<T, E extends Enum<E>, G extends Enum<G>> {

        abstract Class<G> getGeneratedEnum();

        abstract Class<E> getJDAEnum();

        abstract T getRawJDAValue(E variant);

        abstract T getRawDiscordValue(G variant);

        EnumSet<G> getIgnoredSet() {
            return EnumSet.noneOf(getGeneratedEnum());
        }

        @Test
        void testCompleteness() {
            EnumSet<G> generatedEnum = EnumSet.allOf(getGeneratedEnum());
            EnumSet<E> jdaEnum = EnumSet.allOf(getJDAEnum());

            EnumSet<G> ignored = getIgnoredSet();
            Set<T> jdaValues = jdaEnum.stream().map(this::getRawJDAValue).collect(Collectors.toSet());

            assertThat(generatedEnum)
                    .filteredOn(generated -> !ignored.contains(generated))
                    .allSatisfy(generated -> {
                        T rawValue = getRawDiscordValue(generated);
                        assertThat(jdaValues)
                                .as("Expecting enum constant %s (%s)", generated, rawValue)
                                .contains(rawValue);
                    });

            assertThat(ignored).allSatisfy(generated -> {
                T rawValue = getRawDiscordValue(generated);
                assertThat(jdaValues)
                        .as("Expecting enum constant %s (%s) to be unused", generated, rawValue)
                        .doesNotContain(rawValue);
            });
        }
    }

    @Nested
    class DiscordLocaleComplianceTest
            extends AbstractEnumComplianceTest<String, DiscordLocale, AvailableLocalesEnumDto> {

        @Test
        void testDiscordLocaleDefinitions() {
            assertThat(Arrays.stream(DiscordLocale.values())).allSatisfy(locale -> {
                assertThat(locale.getLocale()).isNotBlank();
                assertThat(locale.getLanguageName()).isNotBlank();
                assertThat(locale.getNativeName()).isNotBlank();
            });
        }

        @Override
        Class<AvailableLocalesEnumDto> getGeneratedEnum() {
            return AvailableLocalesEnumDto.class;
        }

        @Override
        Class<DiscordLocale> getJDAEnum() {
            return DiscordLocale.class;
        }

        @Override
        String getRawJDAValue(DiscordLocale variant) {
            return variant.getLocale();
        }

        @Override
        String getRawDiscordValue(AvailableLocalesEnumDto variant) {
            return variant.getId();
        }
    }

    @Nested
    class MessageTypeComplianceTest extends AbstractEnumComplianceTest<Integer, MessageType, MessageTypeDto> {
        @Override
        EnumSet<MessageTypeDto> getIgnoredSet() {
            // Undocumented types
            return EnumSet.of(MessageTypeDto.HD_STREAMING_UPGRADED);
        }

        @Override
        Class<MessageTypeDto> getGeneratedEnum() {
            return MessageTypeDto.class;
        }

        @Override
        Class<MessageType> getJDAEnum() {
            return MessageType.class;
        }

        @Override
        Integer getRawJDAValue(MessageType variant) {
            return variant.getId();
        }

        @Override
        Integer getRawDiscordValue(MessageTypeDto variant) {
            return variant.getId();
        }
    }

    @Nested
    class ChannelTypeComplianceTest extends AbstractEnumComplianceTest<Integer, ChannelType, ChannelTypesDto> {

        @Override
        Class<ChannelTypesDto> getGeneratedEnum() {
            return ChannelTypesDto.class;
        }

        @Override
        Class<ChannelType> getJDAEnum() {
            return ChannelType.class;
        }

        @Override
        Integer getRawJDAValue(ChannelType variant) {
            return variant.getId();
        }

        @Override
        Integer getRawDiscordValue(ChannelTypesDto variant) {
            return variant.getId();
        }
    }

    @Nested
    class AuditLogActionTypeComplianceTest
            extends AbstractEnumComplianceTest<Integer, ActionType, AuditLogActionTypesDto> {

        @Override
        EnumSet<AuditLogActionTypesDto> getIgnoredSet() {
            return EnumSet.of(
                    // Undocumented
                    AuditLogActionTypesDto.HARMFUL_LINKS_BLOCKED_MESSAGE,
                    // Experiments, only accessible to specific guilds.
                    AuditLogActionTypesDto.GUILD_HOME_FEATURE_ITEM,
                    AuditLogActionTypesDto.GUILD_HOME_REMOVE_ITEM);
        }

        @Override
        Class<AuditLogActionTypesDto> getGeneratedEnum() {
            return AuditLogActionTypesDto.class;
        }

        @Override
        Class<ActionType> getJDAEnum() {
            return ActionType.class;
        }

        @Override
        Integer getRawJDAValue(ActionType variant) {
            return variant.getKey();
        }

        @Override
        Integer getRawDiscordValue(AuditLogActionTypesDto variant) {
            return variant.getId();
        }
    }

    @Nested
    class InviteTypeComplianceTest extends AbstractEnumComplianceTest<Integer, Invite.InviteType, InviteTypesDto> {

        @Override
        Class<InviteTypesDto> getGeneratedEnum() {
            return InviteTypesDto.class;
        }

        @Override
        Class<Invite.InviteType> getJDAEnum() {
            return Invite.InviteType.class;
        }

        @Override
        Integer getRawJDAValue(Invite.InviteType variant) {
            return variant.ordinal();
        }

        @Override
        Integer getRawDiscordValue(InviteTypesDto variant) {
            return variant.getId();
        }
    }

    @Nested
    class WebhookTypeComplianceTest extends AbstractEnumComplianceTest<Integer, WebhookType, WebhookTypesDto> {

        @Override
        EnumSet<WebhookTypesDto> getIgnoredSet() {
            // Undocumented
            return EnumSet.of(WebhookTypesDto.APPLICATION_INCOMING);
        }

        @Override
        Class<WebhookTypesDto> getGeneratedEnum() {
            return WebhookTypesDto.class;
        }

        @Override
        Class<WebhookType> getJDAEnum() {
            return WebhookType.class;
        }

        @Override
        Integer getRawJDAValue(WebhookType variant) {
            return variant.getKey();
        }

        @Override
        Integer getRawDiscordValue(WebhookTypesDto variant) {
            return variant.getId();
        }
    }
}
