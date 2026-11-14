package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.registered.EditMode;
import net.noahf.firegen.discord.actions.registered.EditUnits;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigAssignmentStatuses;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.SecondaryImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.MessageStatus;

import java.util.*;
import java.util.stream.Collectors;

public class AssignUnit extends Command {

    public AssignUnit() {
        super("assign-unit", "Assigns a unit to an incident.", CommandFlags.include()
                .options(new OptionData[]{
                        new OptionData(OptionType.STRING, "unit", "The unit to assign.", true, true),
                        new OptionData(OptionType.STRING, "status", "OPTIONAL: The status of the unit.", false, true),
                        new OptionData(OptionType.STRING, "secondary", "OPTIONAL: The secondary information about a unit.", false, true),
                })
                .aliases(new String[]{"au"})
                .disableAutocompleteAutoFilter(true)
                .finish());
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        IncidentImpl incident = (IncidentImpl) EditMode.editIncidents.get(event.getUser());

        if (incident == null) {
            DiscordMessages.error(event, "You are not currently editing an incident. " +
                    "Press 'Edit Mode' of the incident of your choice."
            );
            return;
        }

        if (!incident.getStatus().isInProgress()) {
            DiscordMessages.error(event, "This incident is closed and cannot be edited.");
            return;
        }

        String unitStr = event.getOption("unit", () -> "?", OptionMapping::getAsString);
        ConfigUnits configUnits = Main.config.get(ConfigUnits.class);
        List<Unit> units = new ArrayList<>();
        for (String us : unitStr.contains(",") ? unitStr.split(",") : new String[]{unitStr}) {
            Unit unit = configUnits.fromLonghand(us, configUnits.fromShorthand(us));
            if (unit == null) {
                DiscordMessages.error(event,
                        "Invalid unit: *" + us + "*\n" +
                                "View a list of units at `/view-config file:" + Main.config.get(ConfigUnits.class).getPath() + "`"
                        );
                return;
            }
            units.add(unit);
        }

        OptionMapping statusMapping = event.getOption("status");
        AssignmentStatus defaultStatus = AssignmentStatusImpl.ADD_UNIT;
        AssignmentStatus status;
        String statusStr = (statusMapping != null ? statusMapping.getAsString() : defaultStatus.getName());
        if (statusMapping != null) {
            ConfigAssignmentStatuses config = Main.config.get(ConfigAssignmentStatuses.class);
            status = config.fromName(
                    statusStr, config.fromShortName(statusStr)
            );
        } else {
            status = defaultStatus;
        }

        String secondaryStr = event.getOption("secondary", () -> null, OptionMapping::getAsString);
        Secondary secondary = secondaryStr != null ? new SecondaryImpl(secondaryStr, secondaryStr, null) : null;
        if (secondaryStr != null) {
            secondary = status.getSecondaries().stream()
                    .filter(s -> s.getShortName().equalsIgnoreCase(secondaryStr)
                            || s.getLongName().equalsIgnoreCase(secondaryStr)
                    )
                    .findFirst().orElse(secondary);
        }

        if (units.isEmpty()) {
            DiscordMessages.error(event, "No units were provided, this command requires at least one unit!\n" +
                    "View a list of units at `/view-config file:" + Main.config.get(ConfigUnits.class).getPath() + "`");
            return;
        }

        if (status == null) {
            DiscordMessages.error(event, "Invalid unit status: *" + statusStr + "*\n" +
                    "View a list of unit statuses at `/view-config file:" + Main.config.get(ConfigAssignmentStatuses.class).getPath() + "`");
            return;
        }

        EditUnits editUnits = Main.actions.getAction(EditUnits.class);
        MessageStatus s = editUnits.onSubmit(incident, event, new EditUnits.UnitsChangeInput(
                units, status, secondary
        ));
        if (s == MessageStatus.CONTENT) return;

        DiscordMessages.selfDestruct(event, 5,
                units.stream().map(u -> (UnitImpl)u).map(u -> u.getFormattedStatus(status, null)).collect(Collectors.joining(", ")) +
                        ": " + status.getName() +
                        (secondary != null ? " (" + secondary.getShortName() + ")" : "")
                        + "\n-# " + incident.toString()
                );
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "unit" -> SetUnitStatus.autocompleteUnits(focused.getValue());
            case "status" -> {
                List<AssignmentStatus> statuses = Main.config.get(ConfigAssignmentStatuses.class).get();

                yield Main.commands.autocompleteSearch(focused.getValue(),
                        statuses.stream().map(AssignmentStatus::getName).toList()
                );
            }
            case "secondary" -> {
                ConfigAssignmentStatuses configStatus = Main.config.get(ConfigAssignmentStatuses.class);

                OptionMapping statusMapping = event.getOption("status");
                if (statusMapping == null) {
                    yield null;
                }

                AssignmentStatus status = configStatus.fromName(statusMapping.getAsString(), null);
                if (status == null) {
                    yield null;
                }

                yield Main.commands.autocompleteSearch(focused.getValue(),
                        status.getSecondaries().stream().map(Secondary::getLongName).toList()
                );
            }
            default -> null;
        };
    }
}
