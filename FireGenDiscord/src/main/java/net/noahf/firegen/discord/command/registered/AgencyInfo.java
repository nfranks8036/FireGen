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
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
        ConfigUnits configUnits = Main.config.get(ConfigUnits.class);
        Agency iAgency = configUnits.getAgencyByLonghand(agencyString);
        if (iAgency == null) {
            iAgency = configUnits.getAgencyByShorthand(agencyString);
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

                if (assignments.isEmpty()) {
                    continue;
                }

                List<String> formatted = new LinkedList<>();
                for (UnitAssignment a : assignments) {
                    UnitImpl unit = (UnitImpl) a.getUnit();
                    units.remove(unit);
                    String formattedStatus = unit.getFormattedStatus(a.getLatestAssignment());
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
                    + (embed.getFields().isEmpty() ? "" : "\n\nActive Incidents:")
            );

            returned.add(embed.build());
        }

        event.replyEmbeds(returned).setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("agency")) {
            return Main.config.get(ConfigUnits.class).getAgencies().stream().map(Agency::getTitle).toList();
        }
        return null;
    }
}
