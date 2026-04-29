package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

public class Preview implements ButtonAction {

    @Override
    public String getName() {
        return "preview";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.reply(((IncidentImpl)ctx.getIncident()).formatReceiving()).setEphemeral(true).queue();
    }
}
