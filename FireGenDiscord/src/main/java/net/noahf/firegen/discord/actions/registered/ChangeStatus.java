package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentStatus;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

/**
 * Represents the "Close Incident" or "Re-open Incident" buttons in the Status row.
 */
public class ChangeStatus implements ButtonAction {

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "status";
    }

    /**
     * The event that occurs after pressing the 'Close Incident' or 'Re-open Incident'
     * buttons. This flip-flops the status of the incident from
     * {@link IncidentStatus#CLOSED CLOSED} to either {@link IncidentStatus#PENDING PENDING} or
     * {@link IncidentStatus#ACTIVE ACTIVE} and vice versa
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.INCIDENT_CLOSE, Permission.INCIDENT_REOPEN)) {
            DiscordMessages.error(event, "You don't have permission to change an incident's status.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        incident.setStatus(incident.getStatus().opposite(incident));
        IncidentStatus newStatus = incident.getStatus();

        Contributor<User> user = incident.addContributor(event.getUser());
        switch (newStatus) {
            case PENDING, ACTIVE -> {
                incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, "Incident re-opened");
            }
            case CLOSED, TIMED_OUT -> {
                incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, "Incident closed");
            }
        }

        DiscordMessages.noMessage(event);

        incident.update();
    }

}
