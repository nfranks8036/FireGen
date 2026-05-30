package net.noahf.firegen.discord.incidents.messaging;

public interface MessageSender {

    void sendInitial(MessageContext ctx);

    void sendEdited(MessageContext ctx);

}
