package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;

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

        IncidentStatus newStatus = incident.getStatus().opposite();
        incident.setStatus(newStatus);
        incident.refreshStatus();

        Contributor<User> user = incident.addContributor(event.getUser());
        String narrative = switch (newStatus) {
            case PENDING, ACTIVE -> "Incident re-opened";
            case CLOSED -> "Incident closed";
            default -> "Incident status changed to " + newStatus.name();
        };
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);

        DiscordMessages.noMessage(event, MessageStatus.NONE);

        incident.update();
    }

}
