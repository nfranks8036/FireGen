package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.units.Status;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigAssignmentStatuses;
import net.noahf.firegen.discord.config.files.ConfigOperationalStatuses;
import net.noahf.firegen.discord.config.files.ConfigUnits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SetUnitStatus extends Command {

    public SetUnitStatus() {
        super(
                "set-unit-status", "Sets a specific units status",
                CommandFlags.include()
                        .aliases(new String[]{"sus"})
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "unit", "The unit who's status should be set", true, true),
                                new OptionData(OptionType.STRING, "status", "The status to set the unit as. May require incident if not associated.", true, true),
                                new OptionData(OptionType.STRING, "incident", "The incident to attach the unit to", false, true)

                        })
                        .disableAutocompleteAutoFilter(true)
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply("# Hello, World!\n*This command has not been constructed yet, try again later.*").setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return switch (focused.getName()) {
            case "unit" -> autocompleteUnits(focused.getValue());
            case "status" -> {
                List<Status> statuses = new ArrayList<>();
                statuses.addAll(Main.config.get(ConfigOperationalStatuses.class).get());
                statuses.addAll(Main.config.get(ConfigAssignmentStatuses.class).get());


                yield Main.commands.autocompleteSearch(focused.getValue(), statuses.stream().map(Status::getName).toList());
            }
            case "incident" -> autocompleteIncidents(focused.getValue());
            default -> null;
        };
    }

    public static List<String> autocompleteUnits(String input) {
        int indexOf = input.lastIndexOf(',');
        String prior = indexOf != -1 ? input.substring(0, indexOf) : "";
        String after = indexOf != -1 ? input.substring(indexOf + 1) : "";

        List<Unit> units = Main.config.get(ConfigUnits.class).get();
        List<String> returned = Stream.concat(
                        units.stream().map(Unit::getLonghand),
                        units.stream().map(Unit::getShorthand)
                )
                .toList();

        returned = Main.commands.autocompleteSearch(indexOf != -1 ? after : input, returned);

        return returned.stream().map(s ->
                (indexOf != -1 ?  prior + "," : "")
                        + s).toList();
    }


    private static long lastUpdated;
    private static List<String> incidents;
    public static List<String> autocompleteIncidents(String input) {
        if (lastUpdated < System.currentTimeMillis()) {
            incidents.clear();
            lastUpdated = System.currentTimeMillis() + (20 * 1000L);
            incidents.addAll(
                    Main.incidents.getIncidents().stream()
                            .map(i ->
                                    i.getFormattedId() + ": " +
                                            i.getType().getSelectedName() +
                                            (i.getLocation().isSet() ? " @ " + i.getLocation().format() : "")
                            )
                            .map(s -> DiscordMessages.truncate(s, 100, "..."))
                            .toList()
            );
        }
        return Main.commands.autocompleteSearch(input, incidents);
    }
}
