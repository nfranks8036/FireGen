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
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentTime;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.StatusAttribute;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.messaging.IncidentMessagingService;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Entity @Table(name = "incident")
public class IncidentImpl implements net.noahf.firegen.api.incidents.Incident {

    private final transient IncidentManager manager;

    private
    @Getter
    @Id @Column(name="id")
    final long id;

    private transient @Getter @Setter IncidentStatus status;

    private transient @Getter @Setter @NotNull IncidentType type;
    private transient @Getter @NotNull Map<Agency, AssignmentStatus> agencies;
    private transient @Getter @NotNull IncidentLocation location;
    private transient @Getter @NotNull IncidentTime time;

    private transient @Getter List<IncidentLogEntry> log;
    private transient @Getter List<Contributor<?>> contributors;

    private transient @Getter IncidentMessagingService messagingService;

    public IncidentImpl() {
        this.manager = null;
        this.id = Integer.MIN_VALUE;
    }

    public IncidentImpl(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = null;
        this.type = manager.getFireGenVariables().defaultType();
        this.location = new IncidentLocationImpl(new ArrayList<>());
        this.time = new IncidentTimeImpl(LocalDateTime.now());

        this.agencies = Collections.synchronizedMap(new LinkedHashMap<>());
        this.log = new ArrayList<>();
        this.contributors = new ArrayList<>();

        this.messagingService = new IncidentMessagingService(this);
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
    public void addContributor(Contributor<?> contributor) {
        if (this.contributors.contains(contributor)) {
            return;
        }
        this.contributors.add(contributor);
    }

    public Contributor<User> addContributor(User user) {
        FireGenUser fireGenUser = Main.users.getByDiscord(user);
        this.addContributor(fireGenUser);
        return fireGenUser;
    }

    @Override
    public void addLog(Contributor<?> user, IncidentLogEntry.EntryType type, String log) {
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
        return new ArrayList<>(this.agencies.keySet());
    }

    @Override
    public List<Unit> getAttachedUnits() {
        return List.of();
    }

    public void removeAgencies(List<Agency> agencies) {
        agencies.forEach(a -> this.agencies.remove(a));
        this.refreshStatus();
    }

    public void putAgencies(List<Agency> agencies) {
        this.putAgencies(agencies.stream()
                .collect(Collectors.toMap((a) -> a, (a) -> AssignmentStatus.HIDE_STATUS)));
    }

    public void putAgencies(Map<Agency, AssignmentStatus> agencies) {
        this.agencies.putAll(agencies);
        this.refreshStatus();
    }

    public Map<Agency, AssignmentStatus> getSortedAgencies() {
        List<Map.Entry<Agency, AssignmentStatus>> sortedEntries = new ArrayList<>(this.getAgencies().entrySet());
        sortedEntries.sort(
                Comparator
                        .comparingInt((Map.Entry<Agency, AssignmentStatus> e)
                                -> e.getValue().ordinal()) // status order
                        .thenComparing(e -> e.getKey().ordinal()) // agency name
        );

        Map<Agency, AssignmentStatus> result = new LinkedHashMap<>();
        for (Map.Entry<Agency, AssignmentStatus> entry : sortedEntries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void refreshStatus() {
        if (agencies.isEmpty()) {
            this.status = this.manager.getStatusesWithAttributes(StatusAttribute.DEFAULT)
                    .getFirst();
        } else {
            this.status = this.manager.getStatusesWithAttributes(StatusAttribute.ACTIVE)
                    .getFirst();
        }
    }

    @Override
    public void setLocation(@Nullable IncidentLocation location) {
        if (location == null) {
            location = new IncidentLocationImpl(new ArrayList<>());
        }
        this.location = location;
    }

    public String createInteractionIdString(String... commands) {
        // name of the command has to come first or this will not work
        return String.format(
                "firegen-%s-%s",
                this.getId(), String.join("-", commands)
        );
    }

    public List<IncidentLogEntry> getNarrative() {
        return this.getLog().stream().filter(IncidentLogEntry::isNarrative).toList();
    }


    public String getFormattedId() {
        return this.time.getDateTime().format(DateTimeFormatter.ofPattern("yyyy")) + "-" +
                this.getId();
    }

    public void togglePublished() {
        this.messagingService.togglePublished();
    }

    /**
     * Post the incident changes to the saved messages. Used for updating subscribed servers with new information. <br>
     * <b>This method will block the main thread IF the incident has never been posted before.</b> <br>
     * This is required to ensure the order of the initial message and then edit message when the incident is created.
     */
    @Override
    public void update() {
        if (this.status == null) {
            return;
        }

        long ELAPSED_TIME_THRESHOLD = 25; // milliseconds
        long start = System.currentTimeMillis();

        if (this.receivingMessages.isEmpty() || this.adminMessages.isEmpty()) {
            // if condition is met:
            // this incident has never been posted in any channel yet, so it's likely a new one.

            this.sendStartMessages();
        }

        // edit the messages with the updated detailed content
        String fullMessage = this.formatReceiving();
        for (Message message : this.receivingMessages) {
            try {
                message.editMessage(fullMessage).queue(null, (t) -> {
                    Log.warn("Message does not exist. Removing from the list (possibly deleted by staff).", t);
                    this.receivingMessages.remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit message: " + exception, exception);
            }
        }

        // edit the admin messages with an updated admin panel
        MessageEmbed[] adminMsg = this.formatAdmin();
        List<MessageTopLevelComponent> buttons = this.adminComponents;
        if (!this.status.getAttributes().isInProgress()) {
            buttons = new ArrayList<>(List.of(ActionRow.of(
                    Button.secondary("firegen-disabled-status", "Status:").asDisabled(),
                    Button.success("firegen-" + this.getId() + "-status", "Re-open Incident")
            )));
        }

        for (Message message : this.adminMessages) {
            try {
                // add or remove the components if the status requires it
                message.editMessageComponents(buttons).queue();

                message.editMessageEmbeds(adminMsg).queue(null, (t) -> {
                    Log.warn("Admin message in #" + message.getChannel().getName() + " in " +
                            message.getGuild().getName() + " does not exist. Removing from the list " +
                            "(possibly deleted by staff).", t);
                    this.adminMessages.remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit admin message: " + exception, exception);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > ELAPSED_TIME_THRESHOLD) {
            Log.info("Took " + elapsed + "ms to update messages.");
        }
    }

    public String formatReceiving() {
        List<String> narrative = this.formatNarrative(false);
        IncidentStatusImpl status = (IncidentStatusImpl) this.status;
        return String.format(
                """
                        # %s %s
                        [`%s` @ `%s` // <t:%d:R>]
                        
                        **Responding:** %s
                        **%s:** %s""" +
                        (!narrative.isEmpty() ? "\n\n**Narrative:**\n%s" : ""),
                status.getEmojisFormattedCombined(),
                this.type.getSelectedName(),
                this.getTime().formatDate(this.manager.getFireGenVariables()),
                this.getTime().formatTimeShort(this.manager.getFireGenVariables()),
                this.getTime().getUnix(),
                this.formatAgencies(),
                this.location.getType().getPrefix(),
                this.location.format(),
                !narrative.isEmpty() ? String.join("\n", narrative) : "None"
        );
    }

    public MessageEmbed[] formatAdmin() {
        List<String> narrative = this.formatNarrative(true);
        IncidentStatusImpl status = (IncidentStatusImpl) this.status;
        MessageEmbed adminOverview = new EmbedBuilder()
                .setTitle("ADMIN OVERVIEW")
                .setDescription("Incident `" + this.getFormattedId() + "`"
                                + "\nStatus: " + status.getEmojisFormattedCombined()
                                + "\nMessages (" + this.receivingMessages.size() + "): " + String.join(" , ", this.receivingMessages.stream().map(msg ->
                                "https://discord.com/channels/" + msg.getGuild().getId() + "/" + msg.getChannel().getId() + "/" + msg.getId()).toList())
                                + "\nContributors (" + this.contributors.size() + "): " + String.join(", ", this.getContributors().stream().map(c -> "<@" + c.getId() + ">").toList())
                )
                .addField("Call Type",
                        this.type + "\n\n" +
                                "Base: `" + this.type.getType() + "`\n" +
                                "Tag: `" + this.type.getTag().getTagName() + "`\n" +
                                "Qualifier: `" + (this.type.getTag().getQualifier() != null ? this.type.getTag().getQualifier().getQualifiers().get(this.type.getQualifierChoice()) : "None") + "`",
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
                        true
                )
                .setColor(new Color(255, 94, 94))
                .build();
        MessageEmbed respondingAgencies = new EmbedBuilder()
                .setTitle("Responding Agencies (" + this.agencies.size() + ")")
                .setDescription(formatAgenciesAdmin())
                .setColor(new Color(255, 94, 94))
                .build();
        MessageEmbed log = new EmbedBuilder()
                .setTitle("Incident Log (" + this.log.size() + ")")
                .setDescription(!narrative.isEmpty() ? String.join("\n", narrative) : "None")
                .setColor(new Color(255, 94, 94))
                .build();
        return new MessageEmbed[]{adminOverview, respondingAgencies, log};
    }

    public @NotNull List<String> getNarrativeFormatted(boolean asAdmin) {
        if (this.log == null || this.log.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> response = new ArrayList<>();
        for (IncidentLogEntry entry : this.log) {
            if (!asAdmin && entry.getType() != IncidentLogEntryImpl.EntryType.NARRATIVE) {
                // we don't want admin update logs to be included in the narrative for the public necessarily
                continue;
            }
            IncidentLogEntryImpl entryImpl = (IncidentLogEntryImpl) entry;
            response.add(asAdmin ? entryImpl.formatAdmin() : entryImpl.formatReceiver());
        }
        return response;
    }

    private String formatAgencies() {
        StringJoiner joiner = new StringJoiner(", ");

        for (Map.Entry<Agency, AssignmentStatus> entry : this.getSortedAgencies().entrySet()) {
            Agency agency = entry.getKey();
            AssignmentStatus status = entry.getValue();

            String returned;
            if (this.getStatus().getAttributes().isInProgress()) {
                returned = ((AgencyImpl) agency).getFormattedStatus(status);
            } else {
                // we are not going to show the statuses when an incident is closed
                // the idea being that all of them are going to be "CLEAR" anyways
                returned = agency.getFormatted();
            }

            joiner.add(returned);
        }

        return joiner.toString();
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