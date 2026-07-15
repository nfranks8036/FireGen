package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.*;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.utilities.Time;

public class ViewArea extends Command {

    public ViewArea() {
        super("view-area", "View the information about the area that the bot is running off of.",
                CommandFlags.include()
                        .aliases(new String[]{"info", "view-info"})
                        .finish()
                );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        ConfigManager config = Main.config;
        IncidentManager incidents = Main.incidents;
        SystemMunicipality municipality = config.get(ConfigMunicipality.class).get();
        FireGenVariables vars = incidents.getFireGenVariables();
        ConfigUnits units = config.get(ConfigUnits.class);

        MessageEmbed embed = new EmbedBuilder()
                .setColor(DiscordMessages.randomColorForEmbed())
                .setDescription("There are " + incidents.countIncidents() + " incidents that have been reported.")
                .addField("Municipality", municipality.getName() + " (" + municipality.getShortName() + ")", true)
                .addField("Dispatch Center", municipality.getDispatchName(), true)
                .addField("Defaults", "Default Incident Type: `" + vars.defaultType() + "`\nDefault Incident Tag: `" + vars.defaultTag().toString() + "`", true)
                .addField("Registered",
                        "Units: `" + units.count() + "` (`" + units.getAgencies().size() + "` agencies)\n" +
                                "Incident Types: `" + config.get(ConfigIncidentTypes.class).get().size() + "`\n" +
                                "Assignment Statuses: `" + config.get(ConfigAssignmentStatuses.class).count() + "`\n" +
                                "Locations: `" + config.get(ConfigLocationPresets.class).count() + "`\n" +
                                "Radio Channels: `" + config.get(ConfigRadioChannels.class).count() + "`"
                        , true)
                .addField("Venues", String.join(", ", config.get(ConfigVenues.class).get()), true)
                .addField("Date & Time Formats", "Date: `" + vars.dateFormat() + "`\nTime (Long): `" + vars.longTimeFormat() + "`\nTime (Short): `" + vars.shortTimeFormat() + "`", true)
                .addField("Identifiers", "`" + IdGenerator.getGeneratedIdsAmount() + "` identifiers generated", true)
                .addField("State", municipality.getState().getName() + " (" + municipality.getState().getAbbreviation() + ")", true)
                .addField("Bot Uptime", Time.getTimeDifference(System.currentTimeMillis(), Main.botStartTime, Time.TimeDiffStyle.COMPACT), true)
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
