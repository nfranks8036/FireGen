package net.noahf.firegen.discord.incidents.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.util.ArrayList;
import java.util.List;

public class IncidentMessagingService {

    private final IncidentImpl incident;

    private @Getter boolean published;

    private @Getter(value = AccessLevel.PACKAGE) List<Message> receivingMessages, adminMessages;

    private @Getter(value = AccessLevel.PACKAGE) List<MessageTopLevelComponent> adminComponents;

    public IncidentMessagingService(IncidentImpl incident) {
        this.incident = incident;

        this.adminComponents = new ArrayList<>(List.of(
                // components following are the button row that are used in the admin channel
                // id should be in the format of 'firegen-<incident ID>-<command>-<additional info>'
                ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.danger(this.incident.createInteractionIdString("status"), "Close Incident"),
                        Button.danger(this.incident.createInteractionIdString("publish"), "Publish")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident1", "Edit:").asDisabled(),
                        Button.primary(this.incident.createInteractionIdString("editmode"), "Edit Mode"),
                        Button.primary(this.incident.createInteractionIdString("datetime"), "Date/Time")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident2", "Edit:").asDisabled(),
                        Button.primary(this.incident.createInteractionIdString("location"), "Location"),
                        Button.primary(this.incident.createInteractionIdString("agencies"), "Agencies")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-misc", "Misc:").asDisabled(),
                        Button.primary(this.incident.createInteractionIdString("preview"), "Preview")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-narrative", "Narrative:").asDisabled(),
                        Button.success(this.incident.createInteractionIdString("addnarrative"), "Add"),
                        Button.danger(this.incident.createInteractionIdString("hidenarrative"), "Hide")
                )
        ));
    }

    public void togglePublished() {
        final int PUBLISH_INDEX = 0;

        this.published = !this.published;
        String text;
        if (this.published) {
            text = "Unpublish";
        } else {
            text = "Publish";
            for (Message message : this.receivingMessages) {
                message.delete().complete();
            }
            this.receivingMessages.clear();
        }
        this.adminComponents.set(PUBLISH_INDEX,                 ActionRow.of(
                Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                Button.danger(this.incident.createInteractionIdString("status"), "Close Incident"),
                Button.danger(this.incident.createInteractionIdString("publish"), text)
        ));
    }

}
