package net.noahf.firegen.discord.actions.registered;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.User;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Represents the "Agencies" button in the Edit row.
 */
public class EditAgencies implements ButtonAction, StringDropdownAction {

    private static final Map<Long, List<Agency>> selectedAgencies = new ConcurrentHashMap<>();

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
            DiscordMessages.error(event, "You don't have permission to add or remove agencies from an incident.");
            return;
        }

        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        event.reply("Choose agencies to update their status.")
                .setEphemeral(true)
                .setComponents(ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "select"))
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
                        .setRequired(true)
                        // the max amount of value IS the amount of values we have available.
                        // i.e., the user can select infinite agencies (attach all agencies)
                        .setMaxValues(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                        .build()
                ))
                .queue();
    }

    /**
     * The event that occurs after clicking away from the {@link StringSelectMenu} from the event above.
     */
    @Override
    public void execute(ActionsContext ctx, StringSelectInteractionEvent event) {
        IncidentImpl incident = (IncidentImpl) ctx.getIncident();

        // FIRST STEP: Select the agencies
        if (ctx.getParameters().getFirst().equalsIgnoreCase("select")) {
            List<Agency> agencies = new ArrayList<>();
            for (Agency agency : Main.incidents.getAgencies()) {
                if (!event.getValues().contains(agency.getShorthand())) {
                    continue;
                }
                agencies.add(agency);
            }

            long id = ThreadLocalRandom.current().nextInt(10000, 99999);

            // this way we can keep track of the selected agencies for the next step (changing their status)
            selectedAgencies.put(id, agencies);

            event.reply("You selected **" + String.join(", ", agencies) + "**.\n\n" +
                            "Select a new status for these agencies:")
                    .setEphemeral(true)
                    .setComponents(ActionRow.of(StringSelectMenu.create(this.callbackId(ctx, "status", String.valueOf(id)))
                            .addOptions(Main.incidents.getAssignmentStatuses().stream()
                                    .map(s -> SelectOption.of(s.getName(), s.getShortName())
                                            .withEmoji(s.getEmoji())
                                    )
                                    .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                    .toList()
                            )
                            .setRequired(true)
                            .setMaxValues(1)
                            .build()
                    ))
                    .complete();

        }
        // SECOND STEP: Select the status
        if (ctx.getParameters().getFirst().equalsIgnoreCase("status")) {
            List<Agency> agencies = selectedAgencies.get(Long.parseLong(ctx.getParameters().get(1)));
            if (agencies == null || agencies.isEmpty()) {
                DiscordMessages.error(event, "You did not select any agencies.");
                return;
            }

            String statusStr = event.getSelectedOptions().getFirst().getValue();
            AssignmentStatus status = Main.incidents.getAssignmentStatuses()
                    .stream().filter(as -> as.getShortName().equalsIgnoreCase(statusStr))
                    .findFirst().orElse(null);
            if (status == null) {
                DiscordMessages.error(event, "You did not select an assignment status");
                return;
            }

            String narrative;
            if (status.equals(AssignmentStatus.REMOVE_AGENCY)) {
                incident.removeAgencies(agencies);
                narrative = (agencies.size() == 1 ? "Agency " : "Agencies ") +
                        String.join(", ", agencies) + " removed from incident";
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
                        String.join(", ", agencies) + " unset";
            }

            DiscordMessages.noMessage(event);

            Contributor<User> user = incident.addContributor(event.getUser());
            incident.addLog(user, IncidentLogEntry.EntryType.UPDATE, narrative);

            incident.update();
        }
    }


    @Override
    public void execute(ActionsContext ctx, GenericInteractionCreateEvent event) {
        if (event instanceof  ButtonInteractionEvent e) { this.execute(ctx, e); }
        if (event instanceof  StringSelectInteractionEvent e) { this.execute(ctx, e); }
    }
}
