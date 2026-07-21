package net.noahf.firegen.discord.actions.registered;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.ConfigAssignmentStatuses;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.SecondaryImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.MessageStatus;
import org.jboss.jandex.Index;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the "Agencies" button in the Edit row.
 */
public class EditUnits implements ButtonAction, StringDropdownAction {

    private static final Map<User, UnitsChangeInput> selectedUnits = new ConcurrentHashMap<>();

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "units";
    }

    public List<ActionRow> getActionRows(ActionsContext ctx, IncidentImpl incident, AssignmentStatus selectedStatus) {
        ConfigManager config = ctx.getConfig();
        SelectOption[] selectedOption = new SelectOption[]{this.toSelectOption(selectedStatus)};
        if (selectedStatus == null) {
            selectedOption = new SelectOption[0];
        }

        List<ActionRow> rows = new ArrayList<>(List.of(
                ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "select"))
                        .addOptions(config.get(ConfigUnits.class).get().stream()
                                .map(a -> (UnitImpl) a)
                                .map((a) -> {
                                    UnitAssignment status = incident.getUnitAssignmentFor(a);
                                    if (status == null) {
                                        return a.getSelectOption();
                                    }
                                    return a.getSelectOption().withDescription(
                                            "Status: " + status.getLatestAssignment().getStatus().getName()
                                    );
                                })
                                .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                .toList()
                        )
                        .setRequired(true)
                        // the max amount of value IS the amount of values we have available.
                        // i.e., the user can select infinite agencies (attach all agencies)
                        .setMaxValues(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                        .build()
                ),
                ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "status"))
                        .addOptions(config.get(ConfigAssignmentStatuses.class).get().stream()
                                .map(this::toSelectOption)
                                .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                .toList()
                        )
                        .setDefaultOptions(selectedOption)
                        .setRequired(true)
                        .setMaxValues(1)
                        .build()
                )
        ));

        if (selectedStatus != null && !selectedStatus.getSecondaries().isEmpty()) {
            rows.add(ActionRow.of(
                    StringSelectMenu.create(this.callbackId(ctx, "secondary"))
                            .addOptions(ImmutablePair.withIndices(selectedStatus.getSecondaries()).stream()
                                    .map(pair -> {
                                        Secondary s = pair.getSecondElement();
                                        SelectOption option = SelectOption.of(s.getLongName(), "secondary-" + pair.getFirstElement());
                                        option = option
                                                .withDescription("(" + s.getShortName() + ")");
                                        if (((SecondaryImpl)s).getEmoji() != null) {
                                            option = option.withEmoji(((SecondaryImpl)s).getEmoji());
                                        }
                                        return option;
                                    })
                                    .toList()
                            )
                            .setRequired(false)
                            .setMaxValues(1)
                            .build()
            ));
        }

        rows.add(
                ActionRow.of(
                        Button.success(this.callbackId(ctx, "submit"), "Submit")
                                .withEmoji(Emoji.fromFormatted("✅")))
        );
        return rows;
    }

    /**
     * The event that occurs after pressing the "Agencies" button in the "Edit" row. This replies a String Select Menu
     * to the Discord user that allows them to select agencies they wish to append or remove from the incident.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_UNITS)) {
            DiscordMessages.error(event, "You don't have permission to change unit statuses on an incident.");
            return;
        }

        this.ensureIncidentOpen(event, ctx.getIncident());

        if (!ctx.getParameters().isEmpty()) {
            MessageStatus status = this.onSubmit(ctx.getIncident(), event, null);
            DiscordMessages.noMessage(event, status);
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        selectedUnits.put(event.getUser(),
                new UnitsChangeInput(
                        new ArrayList<>(), null, null
                )
        );

        AssignmentStatus selectedStatus = selectedUnits
                .getOrDefault(event.getUser(), new UnitsChangeInput(new ArrayList<>(), null, null))
                .newStatus;
        event.reply("Choose units to update their status.")
                .setEphemeral(true)
                .setComponents(
                        this.getActionRows(ctx, incident, selectedStatus)
                )
                .queue();
    }

    /**
     * The event that occurs after clicking away from the {@link StringSelectMenu} from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        String param = ctx.getParameters().getFirst();

        MessageStatus message = MessageStatus.NONE;
        // FIRST STEP: Select the units
        if (param.equalsIgnoreCase("select")) {
            List<Unit> units = new ArrayList<>();
            for (Unit unit : Main.config.get(ConfigUnits.class).get()) {
                if (!event.getValues().contains(unit.getShorthand())) {
                    continue;
                }
                units.add(unit);
            }

            // this way we can keep track of the selected units for the next step (changing their status)
            UnitsChangeInput input = selectedUnits.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident units.");
                return;
            }

            input.units = units;
            selectedUnits.put(event.getUser(), input);
        }
        // SECOND STEP: Select the status
        if (param.equalsIgnoreCase("status")) {
            String statusStr = event.getSelectedOptions().getFirst().getValue();
            AssignmentStatusImpl status = Main.config.get(ConfigAssignmentStatuses.class).get()
                    .stream().filter(as -> as.getShortName().equalsIgnoreCase(statusStr))
                    .map(a -> (AssignmentStatusImpl) a)
                    .findFirst().orElse(null);
            if (status == null) {
                DiscordMessages.error(event, "You did not select an assignment status");
                return;
            }

            UnitsChangeInput input = selectedUnits.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident units.");
                return;
            }

            event.editComponents(this.getActionRows(ctx, (IncidentImpl) ctx.getIncident(), status)).queue();
            message = MessageStatus.CONTENT;

            input.newStatus = status;
            selectedUnits.put(event.getUser(), input);
        }

        if (param.equalsIgnoreCase("secondary")) {
            int index = Integer.parseInt(
                    event.getSelectedOptions().getFirst().getValue().substring("secondary-".length())
            );

            UnitsChangeInput input = selectedUnits.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident units.");
                return;
            }

            if (input.newStatus == null) {
                DiscordMessages.error(event, "You cannot set the secondary without setting a status!");
                return;
            }

            Secondary secondary = input.newStatus.getSecondaries().get(index);
            if (secondary == null) {
                DiscordMessages.error(event, "That secondary (index " + index + ") does not exist for assignment: " + input.newStatus.toString());
                return;
            }

            input.secondary = secondary;
            selectedUnits.put(event.getUser(), input);
        }

        DiscordMessages.noMessage(event, message);
    }

    public MessageStatus onSubmit(Incident incidentValue, IReplyCallback event, @Nullable EditUnits.UnitsChangeInput inputUnits) {
        UnitsChangeInput input = (inputUnits == null ? selectedUnits.get(event.getUser()) : inputUnits);
        if (input == null) {
            DiscordMessages.error(event, "You are not currently editing incident units.");
            return MessageStatus.CONTENT;
        }

        IncidentImpl incident = (IncidentImpl) incidentValue;
        List<Unit> units = input.units;
        AssignmentStatus status = input.newStatus != null ? input.newStatus : AssignmentStatusImpl.ADD_UNIT;
        Contributor<User> user = incident.addContributor(event.getUser());

        if (units.isEmpty()) {
            DiscordMessages.error(event, "You cannot set the unit status of zero units.");
            return MessageStatus.CONTENT;
        }

        if (status.equals(AssignmentStatusImpl.REMOVE_UNIT)) {

            units.forEach(incident::removeUnit);

        } else {

            units.forEach(u -> incident.assignUnit(u, user, status, input.secondary));

        }

        incident.addLog(user, IncidentLogEntry.EntryType.UNIT,
                "Unit" + (units.size() == 1 ? "" : "s") + " " +
                        String.join(", ", units) + " " +
                        status.getName()
                + (input.secondary != null ? " " + input.secondary.getShortName() : "")
        );

        incident.update();

        return MessageStatus.NONE;
    }


    private SelectOption toSelectOption(AssignmentStatus assignmentStatus) {
        if (assignmentStatus == null) {
            return null;
        }
        AssignmentStatusImpl status = (AssignmentStatusImpl) assignmentStatus;
        return SelectOption.of(status.getName(), status.getShortName())
                .withEmoji(status.getEmoji());
    }

    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, e); }
    }

    @Getter @AllArgsConstructor
    public static class UnitsChangeInput {
        private List<Unit> units;
        private AssignmentStatus newStatus;
        private Secondary secondary;

        public void setUnits(List<Unit> units) {
            if (this.units == null) {
                this.units = new ArrayList<>();
            }
            this.units.addAll(units);
        }
    }

//    String narrative;
//            if (status.equals(AssignmentStatus.REMOVE_AGENCY)) {
//        incident.removeAgencies(agencies);
//        narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
//                String.join(", ", agencies) + " removed from incident";
//    } else {
//        incident.putAgencies(
//                agencies.stream().collect(Collectors.toMap(
//                        (k) -> k, (v) -> status
//                ))
//        );
//        narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
//                String.join(", ", agencies) + " " + status.getName();
//    }
//
//            if (status.equals(AssignmentStatus.HIDE_STATUS)) {
//        narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
//                String.join(", ", agencies) + " unset";
//    }
//
//            DiscordMessages.noMessage(event);
//
//    Contributor<User> user = incident.addContributor(event.getUser());
//            incident.addLog(user, IncidentLogEntry.EntryType.UPDATE, narrative);
//
//            incident.update();
}
