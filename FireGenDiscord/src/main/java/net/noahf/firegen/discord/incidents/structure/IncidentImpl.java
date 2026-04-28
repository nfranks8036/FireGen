package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.*;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

@RequiredArgsConstructor
@Entity @Table(name = "incident")
public class IncidentImpl implements net.noahf.firegen.api.incidents.Incident {

    private final transient IncidentManager manager;

    private
    @Getter
    @Id @Column(name="id")
    final long id;

    private @Getter @Setter IncidentStatus status;

    private transient @Getter @Setter @NotNull IncidentType type;
    private transient @Getter @NotNull List<Agency> agencies;
    private transient @Getter @Setter @NotNull IncidentLocation location;
    private transient @Getter @NotNull IncidentTime time;

    private transient @Getter List<IncidentLogEntry> log;
    private transient @Getter List<Contributor> contributors;

    private transient List<Message> receivingMessages, adminMessages;

    private transient List<MessageTopLevelComponent> adminComponents;

    public IncidentImpl() {
        this.manager = null;
        this.id = Integer.MIN_VALUE;
    }

    public IncidentImpl(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = IncidentStatus.PENDING;
        this.type = manager.getFireGenVariables().defaultType();
        this.location = new IncidentLocationImpl(new ArrayList<>());
        this.time = new IncidentTimeImpl(LocalDateTime.now());

        this.agencies = new ArrayList<>();
        this.log = new ArrayList<>();
        this.receivingMessages = new ArrayList<>();
        this.adminMessages = new ArrayList<>();
        this.contributors = new ArrayList<>();

        this.adminComponents = new ArrayList<>(List.of(
                // components following are the button row that are used in the admin channel
                // id should be in the format of 'firegen-<incident ID>-<command>-<additional info>'
                ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.danger(this.createInteractionIdString("status"), "Close Incident")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident1", "Edit:").asDisabled(),
                        Button.primary(this.createInteractionIdString("incidenttype"), "Type"),
                        Button.primary(this.createInteractionIdString("datetime"), "Date/Time")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-incident2", "Edit:").asDisabled(),
                        Button.primary(this.createInteractionIdString("location"), "Location"),
                        Button.primary(this.createInteractionIdString("agencies"), "Agencies")
                ),
                ActionRow.of(
                        Button.secondary("firegen-disabled-narrative", "Narrative:").asDisabled(),
                        Button.success(this.createInteractionIdString("addnarrative"), "Add"),
                        Button.danger(this.createInteractionIdString("hidenarrative"), "Hide")
                )
        ));
    }

    public void setTypeBySearch(String type) {
        IncidentType newType = manager.getTypeFromString(type);
        if (type.startsWith("custom:")) {
            type = type.substring("custom:".length()).toUpperCase();
            newType = new IncidentTypeImpl(type, IncidentTypeTagImpl.DEFAULT, 0);
        }

        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.setType(newType);
    }

    @Override
    public void addContributor(Contributor contributor) {
        if (this.contributors.contains(contributor)) {
            return;
        }
        this.contributors.add(contributor);
    }

    public Contributor<User> addContributor(User user) {
        Contributor<User> contributor = ContributorImpl.of(user);
        this.addContributor(contributor);
        return contributor;
    }

    @Override
    public void addLog(Contributor user, IncidentLogEntry.EntryType type, String log) {
        this.addLog(new IncidentLogEntryImpl(LocalDateTime.now(), user, log, type));
    }

    @Override
    public void addLog(IncidentLogEntry entry) {
        this.log.add(entry);
    }

    @Override
    public void injectLog(IncidentLogEntry entry) {
        for (int i = 0; i < this.log.size(); i++) {
            IncidentLogEntry element = this.log.get(i);
            if (element.getId() != entry.getId()) {
                continue;
            }

            this.log.set(i, entry);
            return;
        }
        throw new IllegalStateException("Narrative with ID '" + entry.getId() + "' does not exist in the incident with ID '" + this.getFormattedId() + "'");
    }

    @Override
    public List<Agency> getAttachedAgencies() {
        return List.of();
    }

    @Override
    public List<Unit> getAttachedUnits() {
        return List.of();
    }

    public void setAgencies(List<Agency> agencies) {
        if (agencies.isEmpty()) {
            this.status =  IncidentStatus.PENDING;
        } else {
            this.status =  IncidentStatus.ACTIVE;
        }
        this.agencies = agencies;
    }

    public String createInteractionIdString(String... commands) {
        // name of the command has to come first or this will not work
        return String.format(
                "firegen-%s-%s",
                this.getId(), String.join("-", commands)
        );
    }

    public @NotNull List<String> formatNarrative(boolean admin) {
        if (this.log == null || this.log.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> response = new ArrayList<>();
        for (IncidentLogEntry entry : this.log) {
            if (!admin && entry.getType() != IncidentLogEntryImpl.EntryType.NARRATIVE) {
                // we don't want admin update logs to be included in the narrative for the public necessarily
                continue;
            }
            IncidentLogEntryImpl entryImpl = (IncidentLogEntryImpl) entry;
            response.add(admin ? entryImpl.formatAdmin() : entryImpl.formatReceiver());
        }
        return response;
    }

    public List<IncidentLogEntry> getNarrative() {
        return this.getLog().stream().filter(IncidentLogEntry::isNarrative).toList();
    }


    public String getFormattedId() {
        return this.time.getDateTime().format(DateTimeFormatter.ofPattern("yyyy")) + "-" +
                this.getId();
    }

    /**
     * Post the incident changes to the saved messages. Used for updating subscribed servers with new information. <br>
     * <b>This method will block the main thread IF the incident has never been posted before.</b> <br>
     * This is required to ensure the order of the initial message and then edit message when the incident is created.
     */
    @Override
    public void update() {
        if (this.receivingMessages.isEmpty()) {
            // if condition is met:
            // this incident has never been posted in any channel yet, so it's likely a new one.

            String startingMessage = "New Call- " + this.type.getSelectedName();
            if (this.location.isSet() && !this.location.format().isBlank()) {
                startingMessage = startingMessage + "\nWhere- " + this.location.format();
            }
            if (!this.agencies.isEmpty()) {
                startingMessage = startingMessage + "\nWho- " + String.join(", ", this.agencies.stream().map(Agency::getShorthand).toList());
            }
            startingMessage = startingMessage + "\nWhen- <t:" + this.getTime().getUnix() + ":t>";

            // send a starting message to the subscribed channels, this will be quickly changed by the following edit
            for (TextChannel channel : Main.receiveChannels) {
                Log.info("Sending starting message in #" + channel.getName() + " in " + channel.getGuild().getName() + "...");
                this.receivingMessages.add(channel.sendMessage(startingMessage).complete());
            }

            // send a starting message to the admin channels, this will be quickly changed by the following edit THOUGH
            // the content will remain
            for (TextChannel channel : Main.adminChannels) {
                this.adminMessages
                        .add(channel.sendMessage("New incident " + this.type.getSelectedName() + " created by " + this.contributors.getFirst())
                        .setComponents(this.adminComponents)
                        .complete());
            }
        }

        // edit the messages with the updated detailed content
        String fullMessage = this.formatReceiving();
        for (Message message : this.receivingMessages) {
            Log.info("Updating incident " + this.getFormattedId() + " (" + this.getType().getSelectedName()
                    + ") message in #" + message.getChannel().getName() + " in " + message.getGuild().getName() + "...");
            message.editMessage(fullMessage).queue(null, (t) -> {
                Log.warn("Message does not exist. Removing from the list (possibly deleted by staff).", t);
                this.receivingMessages.remove(message);
            });
        }

        // edit the admin messages with an updated admin panel
        MessageEmbed adminMsg = this.formatAdmin();
        for (Message message : this.adminMessages) {

            // add or remove the components if the status requires it
            if (this.status.isInProgress() && message.getComponents().size() <= this.adminComponents.size()) {
                message.editMessageComponents(this.adminComponents).queue();
            } else if (!this.status.isInProgress() && message.getComponents().size() > 1) {
                message.editMessageComponents(ActionRow.of(
                        Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                        Button.success("firegen-" + this.getId() + "-status", "Re-open Incident")
                )).queue();
            }

            message.editMessageEmbeds(adminMsg).queue(null, (t) -> {
                Log.warn("Admin message in #" + message.getChannel().getName() + " in " +
                        message.getGuild().getName() + " does not exist. Removing from the list " +
                        "(possibly deleted by staff).", t);
                this.adminMessages.remove(message);
            });
        }
    }

    public String formatReceiving() {
        List<String> narrative = this.formatNarrative(false);
        return String.format(
                """
                        # %s %s
                        [`%s` @ `%s` // <t:%d:R>]
                        
                        **Responding:** %s
                        **%s:** %s""" +
                        (!narrative.isEmpty() ? "\n\n**Narrative:**\n%s" : ""),
                this.manager.getStatusEmoji(this),
                this.type.getSelectedName(),
                this.getTime().formatDate(this.manager.getFireGenVariables()),
                this.getTime().formatTimeShort(this.manager.getFireGenVariables()),
                this.getTime().getUnix(),
                String.join(", ", this.agencies.stream().map(Agency::getFormatted).toList()),
                this.location.getType().getPrefix(),
                this.location.format(),
                !narrative.isEmpty() ? String.join("\n", narrative) : "None"
        );
    }

    public MessageEmbed formatAdmin() {
        List<String> narrative = this.formatNarrative(true);
        return new EmbedBuilder()
                        .setTitle("ADMIN OVERVIEW")
                        .setDescription("Incident `" + this.getFormattedId() + "`"
                                + "\nStatus: " + this.manager.getStatusEmoji(this) + " " + this.status
                                + "\nMessages (" + this.receivingMessages.size() + "): " + String.join(" , ", this.receivingMessages.stream().map(msg ->
                                    "https://discord.com/channels/" + msg.getGuild().getId() + "/" + msg.getChannel().getId() + "/" + msg.getId()
                                ).toList())
                        )
                        .setFooter("Contributors: " + String.join(", ", this.getContributors()))
                        .addField("Call Type",
                                this.type + "\n\n" +
                                        "(base `" + this.type.getType() + "`, " +
                                        "tag `" + this.type.getTag().getTagName() + "`, " +
                                        "qualifier `" + (this.type.getTag().getQualifier() != null ? this.type.getTag().getQualifier().getQualifiers().get(this.type.getQualifierChoice()) : "None") + "`)",
                                true
                        )
                        .addField("Date/Time",
                                "Date: <t:" + this.time.getUnix() + ":d>\n" +
                                        "Time: <t:" + this.time.getUnix() + ":T>\n" +
                                        "Relative: <t:" + this.time.getUnix() + ":R>\n" +
                                        "Unix: `" + this.time.getUnix() + "`", true
                        )
                        .addField("Location",
                                "Type: `" + this.location.getType().name() + "`\n" +
                                        "Data: " + String.join(", ", this.location.getData().stream().map(s -> "`" + s + "`").toList()) + "\n" +
                                        "Common Name: `" + (this.location.getCommonName() != null ? this.location.getCommonName() : " ") + "`\n" +
                                        "Venue: `" + (this.location.getVenue() != null ? this.location.getVenue() : " ") + "`\n" +
                                        "Formatted: `" + this.location + "`",
                                false
                        )
                        .addField("Responding Agencies (" + this.agencies.size() + ")", this.getRespondingAgenciesJoinedString(), false)
                        .addField("Log",
                                !narrative.isEmpty() ? String.join("\n", narrative) : "None",
                                false
                        )
                        .setColor(new Color(255, 94, 94))
                .build();
    }

    private @NotNull String getRespondingAgenciesJoinedString() {
        StringJoiner respondingAgenciesJoiner = new StringJoiner("\n");
        int index = 0;
        for (Agency agency : this.agencies) {
            respondingAgenciesJoiner.add("- **" + agency.getLonghand().toUpperCase() + "**");
            respondingAgenciesJoiner.add((index == 0 ? "  " : "") + "  - " +
                    "(shorthand `" + agency.getShorthand() + "`, formatted `" + agency.getFormatted() + "`, emoji " + ((AgencyImpl)agency).getEmoji() + ")"
                    );
            index++;
        }
        return respondingAgenciesJoiner.toString().isBlank() ? "None"
                :  respondingAgenciesJoiner.toString().substring(
                0, Math.min(1024, respondingAgenciesJoiner.toString().length())
        );
    }


    public void admin_wipeMessages() {
        for (Message message : this.receivingMessages) {
            message.delete().queue(null, (t) -> Log.warn("Couldn't delete in #" + message.getChannel().getName()));
        }

        for (Message message : this.adminMessages) {
            message.delete().queue(null, (t) -> Log.warn("Couldn't delete in #" + message.getChannel().getName()));
        }

        this.receivingMessages.clear();
        this.adminMessages.clear();
    }

}