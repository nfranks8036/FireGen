package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;

public class Publish implements ButtonAction {

    @Override
    public String getName() {
        return "publish";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        IncidentImpl incident = ((IncidentImpl) ctx.getIncident());
        this.ensureIncidentOpen(event, incident);

        if (!incident.isPublished() && !this.checkUserPermission(event.getUser(), Permission.INCIDENT_PUBLISH)) {
            // will be published
            DiscordMessages.error(event, "You don't have permission to publish an incident.");
            return;
        } else if (incident.isPublished() && !this.checkUserPermission(event.getUser(), Permission.INCIDENT_UNPUBLISH)){
            // will be deleted (unpublished)
            DiscordMessages.error(event, "You don't have permission to publish an incident.");
            return;
        }

        MessageStatus status = this.onSubmit(incident, event);
        DiscordMessages.noMessage(event, status);
    }

    public MessageStatus onSubmit(IncidentImpl incident, IReplyCallback event) {
        incident.setPublished(incident.getPublished().opposite());

        Contributor<User> contributor = incident.addContributor(event.getUser());
        incident.addLog(contributor, IncidentLogEntry.EntryType.UPDATE,
                "INCIDENT " + incident.getPublished().name()
        );

        incident.update();

        return MessageStatus.NONE;
    }
}
