package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;
import org.jetbrains.annotations.Nullable;

import static net.noahf.firegen.discord.utilities.MessageStatus.CONTENT;

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
    public void execute(ActionsContext ctx, FireGenUser user, ButtonInteractionEvent event) {
        event.deferEdit().queue();

        if (!this.checkUserPermission(event.getUser(), Permission.INCIDENT_STATUS)) {
            DiscordMessages.error(event, "You don't have permission to change an incident's status.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        this.onSubmit(incident, user, null);
    }

    public ServiceResponse<?> onSubmit(IncidentImpl incident, FireGenUser user, @Nullable IncidentStatus parameterStatus) {
        IncidentStatus newStatus = parameterStatus;
        IncidentStatus oldStatus = incident.getStatus();
        if (newStatus == null) {
            newStatus = oldStatus.opposite();
        }

        if (newStatus.equals(incident.getStatus())) {
            return ServiceResponse.fail(user, "This incident is already set to the " + newStatus.name() + " status.");
        }

        incident.setStatus(newStatus);

        if (parameterStatus == null) incident.refreshStatus();

        incident.addContributor(user);
        String narrative = switch (newStatus) {
            case PENDING, ACTIVE -> "Incident Re-opened";
            case CLOSED -> "Incident Closed";
            default -> "Incident status changed to " + newStatus.name();
        };
        if (parameterStatus != null) {
            narrative = "Changed Incident Status: " + oldStatus.name() + " --> " + newStatus.name();
        }
        incident.addLog(user, IncidentLogEntryImpl.EntryType.UPDATE, narrative);

        incident.getMessagingService().sendAll();
        incident.getMessagingService().notifyPublishChange();

        return ServiceResponse.successNone();
    }

}
