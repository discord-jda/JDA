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

package net.dv8tion.jda.test.assertions.restaction;

import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import net.dv8tion.jda.test.PrettyRepresentation;
import net.dv8tion.jda.test.util.SnapshotHandler;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Contract;
import org.mockito.ThrowingConsumer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doNothing;

public class RestActionAssertions implements ThrowingConsumer<Request<?>>
{
    private final SnapshotHandler snapshotHandler;
    private final RestAction<?> action;
    private final List<ThrowingConsumer<Request<?>>> assertions = new ArrayList<>();
    private Consumer<? super DataObject> normalizeRequestBody = (v) -> {};

    public RestActionAssertions(SnapshotHandler snapshotHandler, RestAction<?> action)
    {
        this.snapshotHandler = snapshotHandler;
        this.action = action;
    }

    @CheckReturnValue
    public static RestActionAssertions assertThatNextAction(SnapshotHandler snapshotHandler, Requester requester, RestAction<?> action)
    {
        RestActionAssertions assertions = new RestActionAssertions(snapshotHandler, action);
        doNothing().when(requester).request(assertArg(assertions::acceptThrows));
        return assertions;
    }

    public void whenQueueCalled()
    {
        action.queue();
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions withNormalizedBody(@Nonnull Consumer<? super DataObject> normalizer)
    {
        this.normalizeRequestBody = normalizer;
        return this;
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions checkAssertions(@Nonnull ThrowingConsumer<Request<?>> assertion)
    {
        assertions.add(assertion);
        return this;
    }

    @CheckReturnValue
    @Contract("->this")
    public RestActionAssertions hasMultipartBody()
    {
        return checkAssertions(request -> {
            RequestBody body = request.getBody();
            assertThat(body).isNotNull();
            MediaType mediaType = body.contentType();
            assertThat(mediaType).isNotNull();

            assertThat(mediaType.toString())
                .startsWith("multipart/form-data; boundary=");
        });
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasBodyEqualTo(@Nonnull DataObject expected)
    {
        return checkAssertions(request -> {
            DataObject dataObject = getRequestBody(request);
            normalizeRequestBody.accept(expected);

            assertThat(dataObject.toPrettyString())
                .as("RestAction should send request using expected request body")
                .isEqualTo(expected.toPrettyString());
        });
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasBodyMatching(@Nonnull Predicate<? super DataObject> condition)
    {
        return checkAssertions(request -> {
            DataObject body = getRequestBody(request);
            assertThat(body)
                .withRepresentation(new PrettyRepresentation())
                .matches(condition);
        });
    }

    @CheckReturnValue
    @Contract("->this")
    public RestActionAssertions hasBodyMatchingSnapshot()
    {
        return hasBodyMatchingSnapshot(null);
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasBodyMatchingSnapshot(String suffix)
    {
        return checkAssertions(request -> {
            DataObject body = getRequestBody(request);
            snapshotHandler.compareWithSnapshot(body, suffix);
        });
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasMethod(@Nonnull Method method)
    {
        return checkAssertions(request ->
            assertThat(request.getRoute().getMethod())
                .as("RestAction should send request using expected HTTP Method")
                .isEqualTo(method)
        );
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasCompiledRoute(@Nonnull String route)
    {
        return checkAssertions(request ->
            assertThat(request.getRoute().getCompiledRoute())
                .as("RestAction should send request using expected REST endpoint")
                .isEqualTo(route)
        );
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasQueryParams(@Nonnull Object... params)
    {
        assertThat(params.length).isEven();

        Map<String, String> expectedQuery = new LinkedHashMap<>();

        for (int i = 0; i < params.length; i += 2)
            expectedQuery.put(String.valueOf(params[i]), EncodingUtil.encodeUTF8(String.valueOf(params[i + 1])));

        return checkAssertions(request -> {
            Map<String, String> actualQuery = new LinkedHashMap<>();
            String[] query = request.getRoute().getCompiledRoute().split("[?&=]");

            for (int i = 1; i < query.length; i += 2)
                actualQuery.put(query[i], query[i + 1]);

            assertThat(actualQuery)
                .containsExactlyEntriesOf(expectedQuery);
        });
    }

    @CheckReturnValue
    @Contract("_->this")
    public RestActionAssertions hasAuditReason(@Nonnull String reason)
    {
        return checkAssertions(request ->
            assertThat(request.getHeaders())
                .as("RestAction should set header")
                .contains(new AbstractMap.SimpleEntry<>("X-Audit-Log-Reason", reason))
        );
    }

    @Override
    public void acceptThrows(Request<?> request) throws Throwable
    {
        for (ThrowingConsumer<Request<?>> assertion : assertions)
        {
            assertion.acceptThrows(request);
        }
    }

    @Nonnull
    private DataObject getRequestBody(@Nonnull Request<?> request)
    {
        Object body = request.getRawBody();
        assertThat(body)
                .isNotNull()
                .isInstanceOf(DataObject.class);

        DataObject dataObject = (DataObject) body;
        normalizeRequestBody.accept(dataObject);
        return dataObject;
    }
}
