package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentEventImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitAssignmentImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.Time;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitInfo extends Command {

    public UnitInfo() {
        super("unit-info", "Get information relating to a specific unit.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "unit", "The unit to get the information of.", true, true),
                                new OptionData(OptionType.STRING, "incident", "The specific incident to view details of.", false, true)
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

        OptionMapping unitMapping = event.getOption("unit");
        if (unitMapping == null) {
            DiscordMessages.error(event, "You must specify a specific unit (see: /units).");
            return;
        }

        ConfigUnits configUnits = Main.config.get(ConfigUnits.class);
        String input = unitMapping.getAsString();
        String unitString = input.replace("-BYP", "");
        boolean bypassRestrictions = input.contains("-BYP");
        Unit iUnit = configUnits.getUnitByLonghand(unitString);
        if (iUnit == null) {
            iUnit = configUnits.getUnitByShorthand(unitString);
        }

        if (iUnit == null) {
            DiscordMessages.error(event, "The specified text, `" + unitString + "`, does not come back to a real unit.");
            return;
        }
        UnitImpl unit = (UnitImpl) iUnit;
        if (unit.isPlaceholder() && !bypassRestrictions) {
            DiscordMessages.error(event, "This unit is considered a 'placeholder', which means it's information cannot be viewed.\nYou may be able to view information about it by viewing its parent agency by using `/agency-info <agency>`.");
            return;
        }

        OptionMapping incidentMapping = event.getOption("incident");
        if (incidentMapping != null) {
            this.showUnitIncidentStatus(event, unit, incidentMapping);
            return;
        }

        this.showUnitOverallStatus(event, guild, unit);
    }

    private void showUnitIncidentStatus(IReplyCallback event, UnitImpl unit, OptionMapping incidentMapping) {
        final Pattern pattern = Pattern.compile("(\\d{4})-(\\d+)");
        final Matcher matcher = pattern.matcher(incidentMapping.getAsString());

        if (!matcher.find()) {
            DiscordMessages.error(event, "Unable to find an incident ID in your string. (YYYY-IIIIIII)");
            return;
        }

        String entireNumber = matcher.group(0);
        long incidentNumber = Long.parseLong(matcher.group(2));
        IncidentImpl incident = (IncidentImpl) Main.incidents.getIncidentBy(incidentNumber);
        if (incident == null) {
            DiscordMessages.error(event, "You have entered an incident ID that does not exist: '" + entireNumber + "'");
            return;
        }

        UnitAssignmentImpl assignment = (UnitAssignmentImpl) incident.getUnitAssignmentFor(unit);
        if (assignment == null) {
            DiscordMessages.error(event, "That unit, " + unit.getShorthand() + ", is not attached to the incident " + incident.getFormattedId());
            return;
        }

        AssignmentStatusImpl latest = (AssignmentStatusImpl) assignment.getLatestAssignment().getStatus();

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(210, 123, 69))
                .setTitle(unit.getEmoji().getFormatted() + " " + unit.getLonghand())
                .setDescription(
                        "**Incident:** `" + incident.getFormattedId() + "` (" + incident.getType().getSelectedName()
                        + (incident.getLocation().isSet() ? " @ " + incident.getLocation().format() : "") + ")\n" +
                                "**Unit:** `" + unit.getLonghand() + "` (" + unit.getAgency().getTitle() + ")\n"
                        + "**Status:** " + latest.getEmoji().getFormatted() + " " + latest.getName()
                );
        for (AssignmentEvent a : assignment.getAssignments()) {
            long unix = Time.getUnix(a.getTimestamp());
            AssignmentStatusImpl status = (AssignmentStatusImpl) a.getStatus();
            embed = embed
                    .addField(
                            status.getEmoji().getFormatted() + " " + status.getName(),
                            "Exact: <t:" + unix + ":f>\nAround: <t:" + unix + ":R>"
                            + (a.getSecondary() != null ? "\nSecondary: `" + a.getSecondary().getShortName() + "`" : "")
                            ,
                            true
                    );
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void showUnitOverallStatus(IReplyCallback event, Guild guild, UnitImpl unit) {
        Set<UnitAssignment> assignments = unit.getAssignments();

        List<MessageEmbed> returned = new ArrayList<>();

        returned.add(
                new EmbedBuilder()
                        .setColor(new Color(255, 90, 90))
                        .setAuthor("Unit View")
                        .setTitle(unit.getLonghand())
                        .addField("Emoji", unit.getEmoji().getFormatted() + " (`:" + unit.getEmoji().getName() + ":`)", true)
                        .addField("Order", "#" + unit.ordinal(), true)
                        .addField("Names",
                                "Short: `" + unit.getShorthand() + "`\n" +
                                        "Long: `" + unit.getLonghand() + "`\n" +
                                        "Formatted: " + unit.getFormatted()
                                , false)
                        .addField("Parent Agency", unit.getAgency().getTitle(), false)
                        .build()
        );

        if (!assignments.isEmpty()) {
            EmbedBuilder incidentInformation = new EmbedBuilder()
                    .setColor(new Color(90, 90, 255))
                    .setTitle("Associated Incidents (" + assignments.size() + ")");

            Map<String, String> statuses = new HashMap<>();
            for (UnitAssignment assignment : assignments) {
                IncidentImpl incident = (IncidentImpl) assignment.getIncident();
                AssignmentStatusImpl status = (AssignmentStatusImpl) assignment.getLatestAssignment().getStatus();

                ReceiveMessageSender sender = incident.getMessagingService().get(ReceiveMessageSender.class);
                String link = "*No incident link*";
                if (sender != null) {
                    link = sender.getMessages().stream()
                            .filter(Objects::nonNull)
                            .filter(m -> guild.getIdLong() == m.getGuildIdLong())
                            .map(DiscordMessages::createLink)
                            .findFirst()
                            .orElse(link);
                }

                String keyString = (status.getEmoji() != null ? status.getEmoji().getFormatted() + " " : "") +
                        "Unit is " + status.getName() + ":";
                String valueString = "- `" + incident.getFormattedId() + "` (" + incident.getType() + ") [" + link + "]";

                String existingValue = statuses.get(keyString);
                if (existingValue != null) {
                    valueString = existingValue + "\n" + valueString;
                }

                statuses.put(keyString, valueString);
            }

            for (Map.Entry<String, String> entry : statuses.entrySet()) {
                incidentInformation = incidentInformation
                        .addField(entry.getKey(), entry.getValue(), false);
            }

            returned.add(incidentInformation.build());
        }
//
//        returned.add(new EmbedBuilder()
//                .setTitle("Extra Information")
//                .addField("Unit Assignments:", unit.getAssignments().stream().map(Object::toString).collect(Collectors.joining("\n")), false)
//                .build()
//        );

        event.replyEmbeds(returned).setEphemeral(true).queue();
    }

    private final List<String> incidents = new ArrayList<>();
    private long lastUpdated = 0L;

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("unit")) {
            return Main.config.get(ConfigUnits.class).get().stream().map(u -> (UnitImpl) u).filter(u -> !u.isPlaceholder()).map(Unit::getLonghand).toList();
        }
        if (focused.getName().equalsIgnoreCase("incident")) {
            if (lastUpdated > System.currentTimeMillis()) {
                incidents.clear();
                lastUpdated = System.currentTimeMillis() + (20 * 1000L);
                incidents.addAll(
                        Main.incidents.getIncidents().stream()
                                .map(i ->
                                        i.getFormattedId() + ": " +
                                                i.getType().getSelectedName() +
                                                (i.getLocation().isSet() ? i.getLocation().format() : "")
                                )
                                .toList()
                );
            }
            return incidents;
        }
        return null;
    }
}
