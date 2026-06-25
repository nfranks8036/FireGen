package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl_;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitAssignmentImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgencyInfo extends Command {

    private static final Function<? super Unit, String> DELETE_EMOJI = u -> u.getFormatted().replaceAll("<:[A-Za-z_]+:\\d+>\\s", "");

    public AgencyInfo() {
        super(
                "agency-info", "Gets information relating to a specific agency.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "agency", "The agency to get the information of.", true, true)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            DiscordMessages.error(event, "This command requires a guild to work.");
            return;
        }

        OptionMapping agencyMapping = event.getOption("agency");
        if (agencyMapping == null) {
            DiscordMessages.error(event, "You must specify a specific agency (see: /agencies).");
            return;
        }

        String agencyString = agencyMapping.getAsString();
        Agency iAgency = Main.incidents.getAgencyByLonghand(agencyString);
        if (iAgency == null) {
            iAgency = Main.incidents.getAgencyByShorthand(agencyString);
        }

        if (iAgency == null) {
            DiscordMessages.error(event, "The specified text, `" + agencyString + "`, does not come back to a real agency.");
            return;
        }

        AgencyImpl agency = (AgencyImpl) iAgency;

        List<MessageEmbed> returned = new ArrayList<>();

        returned.add(new EmbedBuilder()
                .setColor(new Color(200, 200, 0))
                .setAuthor("Agency View")
                .setTitle(agency.getTitle())
                .addField("Emoji", agency.getEmoji().getFormatted() + " (`:" + agency.getEmoji().getName() + ":`)", true)
                .addField("Order", "#" + agency.ordinal(), true)
                .addField("Type", agency.getType().name(), true)
                .addField("Names",
                        "Short: `" + agency.getShorthand() + "`\n" +
                                "Long: `" + agency.getTitle() + "`\n" +
                                "Formatted: " + agency.getFormatted(),
                        false
                        )
                .addField("Station", agency.getStation(), false)
                .build());

        List<Unit> units = new ArrayList<>(agency.getUnits());
        if (!units.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(new Color(255, 98, 0))
                    .setTitle("Units (" + agency.getUnits().size() + ")");

            List<IncidentImpl> activeIncidents = Main.incidents.getIncidents().stream()
                    .filter(i -> i.getStatus().isInProgress())
                    .map(i -> (IncidentImpl) i)
                    .toList();

            for (IncidentImpl i : activeIncidents) {
                List<UnitAssignment> assignments = units.stream()
                        .filter(u -> u.getAgency().equals(agency))
                        .map(i::getUnitAssignmentFor)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList();

                List<String> formatted = new LinkedList<>();
                for (UnitAssignment a : assignments) {
                    UnitImpl unit = (UnitImpl) a.getUnit();
                    String formattedStatus = unit.getFormattedStatus((AssignmentStatusImpl) a.getLatestAssignment().getStatus());
                    formatted.add(formattedStatus);
                }

                embed = embed.addField(
                        i.getType() + (i.getLocation().isSet() ? " @ " + i.getLocation().format() : ""),
                        String.join(", ", formatted),
                        false
                );
            }

            embed.setDescription(units.stream()
                    .map(u -> (UnitImpl) u)
                    .map(DELETE_EMOJI)
                    .collect(Collectors.joining(", "))
            );

            returned.add(embed.build());
        }

        event.replyEmbeds(returned).setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("agency")) {
            return Main.incidents.getAgencies().stream().map(Agency::getTitle).toList();
        }
        return null;
    }
}
