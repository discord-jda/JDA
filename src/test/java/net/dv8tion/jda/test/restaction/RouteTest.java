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

package net.dv8tion.jda.test.restaction;

import net.dv8tion.jda.api.requests.Route;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RouteTest {
    private static final HttpUrl BASE_URL = HttpUrl.get("https://discord.com/api/v10/");

    @Test
    void testPathNavigationPrevention() {
        assertThat(Route.Users.GET_USER.compile("../test").getCompiledRoute()).isEqualTo("users/..%2Ftest");
        assertThat(Route.Users.GET_USER.compile("..\\test").getCompiledRoute()).isEqualTo("users/..%5Ctest");
    }

    @Test
    void testPathNavigationPreventionAlreadyEncodedSlashIsEncodedAgain() {
        assertThat(Route.Users.GET_USER.compile("..%2Ftest").getCompiledRoute()).isEqualTo("users/..%252Ftest");
    }

    @Test
    void testCompileRejectsWrongParameterCount() {
        assertThatIllegalArgumentException()
                .isThrownBy(Route.Users.GET_USER::compile)
                .withMessageContaining("incorrect amount of parameters provided");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Route.Users.GET_USER.compile("a", "b"))
                .withMessageContaining("incorrect amount of parameters provided");
    }

    @Test
    void testCompileRejectsNullParametersArray() {
        assertThatIllegalArgumentException().isThrownBy(() -> Route.Users.GET_USER.compile((String[]) null));
    }

    @Test
    void testQueryParamsEncodeValueToPreventInjection() {
        Route.CompiledRoute compiled =
                Route.Messages.GET_MESSAGE_HISTORY.compile("123").withQueryParams("after", "1&limit=100");

        assertThat(compiled.getCompiledRoute()).isEqualTo("channels/123/messages?after=1%26limit%3D100");
    }

    @Test
    void testQueryParamsRejectsOddLengthAndTooShort() {
        Route.CompiledRoute base = Route.Messages.GET_MESSAGE_HISTORY.compile("123");

        assertThatIllegalArgumentException().isThrownBy(base::withQueryParams).withMessageContaining("at least 2");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> base.withQueryParams("limit"))
                .withMessageContaining("at least 2");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> base.withQueryParams("limit", "10", "after"))
                .withMessageContaining("multiple of 2");
    }

    @Test
    void testQueryParamsRejectsEmptyKeyAndNullValue() {
        Route.CompiledRoute base = Route.Messages.GET_MESSAGE_HISTORY.compile("123");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> base.withQueryParams("", "10"))
                .withMessageContaining("Query key");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> base.withQueryParams("limit", null))
                .withMessageContaining("Query value");
    }

    @Test
    void testMajorParameterDoesNotLeakLongInteractionToken() {
        String longToken = "this_is_a_very_long_interaction_token_that_should_not_be_logged_directly";
        Route.CompiledRoute compiled = Route.Interactions.CALLBACK.compile("123", longToken);

        String major = compiled.getMajorParameters();

        assertThat(major).contains("interaction_token=");
        assertThat(major).doesNotContain(longToken);
    }

    @Test
    void testToHttpUrlBuildsUrlWithoutQueryParams() {
        HttpUrl url = Route.Users.GET_USER.compile("42").toHttpUrl(BASE_URL);

        assertThat(url.toString()).isEqualTo("https://discord.com/api/v10/users/42");
    }

    @Test
    void testToHttpUrlBuildsUrlWithEncodedQueryParams() {
        Route.CompiledRoute compiled =
                Route.Messages.GET_MESSAGE_HISTORY.compile("123").withQueryParams("after", "1&limit=100");

        HttpUrl url = compiled.toHttpUrl(BASE_URL);

        assertThat(url.toString()).isEqualTo("https://discord.com/api/v10/channels/123/messages?after=1%26limit%3D100");
    }

    @Test
    void testToHttpUrlDoesNotDecodeOrNormalizeEncodedPathSegments() {
        HttpUrl url = Route.Users.GET_USER.compile("..%2Ftest").toHttpUrl(BASE_URL);

        assertThat(url.toString()).isEqualTo("https://discord.com/api/v10/users/..%252Ftest");
    }
}
