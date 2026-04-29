package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;

public class AgencyEmoji extends Command {

    public AgencyEmoji() {
        super("agency-emoji", "Get the emoji relating to a specific agency.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "agency", "The agency to get the emoji of.", true, true)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        OptionMapping agencyMapping = event.getOption("agency");
        if (agencyMapping == null) {
            DiscordMessages.error(event, "You must specify a specific agency.");
            return;
        }

        String agencyString = agencyMapping.getAsString();
        Agency agency = Main.incidents.getAgencyByLonghand(agencyString);
        if (agency == null) {
            DiscordMessages.error(event, "The specified text, `" + agencyString + "`, does not come back to a real agency.");
            return;
        }
        AgencyImpl agencyImpl = ((AgencyImpl) agency);

        event.reply("The emoji for **" + agency.getLonghand() + "** (" + agency.getShorthand() + ") has the name " +
                "`:" + agencyImpl.getEmoji().getName() + ":` and looks like:\n"
                + "# <:" + agencyImpl.getEmoji().getAsReactionCode() + ">"
        ).setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("agency")) {
            return Main.incidents.getAgencies().stream().map(Agency::getLonghand).toList();
        }
        return null;
    }
}
