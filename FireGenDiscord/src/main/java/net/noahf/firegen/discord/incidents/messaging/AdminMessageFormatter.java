package net.noahf.firegen.discord.incidents.messaging;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.utilities.Log;

public class AdminMessageFormatter implements MessageFormatter {

    @Override
    public void formatInitial(MessageContext ctx) {
        if (!ctx.getService().getAdminMessages().isEmpty()) {
            return;
        }

        // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
        // the content will remain
        String contents = "New incident " +
                ctx.getIncident().getType().getSelectedName() + " created by " +
                ctx.getIncident().getContributors().getFirst();
        for (TextChannel channel : Main.adminChannels) {
            try {
                if (channel == null) {
                    Log.warn("ADMIN - Can't send a message here. This channel does not exist!"); continue;
                }

                Message message = channel.sendMessage(contents)
                        .setComponents(ctx.getService().getAdminComponents())
                        .complete();

                message.createThreadChannel("Incident " +
                        ctx.getIncident().getFormattedId() + " Discussion").complete();

                ctx.getService().getAdminMessages().add(message);
            } catch (Exception exception) {
                Log.error("ADMIN - Can't send message to " + (channel != null ? channel.getName() : null)
                        + ": " + exception, exception);
            }
        }
    }

    @Override
    public void formatEdited(MessageContext ctx) {
        return;
    }

}
