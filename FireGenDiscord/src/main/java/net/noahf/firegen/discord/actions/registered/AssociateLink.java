package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.ModalAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.MessageStatus;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the "Associate Link" button next to the "Narrative:" row
 */
public class AssociateLink implements StringDropdownAction, ButtonAction, ModalAction {

    /**
     * The name of the command
     */
    @Override
    public String getName() {
        return "link";
    }

    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        this.ensureIncidentOpen(event, ctx.getIncident());

        if (!this.checkUserPermission(event.getUser(), Permission.ASSOCIATE_LINKS)) {
            DiscordMessages.error(event, "You don't have permission to associate links to this incident.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        List<SelectOption> options = new ArrayList<>();
        List<String> linkTexts = new ArrayList<>(incident.getLinks().values());

        for (int i = 0; i < linkTexts.size(); i++) {
            String label = DiscordMessages.truncate(linkTexts.get(i), 100, "...");
            options.add(SelectOption.of(label, "associated-" + i));
        }

        if (options.isEmpty()) {
            this.showModal(ctx, event, null);
            return;
        }

        event.reply("Choose a link to edit OR create a new link.")
                .setEphemeral(true)
                .setComponents(ActionRow.of(
                        StringSelectMenu.create(this.callbackId(ctx))
                                .addOption("> CREATE NEW LINK <", "create", Emoji.fromUnicode("➕"))
                                .addOptions(options)
                                .setMaxValues(1)
                                .build()
                ))
                .complete();
    }

    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        this.ensureIncidentOpen(event, ctx.getIncident());

        String selected = event.getInteraction().getSelectedOptions().getFirst().getValue();
        Integer index = null;
        if (!selected.contains("create")) {
            try {
                index = Integer.parseInt(selected.substring("associated-".length()));
            } catch (NumberFormatException num) {
                DiscordMessages.error(event, "Attempted to process ID '" + selected + "' which does not" +
                        " contain the value number syntax expected. 'associated-X'", num);
                return;
            }
        }

        this.showModal(ctx, event, index);
    }

    private void showModal(ActionsContext ctx, IModalCallback event, @Nullable Integer index) {
        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        String linkInput = index != null ? new ArrayList<>(incident.getLinks().keySet()).get((int)index).toString() : null;
        String titleInput = index != null ? new ArrayList<>(incident.getLinks().values()).get(index) : null;

        Modal modal = Modal.create(this.callbackId(ctx, index != null ? String.valueOf(index) : "create"),
                        "Links with " + ctx.getIncident().getFormattedId())
                .addComponents(
                        Label.of(
                                "Link",
                                "The link, preferably including the protocol (e.g., 'https')",
                                TextInput.create("link", TextInputStyle.SHORT)
                                        .setRequiredRange(5, 400)
                                        .setRequired(true)
                                        .setPlaceholder("Ex: https://openmhz.com/system/nrv911?filter-type=talkgroup&filter-code=5")
                                        .setValue(linkInput)
                                        .build()
                        ),
                        Label.of(
                                "Text",
                                "The link title that will be shown as clickable text",
                                TextInput.create("text", TextInputStyle.SHORT)
                                        .setRequiredRange(2, 400)
                                        .setRequired(true)
                                        .setPlaceholder("Ex: View Radio Traffic")
                                        .setValue(titleInput)
                                        .build()
                        )
                )
                .build();

        event.replyModal(modal).queue();
    }

    /**
     * This event occurs when the user presses the complete button on the modal.
     */
    @Override
    public void execute(ActionsContext ctx, ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!this.checkUserPermission(event.getUser(), Permission.ASSOCIATE_LINKS)) {
            DiscordMessages.error(event, "You don't have permission to associate links to this incident.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        this.ensureIncidentOpen(event, incident);

        ModalMapping linkMapping = event.getValue("link");
        ModalMapping textMapping = event.getValue("text");
        if (linkMapping == null || textMapping == null) return;

        MessageStatus status = this.onSubmit(incident, event, linkMapping.getAsString(), textMapping.getAsString());
        DiscordMessages.noMessage(event, status);
    }

    public MessageStatus onSubmit(Incident incidentI, IReplyCallback event, String link, String title) {
        IncidentImpl incident = (IncidentImpl) incidentI;
        Contributor<User> user = incident.addContributor(event.getUser());

        URL url;
        try {
            url = parseUrl(link);
        } catch (Exception exception) {
            DiscordMessages.error(event, "An error occurred parsing your URL: " + link, exception);
            return MessageStatus.CONTENT;
        }

        if (title.length() > 300) {
            DiscordMessages.error(event, "Your link title is too long! " + title.length() + " > 300");
            return MessageStatus.CONTENT;
        }

        incident.addLink(link, title);
        incident.addLog(user, IncidentLogEntry.EntryType.UPDATE, "Associated Link '" + url.getHost() + "'");
        incident.update();

        return MessageStatus.NONE;
    }

    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  ModalInteractionEvent e) { this.execute(ctx, e); }
    }

    private URL parseUrl(String input) throws Exception {
        input = input.trim();

        // Add https:// if no scheme is present
        if (!input.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            input = "https://" + input;
        }

        return URI.create(input).normalize().toURL();
    }
}
