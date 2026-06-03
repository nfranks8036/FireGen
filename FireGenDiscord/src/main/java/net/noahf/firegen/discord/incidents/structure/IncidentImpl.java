package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.IncidentTime;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.StatusAttribute;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.messaging.IncidentMessagingService;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter @Setter
@Entity @Table(name = "incidents")
public class IncidentImpl implements net.noahf.firegen.api.incidents.Incident {

    private final transient @Getter(value = AccessLevel.NONE) @Setter(value = AccessLevel.NONE)
            IncidentManager manager;

    private
    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    final long id;

    private transient IncidentStatus status;
    private transient @NotNull IncidentType type;
    private transient List<UnitAssignment> units;
    private transient @Getter @NotNull IncidentLocation location;
    private transient @Getter @NotNull IncidentTime time;
    private transient @Getter @NotNull IncidentPublishedStatus published;

    private transient @Getter List<IncidentLogEntry> log;
    private transient @Getter List<Contributor<?>> contributors;

    private transient @Getter IncidentMessagingService messagingService;

    public IncidentImpl() {
        this.manager = null;
        this.id = Long.MIN_VALUE;
    }

    public IncidentImpl(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = null;
        this.type = manager.getFireGenVariables().defaultType();
        this.location = new IncidentLocationImpl(new ArrayList<>());
        this.time = new IncidentTimeImpl(LocalDateTime.now());
        this.published = IncidentPublishedStatus.UNPUBLISHED;

        this.units = new ArrayList<>();
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

    @Override
    public void setPublished(IncidentPublishedStatus newStatus) {
        this.published = newStatus;
        this.getMessagingService().notifyPublishChange();
    }

    public boolean isPublished() {
        return this.getPublished() == IncidentPublishedStatus.PUBLISHED;
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
    public List<Unit> getAttachedUnits() {
        return new ArrayList<>(this.units.stream().map(UnitAssignment::getUnit).toList());
    }

    public void removeUnits(List<Unit> units) {
        units.forEach(a -> this.units.remove(a));
        this.refreshStatus();
    }

    public void putUnits(List<Unit> units) {
        this.putUnits(units.stream()
                .collect(Collectors.toMap((a) -> a, (a) -> AssignmentStatus.HIDE_STATUS)));
    }

    public void putUnits(Map<Unit, AssignmentStatus> units) {
        this.units.putAll(units);
        this.refreshStatus();
    }

    public Map<Unit, AssignmentStatus> getSortedUnits() {
        List<Map.Entry<Unit, AssignmentStatus>> sortedEntries = new ArrayList<>(this.getUnits().entrySet());
        sortedEntries.sort(
                Comparator
                        .comparingInt((Map.Entry<Unit, AssignmentStatus> e)
                                -> e.getValue().ordinal()) // status order
                        .thenComparing(e -> e.getKey().ordinal()) // unit name
        );

        Map<Unit, AssignmentStatus> result = new LinkedHashMap<>();
        for (Map.Entry<Unit, AssignmentStatus> entry : sortedEntries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void refreshStatus() {
        if (units.isEmpty()) {
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

        this.messagingService.sendAll();

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > ELAPSED_TIME_THRESHOLD) {
            Log.info("Took " + elapsed + "ms to update messages.");
        }
    }

}