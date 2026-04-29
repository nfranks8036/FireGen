package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.util.List;
import java.util.StringJoiner;

public class Agencies extends Command {

    public Agencies() {
        super("agencies", "View the list of agencies that serve the area.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        List<Agency> agencies = Main.incidents.getAgencies();
        String areaName = Main.incidents.getMunicipality().getName();

        StringJoiner joiner = new StringJoiner("\n");
        for (Agency agency : agencies) {
            String emoji = ((AgencyImpl)agency).getEmoji().getFormatted();
            joiner.add(emoji + " " + agency.getLonghand() + " (`" + agency.getShorthand() + "`)");
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("There are " + agencies.size() + " agencies that serve " + areaName)
                .setDescription(joiner.toString())
                .setColor(DiscordMessages.randomColorForEmbed())
                .setFooter("Use \"/agency-emoji <agency>\" to obtain the name of the emoji used.")
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

}
