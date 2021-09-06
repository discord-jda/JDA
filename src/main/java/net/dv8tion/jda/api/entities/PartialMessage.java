package net.dv8tion.jda.api.entities;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PartialMessage
{
    @NotNull
    String getNonce();

    @NotNull
    String getGuildId();

    long getGuildIdLong();

    @NotNull
    String getChannelId();

    long getChannelIdLong();

    @NotNull
    MessageMentions getMentions();

    @NotNull
    MessageComponents getComponents();

    @Nullable
    MessageReference getMessageReference();

    @Nullable
    String getContentRaw();

    @Nullable
    String getContentStripped();

    @Nullable
    String getContentDisplay();

    @NotNull
    List<MessageEmbed> getEmbeds();

    @NotNull
    List<Message.Attachment> getAttachments();

    @NotNull
    RestAction<Void> pin();

    @NotNull
    RestAction<Void> unpin();

    @NotNull
    RestAction<Void> delete();

    @NotNull
    MessageAction edit(CharSequence content);

    @NotNull
    MessageAction edit(PartialMessage content);

    @NotNull
    MessageAction editFormat(String content, Object... format);

    @NotNull
    MessageAction editEmbeds(MessageEmbed... embeds);

    @NotNull
    MessageAction editEmbeds(Collection<MessageEmbed> embeds);

    @NotNull
    MessageAction editComponents(ComponentLayout... components);

    @NotNull
    MessageAction editComponents(Collection<ComponentLayout> components);

    @NotNull
    User getAuthor();

    @NotNull
    String getJumpUrl();

    @NotNull
    JDA getJDA();

    boolean isFromType(ChannelType type);

    boolean isFromGuild();

    boolean isEdited();

    boolean isPinned();

    boolean isTTS();

    boolean isWebhook();

    @NotNull
    ChannelType getType();

    @NotNull
    OffsetDateTime getTimeEdited();

    @NotNull
    EnumSet<Message.MessageFlag> getFlags();
}
