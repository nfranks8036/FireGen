package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.structure.UnitImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;
import java.util.StringJoiner;

public class Units extends Command {

    public Units() {
        super("units", "View the list of units that serve the area.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        List<Unit> units = Main.incidents.getUnits();
        String areaName = Main.incidents.getMunicipality().getName();

        StringJoiner joiner = new StringJoiner("\n");
        for (Unit unit : units) {
            String emoji = ((UnitImpl) unit).getEmoji().getFormatted();
            joiner.add(emoji + " " + unit.getLonghand() + " (`" + unit.getShorthand() + "`)");
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("There are " + units.size() + " units that serve " + areaName)
                .setDescription(joiner.toString())
                .setColor(DiscordMessages.randomColorForEmbed())
                .setFooter("Use \"/unit-emoji <unit>\" to obtain the name of the emoji used.")
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

}
