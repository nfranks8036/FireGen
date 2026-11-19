package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;
import org.jetbrains.annotations.Nullable;

public class Publish implements ButtonAction {

    @Override
    public String getName() {
        return "publish";
    }

    @Override
    public void execute(ActionsContext ctx, FireGenUser<?> user, ButtonInteractionEvent event) {
        event.deferEdit().queue();

        IncidentImpl incident = ((IncidentImpl) ctx.getIncident());
        MessageStatus status = this.ensureIncidentOpen(event, incident);
        if (status == MessageStatus.CONTENT) {
            return;
        }

        if (!incident.isPublished() && !this.checkUserPermission(event.getUser(), Permission.INCIDENT_PUBLISH)) {
            // will be published
            DiscordMessages.error(event, "You don't have permission to publish an incident.");
            return;
        }

        this.onSubmit(incident, user, null);
    }

    public ServiceResponse<?> onSubmit(Incident incident, FireGenUser user, @Nullable IncidentPublishedStatus option) {
        IncidentPublishedStatus finalOption = option != null ? option : incident.getPublished().opposite();
        incident.setPublished(finalOption);

        incident.addContributor(user);
        String name = incident.getPublished().name();
        incident.addLog(user, IncidentLogEntry.EntryType.UPDATE,
                "Incident " + name.charAt(0) + name.substring(1).toLowerCase()
        );

        incident.update();
        ((IncidentImpl)incident).getMessagingService().notifyPublishChange();

        return ServiceResponse.successNone();
    }
}
