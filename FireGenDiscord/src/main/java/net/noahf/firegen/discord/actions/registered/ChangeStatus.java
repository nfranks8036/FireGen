package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.StatusAttribute;
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
     * buttons.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!this.checkUserPermission(event.getUser(), Permission.INCIDENT_CLOSE, Permission.INCIDENT_REOPEN)) {
            DiscordMessages.error(event, "You don't have permission to change an incident's status.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        StatusAttribute searchFor;
        if (incident.getStatus().getAttributes().isInProgress()) {
            searchFor = StatusAttribute.CLOSED;
        } else {
            searchFor = incident.getAgencies().isEmpty() ? StatusAttribute.DEFAULT : StatusAttribute.ACTIVE;
        }

        IncidentStatus newStatus = ctx.getManager().getStatusesWithAttributes(searchFor).getFirst();
        incident.setStatus(newStatus);

        Contributor<User> user = incident.addContributor(event.getUser());
        String narrative = switch (searchFor) {
            case DEFAULT, ACTIVE -> "Incident re-opened";
            case CLOSED -> "Incident closed";
            default -> "Incident status changed (unknown)";
        };
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);

        DiscordMessages.noMessage(event);

        incident.update();
    }

}
