package net.noahf.firegen.discord.incidents.structure;

import lombok.*;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.IncidentTime;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.utilities.ToStringListStringSelector;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.files.ConfigAssignmentStatuses;
import net.noahf.firegen.discord.config.files.ConfigIncidentTypes;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.messaging.IncidentMessagingService;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitAssignmentImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.SystemUser;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = {"id"})
public class IncidentImpl implements net.noahf.firegen.api.incidents.Incident {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final transient @Getter(value = AccessLevel.NONE) @Setter(value = AccessLevel.NONE)
            IncidentManager manager;

    @Getter
    private final long id;

    private IncidentStatus status;

    @NotNull
    private IncidentType type;

    @Getter(onMethod_ = {@ToStringListStringSelector})
    private Set<UnitAssignment> unitAssignments;

    @NotNull
    private IncidentLocation location;

    @NotNull
    private IncidentTime time;

    @NotNull
    private IncidentPublishedStatus published;

    @Getter(onMethod_ = {@ToStringListStringSelector})
    private List<IncidentLogEntry> log;

    private List<Contributor<?>> contributors;

    private @Setter(value = AccessLevel.NONE) Map<String, String> links;

    private transient @Getter IncidentMessagingService messagingService;

    private transient ScheduledFuture<?> staleFuture;
    private transient ScheduledFuture<?> closedFuture;
    private transient long unixNextStale = Long.MAX_VALUE;
    private transient long unixNextClosedDueToStale = Long.MAX_VALUE;

    public IncidentImpl() {
        this.manager = null;
        this.id = Long.MIN_VALUE;
    }

    public IncidentImpl(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = null;
        this.type = Main.config.getFireGenVariables().defaultType();
        this.location = new IncidentLocationImpl(new ArrayList<>());
        this.time = new IncidentTimeImpl(LocalDateTime.now());
        this.published = IncidentPublishedStatus.UNPUBLISHED;

        this.unitAssignments = new HashSet<>();
        this.log = new ArrayList<>();
        this.contributors = new ArrayList<>();

        this.links = new ConcurrentHashMap<>();

        this.messagingService = new IncidentMessagingService(this);
    }

    public void setTypeBySearch(String type) {
        IncidentType newType = Main.config.get(ConfigIncidentTypes.class).fromString(type);
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
    public void addLink(String url, String title) {
        this.links.put(url, title);
    }

    @Override
    public void removeLink(String url) {
        this.links.remove(url);
    }

    public void editLink(int index, String url, String title) {
        String originalKey = new ArrayList<>(this.links.keySet()).get(index);
        this.links.put(url, this.links.remove(originalKey));
    }

    public void removeLink(int index) {
        this.links.remove(new ArrayList<>(this.links.keySet()).get(index));
    }

    @Override
    public void setPublished(@NotNull IncidentPublishedStatus newStatus) {
        this.published = newStatus;
    }

    public boolean isPublished() {
        return this.getPublished() == IncidentPublishedStatus.PUBLISHED;
    }

    public Contributor<User> addContributor(User user) {
        FireGenUser fireGenUser = Main.users.getByDiscord(user);
        this.addContributor(fireGenUser);
        return fireGenUser;
    }

    public IncidentLogEntry addLog(LocalDateTime time, Contributor<?> user, IncidentLogEntry.EntryType type, String log) {
        return this.addLog(new IncidentLogEntryImpl(time, user, log, type));
    }

    @Override
    public IncidentLogEntry addLog(Contributor<?> user, IncidentLogEntry.EntryType type, String log) {
        return this.addLog(LocalDateTime.now(), user, type, log);
    }

    @Override
    public IncidentLogEntry addLog(IncidentLogEntry entry) {
        this.log.add(entry);
        return entry;
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
    public void assignUnit(Unit unit, Contributor<?> contributor, AssignmentStatus assignment, @Nullable Secondary secondary) {
        UnitAssignment unitAssignment = this.getUnitAssignmentFor(unit);
        if (unitAssignment == null) {
            unitAssignment = new UnitAssignmentImpl(this, unit, contributor);
            this.unitAssignments.add(unitAssignment);
        }

        unitAssignment.assign(contributor, assignment, secondary);
        this.refreshStatus();
    }

    @Override
    public void assignUnit(Unit unit, Contributor<?> contributor, AssignmentStatus assignment) {
        this.assignUnit(unit, contributor, assignment, null);
    }

    public UnitAssignment getUnitAssignmentFor(Unit unit) {
        for (UnitAssignment assignment : this.unitAssignments) {
            if (assignment.getUnit().equals(unit)) {
                return assignment;
            }
        }

        return null;
    }

    public boolean containsUnit(Unit unit) {
        return this.getUnitAssignments().stream()
                .filter(u -> u.getIncident().equals(this))
                .anyMatch(u -> u.getUnit().equals(unit));
    }

    public void removeUnit(Unit unit) {
        this.removeUnit(unit, true);
    }

    private void removeUnit(Unit unit, boolean fromAll) {
        if (fromAll) {
            Main.incidents.getAssignments().removeIf(ua -> ua.getUnit().equals(unit) && ua.getIncident().equals(this));
        }

        ((UnitImpl)unit).getAssignments().removeIf(ua -> ua.getIncident().equals(this));

        this.unitAssignments.removeIf(a -> a.getUnit().equals(unit));
        this.refreshStatus();
    }

    public List<UnitAssignment> getSortedAssignments() {
        List<UnitAssignment> sorted = new ArrayList<>(this.getUnitAssignments());
        Collections.sort(sorted);
        return sorted;
    }

    public void refreshStatus() {
        if (this.status != null && !this.status.isInProgress()) {
            return;
        }

        if (Time.getUnix() >= this.unixNextStale) {
            this.status = IncidentStatus.STALE;
        } else if (unitAssignments.isEmpty()) {
            this.status = IncidentStatus.PENDING;
        } else {
            this.status = IncidentStatus.ACTIVE;
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
        this.update(true);
    }

    private void update(boolean restartStale) {
        if (this.status == null) {
            return;
        }

        if (restartStale) {
            if (this.staleFuture != null)
                this.staleFuture.cancel(false);
            if (this.closedFuture != null)
                this.closedFuture.cancel(false);
        }

        long ELAPSED_TIME_THRESHOLD = 25; // milliseconds
        long start = System.currentTimeMillis();

        this.messagingService.sendAll();

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > ELAPSED_TIME_THRESHOLD) {
            Log.info("Took " + elapsed + "ms to update messages.");
        }

        this.refreshStatus();

        if (restartStale) {
            this.unixNextStale = Main.config.getFireGenVariables().incidentStaleMinutes();
            this.unixNextClosedDueToStale = Main.config.getFireGenVariables().incidentStaleCloseMinutes();

            this.staleFuture = scheduler.schedule(this::onStale, this.unixNextStale, TimeUnit.MINUTES);
            this.closedFuture = scheduler.schedule(this::onStaleClose, this.unixNextClosedDueToStale, TimeUnit.MINUTES);

            this.unixNextStale = Time.getUnix() + (this.unixNextStale * 60);
            this.unixNextClosedDueToStale = Time.getUnix() + (this.unixNextClosedDueToStale * 60);

            if (this.status == IncidentStatus.STALE) {
                this.refreshStatus();
                this.addLog(LocalDateTime.now(), SystemUser.get(), IncidentLogEntry.EntryType.UPDATE,
                        "Incident Active - No Longer Stale"
                );
                this.update(false);
            }
        }
    }

    private void onStale() {
        if (this.getStatus() == IncidentStatus.STALE
                || this.getStatus() == IncidentStatus.CLOSED
        ) {
            Log.warn("Attempted to execute onStale() despite incident already being 'STALE' or 'CLOSE' (currently " + this.getStatus() + ")");
            return;
        }

        Log.warn("Incident #" + this.getFormattedId() + " (" + this.getType().getSelectedName()
                + (this.getLocation().isSet() ? " @ " + this.getLocation().format() : "") + ") " +
                "has gone stale!"
        );
        this.addLog(LocalDateTime.now(), SystemUser.get(), IncidentLogEntry.EntryType.UPDATE,
                "Incident Stale - " + Main.config.getFireGenVariables().incidentStaleMinutes() + "m Inactivity"
        );

        this.status = IncidentStatus.STALE;
        this.update(false);
    }

    private void onStaleClose() {
        if (this.getStatus() != IncidentStatus.STALE) {
            Log.warn("Attempted to executed onStaleClose() despite incident not being 'STALE' (currently " + this.getStatus() + ")");
            return;
        }

        Log.warn("Incident #" + this.getFormattedId() + " (" + this.getType().getSelectedName()
                + (this.getLocation().isSet() ? " @ " + this.getLocation().format() : "") + ") " +
                "has closed due to its staleness!"
        );

        AssignmentStatus clear = Main.config.get(ConfigAssignmentStatuses.class).getFirstFor(AssignmentPurpose.UNIT_CLEAR);
        String recreateEvents = this.getUnitAssignments().stream()
                .filter(a -> !a.getLatestAssignment().getStatus().equals(clear))
                .map(a -> {
            Unit unit = a.getUnit();
            AssignmentEvent latest = a.getLatestAssignment();
            Secondary secondary = latest.getSecondary();
            return unit.getShorthand() + ":" + latest.getStatus().getShortName()
                    + (secondary != null ? ">" + secondary.getShortName() : "");
        }).collect(Collectors.joining(","));
        Log.info("Re-create statuses: " + recreateEvents);
        this.addLog(LocalDateTime.now(), SystemUser.get(), IncidentLogEntry.EntryType.NOTE,
                "All Units " + clear.getName().toUpperCase() + " - Recreate Statuses: " + recreateEvents
        );

        this.getUnitAssignments().forEach(a -> a.assign(SystemUser.get(), clear, null));

        this.addLog(LocalDateTime.now(), SystemUser.get(), IncidentLogEntry.EntryType.UPDATE,
                "Incident Closed - " + Main.config.getFireGenVariables().incidentStaleCloseMinutes() + "m Inactivity"
        );
        this.status = IncidentStatus.CLOSED;
        this.update(false);
    }

}