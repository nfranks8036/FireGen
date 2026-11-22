package net.noahf.firegen.discord.actions.registered;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
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
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.MessageStatus;
import net.noahf.firegen.discord.utilities.PreventUnitAssignment;
import net.noahf.firegen.discord.utilities.errors.ErrorCode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the "Agencies" button in the Edit row.
 */
public class EditUnits implements ButtonAction, StringDropdownAction {

    private static final Map<FireGenUser, UnitsChangeInput> selectedUnits = new ConcurrentHashMap<>();

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
        UnitsChangeInput unitsInput = selectedUnits.get(ctx.getFireGenUser());

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
                        .setDefaultOptions((unitsInput != null ? unitsInput.getUnits() : new ArrayList<>())
                                .stream()
                                .map(u -> (UnitImpl)u)
                                .map(UnitImpl::getSelectOption)
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
                                        SelectOption option = SelectOption.of(s.longName(), "secondary-" + pair.getFirstElement());
                                        option = option
                                                .withDescription("(" + s.shortName() + ")");
                                        if (((SecondaryImpl)s).emoji() != null) {
                                            option = option.withEmoji(((SecondaryImpl)s).emoji());
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
    public void execute(ActionsContext ctx, FireGenUser<?> user, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_UNITS)) {
            DiscordMessages.error(event, "You don't have permission to change unit statuses on an incident.");
            return;
        }

        this.ensureIncidentOpen(event, ctx.getIncident());

        if (!ctx.getParameters().isEmpty()) {
            ServiceResponse<?> status = this.onSubmit(ctx.getIncident(), user, null);
            if (!status.hasObject()) {
                event.deferEdit().queue();
            }
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        selectedUnits.put(user,
                new UnitsChangeInput(
                        new ArrayList<>(), null, null
                )
        );

        AssignmentStatus selectedStatus = selectedUnits
                .getOrDefault(user, new UnitsChangeInput(new ArrayList<>(), null, null))
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
    public void execute(ActionsContext ctx, FireGenUser user, StringSelectInteractionEvent event) {
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
            UnitsChangeInput input = selectedUnits.get(user);
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident units.");
                return;
            }

            input.units = units;
            selectedUnits.put(user, input);
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

            UnitsChangeInput input = selectedUnits.get(user);
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident units.");
                return;
            }

            event.editComponents(this.getActionRows(ctx, (IncidentImpl) ctx.getIncident(), status)).queue();
            message = MessageStatus.CONTENT;

            input.newStatus = status;
            selectedUnits.put(user, input);
        }

        if (param.equalsIgnoreCase("secondary")) {
            int index = Integer.parseInt(
                    event.getSelectedOptions().getFirst().getValue().substring("secondary-".length())
            );

            UnitsChangeInput input = selectedUnits.get(user);
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
            selectedUnits.put(user, input);
        }

        if (message == MessageStatus.NONE) {
            event.deferEdit().queue();
        }
    }

    public ServiceResponse<?> onSubmit(Incident incidentValue, FireGenUser<?> user, @Nullable EditUnits.UnitsChangeInput inputUnits) {
        if (!user.hasPermission(Permission.CHANGE_UNITS)) {
            return ServiceResponse.fail(user, ErrorCode.NO_PERMISSION, "You don't have permission to change unit statuses on an incident.");
        }

        UnitsChangeInput input = (inputUnits == null ? selectedUnits.get(user) : inputUnits);
        if (input == null) {
            return ServiceResponse.fail(user, ErrorCode.NOT_CURRENTLY_EDITING, "You are not currently editing incident units.");
        }

        IncidentImpl incident = (IncidentImpl) incidentValue;
        List<Unit> units = input.units;
        AssignmentStatus status = input.newStatus != null ? input.newStatus : AssignmentStatusImpl.ADD_UNIT;
        incident.addContributor(user);

        if (units.isEmpty()) {
            return ServiceResponse.fail(user, ErrorCode.UNIT_ATTEMPT_SET_ZERO_UNITS, "You cannot set the unit status of zero units.");
        }

        ServiceResponse<?> message = ServiceResponse.successNone();

        List<Unit> unmodifiedList = new ArrayList<>(units);
        for (Unit unit : unmodifiedList) {
            if (status.equals(AssignmentStatusImpl.REMOVE_UNIT)) {
                incident.removeUnit(unit);
                continue;
            }

            if (unit.getAdditional() != null && unit.getAdditional() instanceof PreventUnitAssignment) {
                units.remove(unit);
                message = ServiceResponse.fail(user, ErrorCode.UNIT_HAS_CONTEXT_PREVENT_ADD,
                        "The unit '" + unit.toString() + "' contains additional context " +
                        "that means it cannot be added to the incident in its current form.");
            }

            incident.assignUnit(unit, user, status, input.secondary);
        }

        if (units.isEmpty()) {
            return message;
        }

        incident.addLog(user, IncidentLogEntry.EntryType.UNIT,
                "Unit" + (units.size() == 1 ? "" : "s") + " " +
                        String.join(", ", units) + " " +
                        status.getName()
                + (input.secondary != null ? " " + input.secondary.shortName() : "")
        );

        incident.update();

        return message;
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
    public void execute(ActionsContext ctx, FireGenUser user, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, user, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, user, e); }
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

        @Override
        public String toString() {
            return new StringJoiner(", ", UnitsChangeInput.class.getSimpleName() + "[", "]")
                    .add("units=" + units)
                    .add("newStatus=" + newStatus)
                    .add("secondary=" + secondary)
                    .toString();
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
