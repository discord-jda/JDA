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

package net.dv8tion.jda.entities.channel;

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

import static org.junit.jupiter.api.Assertions.*;
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
        assertEquals(VALID_SORT_ORDER, output);

        output = toListString(cache.parallelStream());
        assertEquals(VALID_SORT_ORDER, output);
    }

    @Test
    void testUnsortedStream()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.streamUnordered());
        assertNotEquals(VALID_SORT_ORDER, output);

        output = toListString(cache.parallelStreamUnordered());
        assertNotEquals(VALID_SORT_ORDER, output);

        output = cache.applyStream(ChannelCacheViewTest::toListString);
        assertNotEquals(VALID_SORT_ORDER, output);
    }

    @Test
    void testAsListWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.asList().stream());

        assertEquals(VALID_SORT_ORDER, output);

        SortedChannelCacheView<VoiceChannel> voiceView = cache.ofType(VoiceChannel.class);
        List<VoiceChannel> fromOfType = voiceView.asList();
        List<GuildChannel> voiceChannelFilter = cache.applyStream(stream -> stream.filter(VoiceChannel.class::isInstance).collect(Collectors.toList()));

        assertEquals(voiceView.size(), voiceChannelFilter.size());
        assertTrue(fromOfType.containsAll(voiceChannelFilter), "The filtered CacheView must contain all of VoiceChannel");
        assertTrue(voiceChannelFilter.containsAll(fromOfType), "The filtered CacheView must contain exactly all of VoiceChannel");
    }

    @Test
    void testAsSetWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        String output = toListString(cache.asSet().stream());

        assertEquals(VALID_SORT_ORDER, output);

        SortedChannelCacheView<VoiceChannel> voiceView = cache.ofType(VoiceChannel.class);
        Set<VoiceChannel> fromOfType = voiceView.asSet();
        Set<GuildChannel> voiceChannelFilter = cache.applyStream(stream -> stream.filter(VoiceChannel.class::isInstance).collect(Collectors.toSet()));

        assertEquals(voiceView.size(), voiceChannelFilter.size());
        assertTrue(fromOfType.containsAll(voiceChannelFilter), "The filtered CacheView must contain all of VoiceChannel");
        assertTrue(voiceChannelFilter.containsAll(fromOfType), "The filtered CacheView must contain exactly all of VoiceChannel");
    }

    @Test
    void testSizeWorks()
    {
        SortedChannelCacheView<GuildChannel> cache = getMockedGuildCache();
        NavigableSet<GuildChannel> asSet = cache.asSet();

        assertEquals(asSet.size(), cache.size());

        SortedChannelCacheView<GuildMessageChannel> ofTypeMessage = cache.ofType(GuildMessageChannel.class);
        Set<GuildChannel> filterMessageType = asSet.stream().filter(GuildMessageChannel.class::isInstance).collect(Collectors.toSet());

        assertEquals(filterMessageType.size(), ofTypeMessage.size());
    }

    @Test
    void testEmptyWorks()
    {
        SortedChannelCacheView<GuildChannel> empty = new SortedChannelCacheViewImpl<>(GuildChannel.class);

        assertTrue(empty.isEmpty(), "New cache must be empty");

        SortedChannelCacheViewImpl<GuildChannel> filled = getMockedGuildCache();

        assertFalse(filled.ofType(GuildMessageChannel.class).isEmpty(), "Filtered cache must not be empty before remove");

        filled.removeIf(GuildMessageChannel.class, (c) -> true);

        assertFalse(filled.isEmpty(), "Filled cache must not be empty");
        assertTrue(filled.ofType(GuildMessageChannel.class).isEmpty(), "Filtered cache must be empty");
    }

    @Test
    void testRemoveWorks()
    {
        SortedChannelCacheViewImpl<GuildChannel> cache = getMockedGuildCache();
        Supplier<List<GuildChannel>> getByName = () -> cache.getElementsByName("TEXT without parent", true);
        Supplier<List<GuildMessageChannel>> getOfType = () -> cache.ofType(GuildMessageChannel.class).asList();

        GuildChannel textWithoutParent = getByName.get().get(0);

        assertSame(textWithoutParent, cache.remove(textWithoutParent), "Remove returns instance");
        assertTrue(getByName.get().isEmpty(), "Channel should be removed");

        List<GuildMessageChannel> messageChannels = getOfType.get();

        assertFalse(messageChannels.isEmpty(), "Message channels should not be removed");

        cache.removeIf(GuildChannel.class, GuildMessageChannel.class::isInstance);

        messageChannels = getOfType.get();

        assertTrue(messageChannels.isEmpty(), "Message channels should be removed");
    }
}
