package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.messaging.IncidentMessagingService;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.Log;
import org.hibernate.Session;

import java.awt.*;
import java.util.*;
import java.util.List;

public class UnitInfo extends Command {

    public UnitInfo() {
        super("unit-info", "Get information relating to a specific unit.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "unit", "The unit to get the information of.", true, true)
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

//        String a = unitMapping.getAsString();
//        Session session = Main.database.getFactory().openSession();
//        long id = Long.parseLong(a.substring(1).substring(6));
//        if (a.startsWith("f")) {
//            IncidentImpl i = session.find(IncidentImpl.class, id);
//            DiscordMessages.selfDestruct(event, 20, (i != null ? i.toString() : "null"));
//        } else if (a.startsWith("p")) {
//            IncidentImpl i = (IncidentImpl) Main.incidents.getIncidentBy(id);
//            if (i != null)
//                session.persist(i);
//            DiscordMessages.selfDestruct(event, 20, "Persisted------" + (i != null ? i.toString() : "null"));
//        } else {
//            DiscordMessages.selfDestruct(event, 5, "Invalid command: " + a.charAt(0));
//        }
//        session.close();
//
//        if (2 > 1) {
//            return;
//        }

        String unitString = unitMapping.getAsString();
        Unit iUnit = Main.incidents.getUnitByLonghand(unitString);
        if (iUnit == null) {
            iUnit = Main.incidents.getUnitByShorthand(unitString);
        }

        if (iUnit == null) {
            DiscordMessages.error(event, "The specified text, `" + unitString + "`, does not come back to a real unit.");
            return;
        }
        UnitImpl unit = (UnitImpl) iUnit;
        if (unit.isPlaceholder()) {
            DiscordMessages.error(event, "This unit is considered a 'placeholder', which means it's information cannot be viewed.");
            return;
        }

        Set<UnitAssignment> assignments = unit.getAssignments();

        List<MessageEmbed> returned = new ArrayList<>();

        returned.add(
                new EmbedBuilder()
                        .setColor(new Color(255, 90, 90))
                        .setTitle(unit.getLonghand())
                        .addField("Emoji", unit.getEmoji().getFormatted() + " (`:" + unit.getEmoji().getName() + ":`)", true)
                        .addField("Order", "#" + unit.ordinal(), true)
                        .addField("Agency", unit.getAgency().getTitle(), true)
                        .addField("Names",
                                "Short: `" + unit.getShorthand() + "`\n" +
                                        "Long: `" + unit.getLonghand() + "`\n" +
                                        "Formatted: " + unit.getFormatted()
                                , false)
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

        event.replyEmbeds(returned).setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("unit")) {
            return Main.incidents.getUnits().stream().map(u -> (UnitImpl) u).filter(u -> !u.isPlaceholder()).map(Unit::getLonghand).toList();
        }
        return null;
    }
}
