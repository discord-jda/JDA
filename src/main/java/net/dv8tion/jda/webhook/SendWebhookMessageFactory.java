package net.dv8tion.jda.webhook;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* package-private */ class SendWebhookMessageFactory
{

    SendWebhookMessage create(JSONObject jsonObject)
    {
        Checks.notNull(jsonObject, "JSONObject");

        final long id = jsonObject.getLong("id");
        final int type = jsonObject.getInt("type");
        final long channelId = jsonObject.getLong("channel_id");
        final long webhookId = jsonObject.getLong("webhook_id");
        final String content = jsonObject.optString("content");
        final String nonce = jsonObject.optString("nonce", null);

        WebhookUser author = createWebhookUser(jsonObject.getJSONObject("author"));

        final boolean isTTS = Helpers.optBoolean(jsonObject, "tts");
        final boolean mentionEveryone = Helpers.optBoolean(jsonObject, "mention_everyone");
        final boolean pinned = Helpers.optBoolean(jsonObject, "pinned");

        final List<MessageEmbed> embeds = EntityBuilder.map(jsonObject, "embeds", this::createMessageEmbed);
        final List<Message.Attachment> attachments = EntityBuilder.map(jsonObject, "attachments", this::createMessageAttachment);
        final List<User> mentionedUsers = EntityBuilder.map(jsonObject, "mentions", this::createWebhookUser);

        Set<Long> mentionedRoles = new HashSet<>();

        jsonObject.getJSONArray("mention_roles").forEach(
            (roleId) -> mentionedRoles.add(Long.parseUnsignedLong(roleId.toString()))
        );

        return new SendWebhookMessage(id, channelId, webhookId, MessageType.fromId(type), mentionEveryone,
            mentionedUsers, mentionedRoles, isTTS, pinned, content, nonce, author, attachments, embeds);

    }

    private MessageEmbed createMessageEmbed(JSONObject json)
    {
        EmbedBuilder builder = new EmbedBuilder();

        if (!json.isNull("title")) {
            builder.setTitle(json.getString("title"), json.optString("url", null));
        }

        if (!json.isNull("description")) {
            builder.setDescription(json.getString("description"));
        }

        if (!json.isNull("timestamp")) {
            builder.setTimestamp(OffsetDateTime.parse(json.getString("timestamp")));
        }

        if (!json.isNull("color")) {
            builder.setColor(json.getInt("color"));
        }

        if (!json.isNull("footer")) {
            JSONObject footer = json.getJSONObject("footer");
            builder.setFooter(footer.getString("text"), footer.optString("icon_url", null));
        }

        if (!json.isNull("image")) {
            JSONObject image = json.getJSONObject("image");
            builder.setImage(image.optString("url", null));
        }

        if (!json.isNull("thumbnail")) {
            JSONObject thumbnail = json.getJSONObject("thumbnail");
            builder.setThumbnail(thumbnail.optString("url", null));
        }

        if (!json.isNull("author")) {
            JSONObject author = json.getJSONObject("thumbnail");
            builder.setAuthor(author.optString("name", null),
                author.optString("url", null), author.optString("icon_url", null));
        }

        EntityBuilder.map(json, "fields", (field) -> {

            builder.addField(
                field.getString("name"),
                field.getString("value"),
                field.optBoolean("inline", false)
            );

            return null;
        });

        return builder.build();
    }

    private Message.Attachment createMessageAttachment(JSONObject jsonObject)
    {
        return new SendWebhookMessage.Attachment(jsonObject.getLong("id"), jsonObject.getString("url"),
            jsonObject.getString("proxy_url"), jsonObject.getString("filename"),
            jsonObject.getInt("size"), jsonObject.optInt("height"), jsonObject.optInt("width"));
    }

    private WebhookUser createWebhookUser(JSONObject jsonObject)
    {
        final long authorId = jsonObject.getLong("id");
        final String username = jsonObject.getString("username");
        final short discriminator = Short.parseShort(jsonObject.getString("discriminator"));
        final String avatarId = jsonObject.getString("avatar");
        final boolean isBot = jsonObject.getBoolean("bot");
        return new WebhookUser(authorId, discriminator, username, avatarId, isBot);
    }
}
