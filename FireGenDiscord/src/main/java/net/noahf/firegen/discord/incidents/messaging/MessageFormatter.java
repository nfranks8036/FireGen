package net.noahf.firegen.discord.incidents.messaging;

public interface MessageFormatter {

    void formatInitial(MessageContext ctx);

    void formatEdited(MessageContext ctx);

}
