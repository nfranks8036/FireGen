package net.noahf.firegen.discord.bot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MessageGenericData {

    public static MessageGenericData fromMessage(String message) {
        return new MessageGenericData(message, null);
    }

    public static MessageGenericData fromEmbed(MessageEmbed embed) {
        return new MessageGenericData(null, embed);
    }

    private final String content;
    private final MessageEmbed embed;

    public MessageCreateData asCreate() {
        if (content != null) {
            return MessageCreateData.fromContent(content);
        }
        if (embed != null) {
            return MessageCreateData.fromEmbeds(embed);
        }
        throw new IllegalStateException("No values set.");
    }

    public MessageEditData asEdit() {
        if (content != null) {
            return MessageEditData.fromContent(content);
        }
        if (embed != null) {
            return MessageEditData.fromEmbeds(embed);
        }
        throw new IllegalStateException("No values set.");
    }

    public UnitsResponseType guessType() {
        if (this.content != null) {
            return UnitsResponseType.TABLE;
        }
        if (this.embed != null) {
            return UnitsResponseType.EMBED;
        }
        return UnitsResponseType.NOT_SET;
    }

}
