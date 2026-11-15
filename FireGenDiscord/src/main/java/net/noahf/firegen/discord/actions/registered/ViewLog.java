package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.listeners.ContextMenuDetector;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.FireGenUser;

public class ViewLog implements ButtonAction {

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser user, ButtonInteractionEvent event) {
        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        ContextMenuDetector.createIncidentLog(event, incident, ctx.getConfig().getFireGenVariables(), true);
    }
}
