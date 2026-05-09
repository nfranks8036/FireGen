package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;

public class ReceiveMessageFormatter implements MessageFormatter {
    @Override
    public void formatInitial(MessageContext ctx) {
        if (!ctx.getService().isPublished()) {
            return;
        }

        if (!ctx.getService().getReceivingMessages().isEmpty()) {
            return;
        }

        String startingMessage = "New Call- " + ctx.getIncident().getType().getSelectedName();

        String formattedLocation = ctx.getIncident().getLocation().format();
        if (ctx.getIncident().getLocation().isSet() && !formattedLocation.isBlank()) {
            startingMessage = startingMessage + "\nWhere- " + formattedLocation;
        }

        if (!ctx.getIncident().getAttachedAgencies().isEmpty()) {
            startingMessage = startingMessage + "\n" +
                    "Who- " + String.join(", ",
                        ctx.getIncident().getAttachedAgencies().stream().map(Agency::getShorthand).toList()
                    );
        }

        startingMessage = startingMessage + "\nWhen- <t:" + ctx.getIncident().getTime().getUnix() + ":t>";

        // send a starting message to the subscribed channels, this will be quickly changed by the following edit
        for (TextChannel channel : Main.receiveChannels) {
            try {
                if (channel == null) {
                    Log.warn("RECEIVE - Can't send a message here. This channel does not exist!"); continue;
                }

                Message message = channel.sendMessage(startingMessage).complete();
                ctx.getService().getReceivingMessages().add(message);
            } catch (Exception exception) {
                Log.error("RECEIVE - Can't send message to " + (channel != null ? channel.getName() : null) +
                        ": " + exception, exception);
            }
        }
    }

    @Override
    public void formatEdited(MessageContext ctx) {

    }
}
