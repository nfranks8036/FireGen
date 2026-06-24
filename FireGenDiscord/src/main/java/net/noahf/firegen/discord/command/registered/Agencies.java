package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Agencies extends Command {

    public Agencies() {
        super(
                "agencies", "View the list of agencies that serve the area."
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        List<Agency> agencies = new ArrayList<>(Main.incidents.getAgencies());
        SystemMunicipality local = Main.incidents.getMunicipality();
        event.replyEmbeds(new EmbedBuilder()
                .setColor(new Color(128, 252, 222))
                .setTitle("Agencies (" + agencies.size() + ")")
                .setDescription(
                        "These agencies serve " + local.getName() + ", " + local.getState().getAbbreviation() + ":\n\n" +
                        agencies.stream().map(a -> (AgencyImpl) a).map(a ->
                                "- " + (a.getEmoji() != null ? a.getEmoji().getFormatted() + " " : "") + a.getFormatted()
                                + " (`" + a.getShorthand() + "`)"
                                ).collect(Collectors.joining("\n"))
                )
                .build()
        ).setEphemeral(true).queue();
    }

}
