package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.ImmutablePair;

import java.util.List;

public class Preview implements ButtonAction {

    @Override
    public String getName() {
        return "preview";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        ImmutablePair<String, List<MessageEmbed>> response =
                ((IncidentImpl)ctx.getIncident()).getMessagingService().get(ReceiveMessageSender.class).getReceivingFormat();
        event.reply(response.getFirstElement()).setEmbeds(response.getSecondElement()).setEphemeral(true).queue();
    }
}
