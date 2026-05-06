package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the "Edit Mode" button in the "Misc" row
 */
public class EditMode implements ButtonAction {

    /**
     * Represents the list of users that are currently editing an incident and which incident they're currently editing.
     */
    public static final Map<User, Incident> editIncidents = new ConcurrentHashMap<>();

    /**
     * The command name required to access this class.
     */
    @Override
    public String getName() {
        return "editmode";
    }

    /**
     * The event that occurs after pressing the 'Edit Mode' button in the 'Misc' row. This is not only informational but does
     * set a value so the user can edit this incident. It creates a gateway to the `/set-details` command.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        this.ensureIncidentOpen(event, ctx.getIncident());

        Incident incident = ctx.getIncident();

        editIncidents.put(event.getUser(), incident);
        DiscordMessages.selfDestruct(event, 5,
                "You're now editing the incident " +
                        incident.getFormattedId() + " (" + incident.getType().getSelectedName() + "). "
                + "Type `/set-details` to change the incident details in command format."
        );
    }
}
