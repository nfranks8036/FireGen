package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.listeners.ContextMenuDetector;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

public class ViewFields implements ButtonAction {

    @Override
    public String getName() {
        return "fields";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.replyEmbeds(ContextMenuDetector.createFieldsDisplay(
                (IncidentImpl) ctx.getIncident(), Main.config.getFireGenVariables()
        )).setEphemeral(true).queue();
    }

}
