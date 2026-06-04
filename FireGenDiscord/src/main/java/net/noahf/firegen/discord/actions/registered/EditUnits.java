package net.noahf.firegen.discord.actions.registered;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
            this.onSubmit(ctx.getIncident(), event, null);
            DiscordMessages.noMessage(event);
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        selectedUnits.put(event.getUser(),
                new UnitsChangeInput(
                        new ArrayList<>(), null
                )
        );

        SelectOption[] selectedStatus = new SelectOption[] {this.toSelectOption(
                selectedUnits
                        .getOrDefault(event.getUser(), new UnitsChangeInput(new ArrayList<>(), null))
                        .newStatus
        )};
        if (selectedStatus[0] == null) {
            selectedStatus = new SelectOption[0];
        }

        event.reply("Choose units to update their status.")
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "select"))
                                .addOptions(Main.incidents.getUnits().stream()
                                        .map(a -> (UnitImpl) a)
                                        .map((a) -> {
                                            UnitAssignment status = incident.getUnitAssignmentFor(a);
                                            if (status == null) {
                                                return a.getSelectOption();
                                            }
                                            return a.getSelectOption().withDescription(
                                                    "Status: " + status.getLatestAssignment().status().getName()
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
                                .addOptions(Main.incidents.getAssignmentStatuses().stream()
                                        .map(this::toSelectOption)
                                        .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                        .toList()
                                )
                                        .setDefaultOptions(selectedStatus)
                                .setRequired(true)
                                .setMaxValues(1)
                                .build()
                        ),
                        ActionRow.of(
                                Button.success(this.callbackId(ctx, "submit"), "Submit")
                                        .withEmoji(Emoji.fromFormatted("✅"))
                        )
                )
                .queue();
    }

    /**
     * The event that occurs after clicking away from the {@link StringSelectMenu} from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        // FIRST STEP: Select the units
        if (ctx.getParameters().getFirst().equalsIgnoreCase("select")) {
            List<Unit> units = new ArrayList<>();
            for (Unit unit : Main.incidents.getUnits()) {
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
        if (ctx.getParameters().getFirst().equalsIgnoreCase("status")) {
            String statusStr = event.getSelectedOptions().getFirst().getValue();
            AssignmentStatus status = Main.incidents.getAssignmentStatuses()
                    .stream().filter(as -> as.getShortName().equalsIgnoreCase(statusStr))
                    .findFirst().orElse(null);
            if (status == null) {
                DiscordMessages.error(event, "You did not select an assignment status");
                return;
            }

            UnitsChangeInput input = selectedUnits.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident agencies.");
                return;
            }

            input.newStatus = status;
            selectedUnits.put(event.getUser(), input);
        }

        DiscordMessages.noMessage(event);
    }

    public void onSubmit(Incident incidentValue, IReplyCallback event, @Nullable EditUnits.UnitsChangeInput inputUnits) {
        UnitsChangeInput input = (inputUnits == null ? selectedUnits.get(event.getUser()) : inputUnits);
        if (input == null) {
            DiscordMessages.error(event, "You are not currently editing incident units.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) incidentValue;
        List<Unit> units = input.units;
        AssignmentStatus status = input.newStatus != null ? input.newStatus : AssignmentStatusImpl.HIDE_STATUS;
        Contributor<User> user = incident.addContributor(event.getUser());

        String narrative = "Unit" + (units.size() == 1 ? "" : "s") + " ";
        if (status.equals(AssignmentStatusImpl.REMOVE_UNIT)) {
            incident.removeUnits(units);
            narrative = narrative + String.join(", ", units) + " removed";
        } else if (!status.equals(AssignmentStatusImpl.HIDE_STATUS)) {
            units.forEach(u -> incident.assignUnit(u, user, status));
            narrative = narrative + String.join(", ", units) + " " + status.getName();
        }

        if (status.equals(AssignmentStatusImpl.HIDE_STATUS)) {
            narrative = narrative + String.join(", ", units) + " added";
        }

        incident.addLog(user, IncidentLogEntry.EntryType.UNIT, narrative);

        incident.update();
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

    @AllArgsConstructor
    @Getter @Setter
    public static class UnitsChangeInput {
        private List<Unit> units;
        private AssignmentStatus newStatus;
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
