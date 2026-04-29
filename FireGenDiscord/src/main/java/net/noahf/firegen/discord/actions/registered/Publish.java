package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

public class Publish implements ButtonAction {

    @Override
    public String getName() {
        return "publish";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        IncidentImpl incident = ((IncidentImpl) ctx.getIncident());
        if (!incident.isPublished() && !this.checkUserPermission(event.getUser(), Permission.INCIDENT_PUBLISH)) {
            // will be published
            DiscordMessages.error(event, "You don't have permission to publish an incident.");
            return;
        } else if (incident.isPublished() && !this.checkUserPermission(event.getUser(), Permission.INCIDENT_UNPUBLISH)){
            // will be deleted (unpublished)
            DiscordMessages.error(event, "You don't have permission to publish an incident.");
            return;
        }

        incident.togglePublished();

        Contributor<User> contributor = incident.addContributor(event.getUser());
        incident.addLog(contributor, IncidentLogEntry.EntryType.UPDATE,
                "INCIDENT " + (incident.isPublished() ? "PUBLISHED TO" : "UNPUBLISHED FROM") + " RECEIVERS"
        );

        incident.update();

        DiscordMessages.noMessage(event);
    }
}
