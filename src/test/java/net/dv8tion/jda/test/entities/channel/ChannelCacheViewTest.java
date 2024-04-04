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

package net.dv8tion.jda.test.entities.channel;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.utils.cache.SortedChannelCacheView;
import net.dv8tion.jda.internal.utils.ChannelUtil;
import net.dv8tion.jda.internal.utils.cache.SortedChannelCacheViewImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChannelCacheViewTest
{
    private static long counter = 0;

    private static final String VALID_SORT_ORDER = String.join("\n",
        "TEXT without parent",
        "NEWS without parent",
        "TEXT parent of GUILD_PRIVATE_THREAD",
        "GUILD_PRIVATE_THREAD",
        "NEWS parent of GUILD_NEWS_THREAD",
        "GUILD_NEWS_THREAD",
        "FORUM parent of GUILD_PUBLIC_THREAD",
        "GUILD_PUBLIC_THREAD",
        "FORUM without parent",
        "MEDIA without parent",
        "VOICE without parent",
        "STAGE without parent",
        "CATEGORY parent of TEXT",
        "TEXT with parent",
        "CATEGORY parent of VOICE",
        "VOICE with parent",
        "CATEGORY without parent",
        "CATEGORY parent of NEWS",
        "NEWS with parent",
        "CATEGORY parent of STAGE",
        "STAGE with parent",
        "CATEGORY parent of FORUM",
        "FORUM with parent",
        "CATEGORY parent of MEDIA",
        "MEDIA with parent"
    );

    @SuppressWarnings("unchecked")
    private static <T extends Channel> T mockChannel(ChannelType type, String name)
    {
        return (T) mockChannel(type.getInterface(), type, name);
    }

    @SafeVarargs
    private static <T extends Channel> T mockChannel(Class<T> clazz, ChannelType type, String name, Class<? extends Channel>... extraInterfaces)
    {
        T mock = extraInterfaces.length > 0 ? mock(clazz, withSettings().extraInterfaces(extraInterfaces)) : mock(clazz);
        when(mock.getType())
            .thenReturn(type);
        when(mock.toString())
            .thenReturn(name);
        when(mock.getName())
            .thenReturn(name);
        when(mock.getIdLong())
            .thenReturn(type.ordinal() + (counter++));
        if (IPositionableChannel.class.isAssignableFrom(clazz))
        {
            IPositionableChannel positionable = (IPositionableChannel) mock;
            when(positionable.getPositionRaw())
                .thenReturn(type.ordinal() + (int) (counter++));
        }
        if (GuildChannel.class.isAssignableFrom(clazz))
        {
            GuildChannel comparable = (GuildChannel) mock;
            when(comparable.compareTo(any()))
                .then((args) -> ChannelUtil.compare((GuildChannel) args.getMock(), args.getArgument(0)));
        }
        return mock;
    }

    private static IThreadContainerUnion getThreadContainer(ChannelType threadType)
    {
        switch (threadType)
        {
        case GUILD_PRIVATE_THREAD:
            return mockChannel(IThreadContainerUnion.class, ChannelType.TEXT, "TEXT parent of " + threadType, GuildMessageChannel.class);
        case GUILD_NEWS_THREAD:
            return mockChannel(IThreadContainerUnion.class, ChannelType.NEWS, "NEWS parent of " + threadType, GuildMessageChannel.class);
        case GUILD_PUBLIC_THREAD:
            return mockChannel(IThreadContainerUnion.class, ChannelType.FORUM, "FORUM parent of " + threadType, IPostContainer.class);
        default:
            throw new IllegalStateException("Cannot map unknown thread type " + threadType);
        }
    }

    private static SortedChannelCacheViewImpl<GuildChannel> getMockedGuildCache()
    {
        SortedChannelCacheViewImpl<GuildChannel> view = new SortedChannelCacheViewImpl<>(GuildChannel.class);

        for (ChannelType type : ChannelType.values())
        {
            Class<? extends Channel> channelType = type.getInterface();

            if (ICategorizableChannel.class.isAssignableFrom(channelType))
            {
                Category category = mockChannel(ChannelType.CATEGORY, "CATEGORY parent of " + type);
                ICategorizableChannel channel = mockChannel(type, type + " with parent");
                long categoryId = category.getIdLong();

                when(channel.getParentCategoryIdLong())
                    .thenReturn(categoryId);
                when(channel.getParentCategory())
                    .thenReturn(category);

                view.put(category);
                view.put(channel);

                GuildChannel noParent = mockChannel(type, type + " without parent");
                view.put(noParent);
            }
            else if (ThreadChannel.class.isAssignableFrom(channelType))
            {
                IThreadContainerUnion parent = getThreadContainer(type);
                ChannelType containerType = parent.getType();
                when(parent.toString())
                        .thenReturn(containerType + " parent of " + type);

                ThreadChannel thread = mockChannel(type, type.name());
                when(thread.getParentChannel())
                    .thenReturn(parent);

                view.put(parent);
                view.put(thread);
            }
            else if (GuildChannel.class.isAssignableFrom(channelType))
            {
                GuildChannel channel = mockChannel(type, type + " without parent");
                view.put(channel);
            }
        }

        return view;
    }

    private static String toListString(Stream<?> stream)
    {
        return stream.map(Objects::toString).collect(Collectors.joining("\n"));
    }

    @Test
    void testSortedStream()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.stream());
        assertThat(output).isEqualTo(VALID_SORT_ORDER);

        output = toListString(cache.parallelStream());
        assertThat(output).isEqualTo(VALID_SORT_ORDER);
    }

    @Test
    void testUnsortedStream()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.streamUnordered());
        assertThat(output).isNotEqualTo(VALID_SORT_ORDER);

        output = toListString(cache.parallelStreamUnordered());
        assertThat(output).isNotEqualTo(VALID_SORT_ORDER);

        output = cache.applyStream(ChannelCacheViewTest::toListString);
        assertThat(output).isNotEqualTo(VALID_SORT_ORDER);
    }

    @Test
    void testAsListWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.asList().stream());

        assertThat(output).isEqualTo(VALID_SORT_ORDER);

        SortedChannelCacheView<VoiceChannel> voiceView = cache.ofType(VoiceChannel.class);
        List<VoiceChannel> fromOfType = voiceView.asList();
        List<GuildChannel> voiceChannelFilter = cache.applyStream(stream -> stream.filter(VoiceChannel.class::isInstance).collect(Collectors.toList()));

        assertThat(voiceChannelFilter)
            .hasSameSizeAs(voiceView);
        assertThat(voiceChannelFilter)
            .hasSameElementsAs(fromOfType);
    }

    @Test
    void testAsSetWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.asSet().stream());

        assertThat(output).isEqualTo(VALID_SORT_ORDER);

        SortedChannelCacheView<VoiceChannel> voiceView = cache.ofType(VoiceChannel.class);
        Set<VoiceChannel> fromOfType = voiceView.asSet();
        Set<GuildChannel> voiceChannelFilter = cache.applyStream(stream -> stream.filter(VoiceChannel.class::isInstance).collect(Collectors.toSet()));

        assertThat(voiceChannelFilter)
            .hasSize((int) voiceView.size());
        assertThat(voiceChannelFilter)
            .hasSameElementsAs(fromOfType);
    }

    @Test
    void testSizeWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        NavigableSet<GuildChannel> asSet = cache.asSet();

        assertThat(cache).hasSameSizeAs(asSet);

        SortedChannelCacheView<GuildMessageChannel> ofTypeMessage = cache.ofType(GuildMessageChannel.class);
        Set<GuildChannel> filterMessageType = asSet.stream().filter(GuildMessageChannel.class::isInstance).collect(Collectors.toSet());

        assertThat(ofTypeMessage).hasSameSizeAs(filterMessageType);
    }

    @Test
    void testEmptyWorks()
    {
        SortedChannelCacheView<GuildChannel> empty = new SortedChannelCacheViewImpl<>(GuildChannel.class);

        assertThat(empty).isEmpty();

        SortedChannelCacheViewImpl<GuildChannel> filled = getMockedGuildCache();

        assertThat(filled.ofType(GuildMessageChannel.class))
            .as("Filtered cache must not be empty before remove")
            .isNotEmpty();

        filled.removeIf(GuildMessageChannel.class, (c) -> true);

        assertThat(filled)
            .as("Filled cache must not be empty")
            .isNotEmpty();
        assertThat(filled.ofType(GuildMessageChannel.class))
            .as("Filtered cache must be empty")
            .isEmpty();
    }

    @Test
    void testRemoveWorks()
    {
        SortedChannelCacheViewImpl<GuildChannel> cache = getMockedGuildCache();
        Supplier<List<GuildChannel>> getByName = () -> cache.getElementsByName("TEXT without parent", true);
        Supplier<List<GuildMessageChannel>> getOfType = () -> cache.ofType(GuildMessageChannel.class).asList();

        GuildChannel textWithoutParent = getByName.get().get(0);

        assertThat(textWithoutParent)
            .as("Remove returns instance")
            .isSameAs(cache.remove(textWithoutParent));
        assertThat(getByName.get())
            .as("Channel should be removed")
            .isEmpty();

        List<GuildMessageChannel> messageChannels = getOfType.get();

        assertThat(messageChannels).isNotEmpty();

        cache.removeIf(GuildChannel.class, GuildMessageChannel.class::isInstance);

        messageChannels = getOfType.get();

        assertThat(messageChannels).isEmpty();
    }
}
