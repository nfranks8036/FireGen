package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;

public class UnitEmoji extends Command {

    public UnitEmoji() {
        super("unit-emoji", "Get the emoji relating to a specific unit.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "unit", "The unit to get the emoji of.", true, true)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        OptionMapping unitMapping = event.getOption("unit");
        if (unitMapping == null) {
            DiscordMessages.error(event, "You must specify a specific unit (see: /units).");
            return;
        }

        String unitString = unitMapping.getAsString();
        Unit unit = Main.incidents.getUnitByLonghand(unitString);
        if (unit == null) {
            DiscordMessages.error(event, "The specified text, `" + unitString + "`, does not come back to a real unit.");
            return;
        }
        UnitImpl agencyImpl = ((UnitImpl) unit);

        event.reply("The emoji for **" + unit.getLonghand() + "** (" + unit.getShorthand() + ") has the name " +
                "`:" + agencyImpl.getEmoji().getName() + ":` and looks like:\n"
                + "# <:" + agencyImpl.getEmoji().getAsReactionCode() + ">"
        ).setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("unit")) {
            return Main.incidents.getUnits().stream().map(Unit::getLonghand).toList();
        }
        return null;
    }
}
