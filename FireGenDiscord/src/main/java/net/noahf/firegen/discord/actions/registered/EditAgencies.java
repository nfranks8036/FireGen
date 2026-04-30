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
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.ActionsContext;
import net.noahf.firegen.discord.actions.ButtonAction;
import net.noahf.firegen.discord.actions.StringDropdownAction;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.AssignmentStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Represents the "Agencies" button in the Edit row.
 */
public class EditAgencies implements ButtonAction, StringDropdownAction {

    private static final Map<User, AgenciesInput> selectedAgencies = new ConcurrentHashMap<>();

    /**
     * The name of the command needed to access this class
     */
    @Override
    public String getName() {
        return "agencies";
    }

    /**
     * The event that occurs after pressing the "Agencies" button in the "Edit" row. This replies a String Select Menu
     * to the Discord user that allows them to select agencies they wish to append or remove from the incident.
     */
    @Override
    public void execute(ActionsContext ctx, ButtonInteractionEvent event) {
        if (!this.checkUserPermission(event.getUser(), Permission.CHANGE_AGENCIES)) {
            DiscordMessages.error(event, "You don't have permission to change agencies' status on an incident.");
            return;
        }

        if (!ctx.getParameters().isEmpty()) {
            this.onSubmit(ctx, event);
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        selectedAgencies.put(event.getUser(),
                new AgenciesInput(
                        ThreadLocalRandom.current().nextInt(100000, 999999),
                        new ArrayList<>(), null
                )
        );

        SelectOption[] selectedStatus = new SelectOption[] {this.toSelectOption(
                selectedAgencies
                        .getOrDefault(event.getUser(), new AgenciesInput(-1, new ArrayList<>(), null))
                        .newStatus
        )};
        if (selectedStatus[0] == null) {
            selectedStatus = new SelectOption[0];
        }

        event.reply("Choose agencies to update their status.")
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "select"))
                                .addOptions(Main.incidents.getAgencies().stream()
                                        .map(a -> (AgencyImpl) a)
                                        .map((a) -> {
                                            AssignmentStatus status = incident.getAgencies().get(a);
                                            if (status == null) {
                                                return a.getSelectOption();
                                            }
                                            return a.getSelectOption().withDescription(
                                                    "Status: " + status.getName()
                                            );
                                        })
                                        .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                        .toList()
                                )
                                .setDefaultOptions(
                                        selectedAgencies.getOrDefault(event.getUser(), new AgenciesInput(-1, new ArrayList<>(), null))
                                                .agencies.stream()
                                                .map(a -> (AgencyImpl) a)
                                                .map(a -> {
                                                    AssignmentStatus status = incident.getAgencies().get(a);
                                                    if (status == null) {
                                                        return a.getSelectOption();
                                                    }
                                                    return a.getSelectOption().withDescription(
                                                            "Status: " + status.getName()
                                                    );
                                                })
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
        // FIRST STEP: Select the agencies
        if (ctx.getParameters().getFirst().equalsIgnoreCase("select")) {
            List<Agency> agencies = new ArrayList<>();
            for (Agency agency : Main.incidents.getAgencies()) {
                if (!event.getValues().contains(agency.getShorthand())) {
                    continue;
                }
                agencies.add(agency);
            }

            // this way we can keep track of the selected agencies for the next step (changing their status)
            AgenciesInput input = selectedAgencies.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident agencies.");
                return;
            }

            input.agencies = agencies;
            selectedAgencies.put(event.getUser(), input);
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

            AgenciesInput input = selectedAgencies.get(event.getUser());
            if (input == null) {
                DiscordMessages.error(event, "You are not currently editing incident agencies.");
                return;
            }

            input.newStatus = status;
            selectedAgencies.put(event.getUser(), input);
        }

        DiscordMessages.noMessage(event);
    }

    private void onSubmit(ActionsContext ctx, ButtonInteractionEvent event) {
        AgenciesInput input = selectedAgencies.get(event.getUser());
        if (input == null) {
            DiscordMessages.error(event, "You are not currently editing incident agencies.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();
        List<Agency> agencies = input.agencies;
        AssignmentStatus status = input.newStatus != null ? input.newStatus : AssignmentStatus.HIDE_STATUS;

        String narrative;
        if (status.equals(AssignmentStatus.REMOVE_AGENCY)) {
            incident.removeAgencies(agencies);
            narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
                    String.join(", ", agencies) + " removed";
        } else {
            incident.putAgencies(
                    agencies.stream().collect(Collectors.toMap(
                            (k) -> k, (v) -> status
                    ))
            );
            narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
                    String.join(", ", agencies) + " " + status.getName();
        }

        if (status.equals(AssignmentStatus.HIDE_STATUS)) {
            narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
                    String.join(", ", agencies) + " added";
        }

        DiscordMessages.noMessage(event);

        Contributor<User> user = incident.addContributor(event.getUser());
        incident.addLog(user, IncidentLogEntry.EntryType.UPDATE, narrative);

        incident.update();
    }


    private SelectOption toSelectOption(AssignmentStatus status) {
        if (status == null) {
            return null;
        }
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
    private static class AgenciesInput {
        private long id;
        private List<Agency> agencies;
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
