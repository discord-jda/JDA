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

import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.restaction.pagination.EntitlementPaginationActionImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.util.LinkedHashSet;
import java.util.Set;

import static net.dv8tion.jda.api.requests.Method.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EntitlementPaginationTest extends IntegrationTest
{
    protected static final String routeTemplate = "applications/%d/entitlements%s";

    @Spy
    protected SelfUser selfUser;

    protected EntitlementPaginationActionImpl action;

    private DataObject fakeEntitlement(String id)
    {
        return DataObject.empty()
            .put("id", id)
            .put("sku_id", randomSnowflake())
            .put("application_id", randomSnowflake())
            .put("user_id", randomSnowflake())
            .put("type", 8)
            .put("deleted", false);
    }

    @BeforeEach
    void setupSelfUser()
    {
        when(selfUser.getApplicationIdLong()).thenReturn(Constants.BUTLER_USER_ID);
        when(jda.getSelfUser()).thenReturn(selfUser);

        action = new EntitlementPaginationActionImpl(jda);
    }

    @Test
    void testParsingFailure()
    {
        assertThatNextRequest()
            .hasMethod(GET)
            .hasCompiledRoute(String.format(routeTemplate, Constants.BUTLER_USER_ID, "?limit=100"));

        action.queue();

        DataArray responseBody = DataArray.empty()
                .add(DataObject.empty()); // Invalid entitlement object

        whenSuccess(action, responseBody, response ->
            assertThat(response).isEmpty() // Is logged and skipped
        );
    }

    @Test
    void testDefaultPagination()
    {
        assertThatNextRequest()
            .hasQueryParams("limit", "100");

        action.queue();

        DataArray array = DataArray.empty()
            .add(fakeEntitlement("2"))
            .add(fakeEntitlement("1"));

        whenSuccess(action, array, response ->
            assertThat(response)
                .hasSize(2)
                .map(Entitlement::getId)
                .containsExactly("2", "1")
        );

        assertThatNextRequest()
            .hasQueryParams("limit", "100", "before", "1");

        action.queue();

        whenSuccess(action, DataArray.empty(), response ->
            assertThat(response)
                .isEmpty()
        );

        assertThat(action.cacheSize()).isEqualTo(2);
        assertThat(action.getCached())
            .hasSize(2)
            .map(Entitlement::getId)
            .containsExactly("2", "1");

    }

    @Test
    void testReversePagination()
    {
        assertThatNextRequest()
            .hasQueryParams("limit", 100, "after", 0);

        action.reverse().queue();

        DataArray array = DataArray.empty()
            .add(fakeEntitlement("1"))
            .add(fakeEntitlement("2"));

        whenSuccess(action, array, response ->
            assertThat(response)
                .hasSize(2)
                .map(Entitlement::getId)
                .containsExactly("1", "2")
        );

        assertThatNextRequest()
            .hasQueryParams("limit", "100", "after", "2");

        action.queue();

        whenSuccess(action, DataArray.empty(), response ->
            assertThat(response)
                .isEmpty()
        );

        assertThat(action.cacheSize()).isEqualTo(2);
        assertThat(action.getCached())
            .hasSize(2)
            .map(Entitlement::getId)
            .containsExactly("1", "2");
    }

    @Test
    void testSkipTo()
    {
        long skipId = Math.abs(random.nextLong());

        assertThatNextRequest()
            .hasQueryParams("limit", "100", "before", skipId);

        action.skipTo(skipId).queue();
    }

    @Nested
    class Filter
    {
        @Test
        void byExcludeEnded()
        {
            assertThatNextRequest()
                .hasQueryParams("limit", "100", "exclude_ended", "true");

            action.excludeEnded(true).queue();
        }

        @Test
        void bySkuIds()
        {
            Set<String> sku = new LinkedHashSet<>();

            for (int i = -5; i < random.nextInt(10); i++)
                sku.add(Long.toUnsignedString(random.nextLong()));

            assertThatNextRequest()
                .hasQueryParams("limit", "100", "sku_ids", String.join(",", sku));

            action.skuIds(sku).queue();
        }

        @Test
        void byUserId()
        {
            assertThatNextRequest()
                .hasQueryParams( "limit", 100, "user_id", Constants.MINN_USER_ID);

            action.user(User.fromId(Constants.MINN_USER_ID)).queue();
        }

        @Test
        void byGuildId()
        {
            assertThatNextRequest()
                .hasQueryParams("limit", 100, "guild_id", Constants.GUILD_ID);

            action.guild(Constants.GUILD_ID).queue();
        }
    }
}
