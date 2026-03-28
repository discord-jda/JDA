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

package net.dv8tion.jda.internal.entities.messages;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.messages.MessageSearchAction;
import net.dv8tion.jda.api.entities.messages.MessageSearchResponse;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageSearchActionImpl extends RestActionImpl<MessageSearchResponse> implements MessageSearchAction {
    private final Guild guild;

    private Integer limit;
    private Integer offset;
    private String minId, maxId;
    private Integer slop;
    private String content;
    private Set<String> channels = Collections.emptySet();
    private Set<AuthorType> includedAuthorTypes = Collections.emptySet();
    private Set<AuthorType> excludedAuthorTypes = Collections.emptySet();
    private Set<String> authors = Collections.emptySet();
    private Set<String> mentionsUsers = Collections.emptySet();
    private Set<String> mentionsRoles = Collections.emptySet();
    private Boolean mentionsEveryone;
    private Set<String> repliesToUsers = Collections.emptySet();
    private Set<String> repliesToMessages = Collections.emptySet();
    private Boolean pinned;
    private Set<HasType> includedHasTypes = Collections.emptySet();
    private Set<HasType> excludedHasTypes = Collections.emptySet();
    private Set<EmbedType> embedTypes = Collections.emptySet();
    private Set<String> embedProviders = Collections.emptySet();
    private Set<String> linkHostnames = Collections.emptySet();
    private Set<String> attachmentFilenames = Collections.emptySet();
    private Set<String> attachmentExtensions = Collections.emptySet();
    private SortType sortBy = null;
    private SortOrder sortOrder = null;
    private Boolean includeNsfw;

    public MessageSearchActionImpl(Guild guild) {
        super(guild.getJDA(), Route.Guilds.SEARCH_MESSAGES.compile(guild.getId()));
        this.guild = guild;
    }

    @Nonnull
    @Override
    public MessageSearchAction limit(@Nullable Integer limit) {
        if (limit != null) {
            Checks.positive(limit, "Limit");
            Checks.check(limit <= 25, "Limit must be lower than or equal to 25");
        }
        this.limit = limit;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction offset(@Nullable Integer offset) {
        if (offset != null) {
            Checks.positive(offset, "Offset");
            Checks.check(offset <= 9975, "Offset must be lower than or equal to 9975");
        }
        this.offset = offset;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction minId(@Nullable Long minId) {
        if (minId != null) {
            Checks.notNegative(minId, "Min ID");
            this.minId = Long.toUnsignedString(minId);
        } else {
            this.minId = null;
        }
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction minId(@Nullable String minId) {
        if (minId != null) {
            Checks.isSnowflake(minId, "Min ID");
        }
        this.minId = minId;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction maxId(@Nullable Long maxId) {
        if (maxId != null) {
            Checks.notNegative(maxId, "Max ID");
            this.maxId = Long.toUnsignedString(maxId);
        } else {
            this.maxId = null;
        }
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction maxId(@Nullable String maxId) {
        if (maxId != null) {
            Checks.isSnowflake(maxId, "Max ID");
        }
        this.maxId = maxId;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction slop(@Nullable Integer slop) {
        if (slop != null) {
            Checks.notNegative(slop, "Slop");
            Checks.check(slop <= 100, "Slop must be lower than or equal to 100");
        }
        this.slop = slop;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction content(@Nullable String content) {
        if (content != null) {
            Checks.inRange(content, 0, 1024, "Content");
        }
        this.content = content;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction channels(@Nonnull Collection<? extends GuildMessageChannel> channels) {
        Checks.noneNull(channels, "Channels");
        Checks.check(channels.size() <= 500, "Cannot filter on more than 500 channels");
        for (GuildMessageChannel channel : channels) {
            Checks.check(
                    channel.getGuild().equals(guild),
                    "Channel %s is from a different guild (expected %s, was %s)",
                    channel,
                    guild,
                    channel.getGuild());
            Checks.checkAccess(guild.getSelfMember(), channel);
            if (!PermissionUtil.checkPermission(
                    channel.getPermissionContainer(), guild.getSelfMember(), Permission.MESSAGE_HISTORY)) {
                throw new InsufficientPermissionException(channel, Permission.MESSAGE_HISTORY);
            }
        }
        this.channels = channels.stream().map(GuildMessageChannel::getId).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction channels(@Nonnull long... channels) {
        Checks.notNull(channels, "Channels");
        Checks.check(channels.length <= 500, "Cannot filter on more than 500 channels");
        this.channels = Arrays.stream(channels).mapToObj(Long::toUnsignedString).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction channels(@Nonnull String... channels) {
        Checks.noneNull(channels, "Channels");
        for (String channel : channels) {
            Checks.isSnowflake(channel, "Channel");
        }
        this.channels = new HashSet<>(Arrays.asList(channels));
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction includeAuthorTypes(@Nonnull Collection<AuthorType> authorTypes) {
        Checks.noneNull(authorTypes, "Author types");
        this.includedAuthorTypes = Helpers.copyEnumSet(AuthorType.class, authorTypes);
        this.excludedAuthorTypes = Collections.emptySet();
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction excludeAuthorTypes(@Nonnull Collection<AuthorType> authorTypes) {
        Checks.noneNull(authorTypes, "Author types");
        this.includedAuthorTypes = Collections.emptySet();
        this.excludedAuthorTypes = Helpers.copyEnumSet(AuthorType.class, authorTypes);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction authors(@Nonnull Collection<? extends UserSnowflake> authors) {
        Checks.noneNull(authors, "Authors");
        this.authors = authors.stream().map(UserSnowflake::getId).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction mentionsUsers(@Nonnull Collection<? extends UserSnowflake> mentions) {
        Checks.noneNull(mentions, "Mentions");
        this.mentionsUsers = mentions.stream().map(UserSnowflake::getId).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction mentionsRoles(@Nonnull Collection<? extends Role> mentions) {
        Checks.noneNull(mentions, "Mentions");
        this.mentionsRoles = mentions.stream().map(Role::getId).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction mentionsEveryone(@Nullable Boolean mentionsEveryone) {
        this.mentionsEveryone = mentionsEveryone;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction repliesToUsers(@Nonnull Collection<? extends UserSnowflake> repliedTo) {
        Checks.noneNull(repliedTo, "Users");
        this.repliesToUsers = repliedTo.stream().map(UserSnowflake::getId).collect(Collectors.toSet());
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction repliesToMessages(@Nonnull Collection<String> repliedTo) {
        Checks.noneNull(repliedTo, "Messages");
        for (String messageId : repliedTo) {
            Checks.isSnowflake(messageId, "Message ID");
        }
        this.repliesToMessages = new HashSet<>(repliedTo);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction pinned(@Nullable Boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction includeHasTypes(@Nonnull Collection<HasType> hasTypes) {
        Checks.noneNull(hasTypes, "HasTypes");
        this.includedHasTypes = Helpers.copyEnumSet(HasType.class, hasTypes);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction excludeHasTypes(@Nonnull Collection<HasType> hasTypes) {
        Checks.noneNull(hasTypes, "HasTypes");
        this.excludedHasTypes = Helpers.copyEnumSet(HasType.class, hasTypes);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction embedTypes(@Nonnull Collection<EmbedType> embedTypes) {
        Checks.notNull(embedTypes, "Embed types");
        this.embedTypes = Helpers.copyEnumSet(EmbedType.class, embedTypes);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction embedProvider(@Nonnull Collection<String> embedProviders) {
        Checks.noneNull(embedProviders, "Embed providers");
        this.embedProviders = new HashSet<>(embedProviders);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction linkHostnames(@Nonnull Collection<String> linkHostnames) {
        Checks.noneNull(linkHostnames, "Link hostnames");
        this.linkHostnames = new HashSet<>(linkHostnames);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction attachmentFilenames(@Nonnull Collection<String> attachmentFilenames) {
        Checks.noneNull(attachmentFilenames, "Attachment filenames");
        this.attachmentFilenames = new HashSet<>(attachmentFilenames);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction attachmentExtensions(@Nonnull Collection<String> attachmentExtensions) {
        Checks.noneNull(attachmentExtensions, "Attachment extensions");
        this.attachmentExtensions = new HashSet<>(attachmentExtensions);
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction sortBy(@Nullable MessageSearchAction.SortType sortType) {
        this.sortBy = sortType;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction sortOrder(@Nullable MessageSearchAction.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction includeNsfw(boolean includeNsfw) {
        this.includeNsfw = includeNsfw;
        return this;
    }

    @Nonnull
    @Override
    public MessageSearchAction setCheck(BooleanSupplier checks) {
        return (MessageSearchAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public MessageSearchAction timeout(long timeout, @Nonnull TimeUnit unit) {
        return (MessageSearchAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public MessageSearchAction deadline(long timestamp) {
        return (MessageSearchAction) super.deadline(timestamp);
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        Route.CompiledRoute route = super.finalizeRoute();
        if (limit != null) {
            route = route.withQueryParams("limit", Integer.toString(limit));
        }
        if (offset != null) {
            route = route.withQueryParams("offset", Integer.toString(offset));
        }
        if (minId != null) {
            route = route.withQueryParams("min_id", minId);
        }
        if (maxId != null) {
            route = route.withQueryParams("max_id", maxId);
        }
        if (slop != null) {
            route = route.withQueryParams("slop", Integer.toString(slop));
        }
        if (content != null) {
            route = route.withQueryParams("content", content);
        }
        if (!channels.isEmpty()) {
            route = appendList(route, "channel_id", channels);
        }
        if (!includedAuthorTypes.isEmpty()) {
            route = appendList(route, "author_type", includedAuthorTypes, AuthorType::getValue);
        } else if (!excludedAuthorTypes.isEmpty()) {
            route = appendList(route, "author_type", excludedAuthorTypes, authorType -> "-" + authorType.getValue());
        }
        if (!authors.isEmpty()) {
            route = appendList(route, "author_id", authors);
        }
        if (!mentionsUsers.isEmpty()) {
            route = appendList(route, "mentions", mentionsUsers);
        }
        if (!mentionsRoles.isEmpty()) {
            route = appendList(route, "mentions_role_id", mentionsRoles);
        }
        if (mentionsEveryone != null) {
            route = route.withQueryParams("mention_everyone", Boolean.toString(mentionsEveryone));
        }
        if (!repliesToUsers.isEmpty()) {
            route = appendList(route, "replied_to_user_id", repliesToUsers);
        }
        if (!repliesToMessages.isEmpty()) {
            route = appendList(route, "replied_to_message_id", repliesToMessages);
        }
        if (pinned != null) {
            route = route.withQueryParams("pinned", Boolean.toString(pinned));
        }
        if (!includedHasTypes.isEmpty()) {
            route = appendList(route, "has", includedHasTypes, HasType::getValue);
        } else if (!excludedHasTypes.isEmpty()) {
            route = appendList(route, "has", includedHasTypes, hasType -> "-" + hasType.getValue());
        }
        if (!embedTypes.isEmpty()) {
            route = appendList(route, "embed_type", embedTypes, EmbedType::getValue);
        }
        if (!embedProviders.isEmpty()) {
            route = appendList(route, "embed_provider", embedProviders);
        }
        if (!linkHostnames.isEmpty()) {
            route = appendList(route, "link_hostname", linkHostnames);
        }
        if (!attachmentFilenames.isEmpty()) {
            route = appendList(route, "attachment_filename", attachmentFilenames);
        }
        if (!attachmentExtensions.isEmpty()) {
            route = appendList(route, "attachment_extension", attachmentExtensions);
        }
        if (sortBy != null) {
            route = route.withQueryParams("sort_by", sortBy.getValue());
        }
        if (sortOrder != null) {
            route = route.withQueryParams("sort_order", sortOrder.getValue());
        }
        if (includeNsfw != null) {
            route = route.withQueryParams("include_nsfw", Boolean.toString(includeNsfw));
        }

        return route;
    }

    @Nonnull
    private static Route.CompiledRoute appendList(
            @Nonnull Route.CompiledRoute route, @Nonnull String paramName, @Nonnull Collection<String> list) {
        for (String element : list) {
            route = route.withQueryParams(paramName, element);
        }
        return route;
    }

    @Nonnull
    private static <T> Route.CompiledRoute appendList(
            @Nonnull Route.CompiledRoute route,
            @Nonnull String paramName,
            @Nonnull Collection<T> list,
            @Nonnull Function<? super T, String> valueFunction) {
        for (T element : list) {
            route = route.withQueryParams(paramName, valueFunction.apply(element));
        }
        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<MessageSearchResponse> request) {
        MessageSearchResponse searchResponse;
        DataObject object = response.getObject();
        if (response.code == 202) {
            searchResponse = new MessageSearchResponseImpl.FailureImpl(
                    object.getInt("documents_indexed"), object.getInt("retry_after"));
        } else {
            searchResponse = new MessageSearchResponseImpl.BodyImpl(
                    readMessages(object),
                    object.getBoolean("doing_deep_historical_index"),
                    object.getInt("total_results"));
        }

        request.onSuccess(searchResponse);
    }

    private List<Message> readMessages(DataObject object) {
        Map<Long, ThreadChannel> threads = readThreadChannels(object);

        return Helpers.mapGracefully(
                        object
                                .getArray("messages")
                                // Flatten as the API returns a 2D array
                                .stream(DataArray::getArray)
                                .flatMap(array -> array.stream(DataArray::getObject)),
                        d -> {
                            long channelId = d.getUnsignedLong("channel_id");
                            GuildMessageChannel channel = threads.get(channelId);
                            if (channel == null) {
                                channel = guild.getChannelById(GuildMessageChannel.class, channelId);
                            }
                            if (channel == null) {
                                throw new IllegalStateException(Helpers.format(
                                        "Could not find a thread or a regular channel with ID %d in guild %s",
                                        channelId, guild.getId()));
                            }
                            return api.getEntityBuilder().createMessageWithChannel(d, channel, false);
                        },
                        "Unable to read a message from search results")
                .collect(Helpers.toUnmodifiableList());
    }

    @Nonnull
    private Map<Long, ThreadChannel> readThreadChannels(DataObject object) {
        if (object.isNull("threads")) {
            return Collections.emptyMap();
        }

        // Thread ID -> Thread member object
        Map<Long, DataObject> selfThreadMemberObjects = readSelfThreadMemberObjects(object);

        return Helpers.mapGracefully(
                        object.getArray("threads").stream(DataArray::getObject),
                        o -> {
                            // Put the self thread member, if it did join the thread
                            o.put("member", selfThreadMemberObjects.get(o.getUnsignedLong("id")));

                            return api.getEntityBuilder()
                                    .createThreadChannel((GuildImpl) guild, o, guild.getIdLong(), false);
                        },
                        "Unable to read a thread channel from search results")
                .collect(Collectors.toMap(ISnowflake::getIdLong, c -> c));
    }

    @Nonnull
    private static Map<Long, DataObject> readSelfThreadMemberObjects(DataObject object) {
        if (object.isNull("members")) {
            return Collections.emptyMap();
        }

        return object.getArray("members").stream(DataArray::getObject)
                .collect(Collectors.toMap(o -> o.getUnsignedLong("id"), o -> o));
    }
}
