package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Time;

import java.util.stream.Collectors;

public class ViewArea extends Command {

    public ViewArea() {
        super("view-area", "View the information about the area that the bot is running off of.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        IncidentManager incidents = Main.incidents;
        SystemMunicipality municipality = incidents.getMunicipality();
        FireGenVariables vars = incidents.getFireGenVariables();
        MessageEmbed embed = new EmbedBuilder()
                .setColor(DiscordMessages.randomColorForEmbed())
                .setDescription("There are " + incidents.countIncidents() + " incidents that have been reported.")
                .addField("Municipality", municipality.getName() + " (" + municipality.getShortName() + ")", true)
                .addField("Dispatch Center", municipality.getDispatchName(), true)
                .addField("State", municipality.getState().getName() + " (" + municipality.getState().getAbbreviation() + ")", true)
                .addField("Agencies", "`" + incidents.getAgencies().size() + "` registered", true)
                .addField("Incident Types", "`" + incidents.getIncidentTypes().size() + "` registered", true)
                .addField("Identifiers", "`" + IdGenerator.getGeneratedIdsAmount() + "` identifiers generated", true)
                .addField("Venues", String.join("\n", incidents.getVenues()), true)
                .addField("Date & Time Formats", "Date: `" + vars.dateFormat() + "`\nTime (Long): `" + vars.longTimeFormat() + "`\nTime (Short): `" + vars.shortTimeFormat() + "`", true)
                .addField("Defaults", "Default Incident Type: `" + vars.defaultType() + "`\nDefault Incident Tag: `" + vars.defaultTag().toString() + "`", true)
                .setFooter("The bot has been online for " + Time.getTimeDifference(System.currentTimeMillis(), Main.botStartTime, Time.TimeDiffStyle.COMPACT) + ".")
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
