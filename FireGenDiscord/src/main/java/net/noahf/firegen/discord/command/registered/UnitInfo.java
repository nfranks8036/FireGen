package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.incidents.units.UnitType;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigUnitTypes;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.messaging.ReceiveMessageSender;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitAssignmentImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.noahf.firegen.discord.users.FireGenUser.createId;

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
        Unit iUnit = configUnits.fromLonghand(unitString);
        if (iUnit == null) {
            iUnit = configUnits.fromShorthand(unitString);
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
            showUnitIncidentStatus(event, unit, incidentMapping.getAsString());
            return;
        }

        showUnitOverallStatus(event, guild, unit);
    }

    private static void showUnitIncidentStatus(IReplyCallback event, UnitImpl unit, String incidentStr) {
        final Pattern pattern = Pattern.compile("(\\d{4})-(\\d+)");
        final Matcher matcher = pattern.matcher(incidentStr);

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
                .setAuthor("Unit Incident Status View")
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

    private static void showUnitOverallStatus(IReplyCallback event, Guild guild, UnitImpl unit) {
        Set<UnitAssignment> assignments = unit.getAssignments();

        List<MessageEmbed> returned = new ArrayList<>();
        List<MessageTopLevelComponent> components = new ArrayList<>();

        String id = unit.getType().getId();
        if (!id.startsWith("$")) {
            String article = "a";
            if (List.of('a','e','i','o','u').contains(id.toLowerCase().charAt(0))) {
                article = "an";
            }
            components.add(ActionRow.of(
                    Button.secondary("firegenuser-" + event.getUser().getId() + "-explain-" + id,
                            "What is " + article + " " + id.replace("_", " ") + " unit?"
                    )
            ));
        }

        returned.add(
                new EmbedBuilder()
                        .setColor(new Color(255, 90, 90))
                        .setAuthor("Unit View")
                        .setTitle(unit.getLonghand())
                        .addField("Emoji", unit.getEmoji().getFormatted() + " (`:" + unit.getEmoji().getName() + ":`)", true)
                        .addField("Order", "#" + unit.ordinal(), true)
                        .addField("Unit Type (#)", unit.getType().getId().replace("_", " ") + (unit.getNumber() != Integer.MIN_VALUE ? " (" + unit.getNumber() + ")" : ""), true)
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
                    .setTitle("Associated Incidents (" + assignments.size() + ")")
                    .setFooter("Press an incident below to view more information about this unit and that incident.");

            List<SelectOption> options = new ArrayList<>();
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

                options.add(
                        SelectOption.of(DiscordMessages.truncate(incident.getFormattedId() + ": " + incident.getType().getSelectedName()
                                                + (incident.getLocation().isSet() ? " @ " + incident.getLocation().format() : ""), SelectOption.LABEL_MAX_LENGTH, "..."),
                                createId(event.getUser(), "unitselectinfo", String.valueOf(incident.getId()), String.valueOf(unit.getShorthand()))
                        )
                                .withEmoji(status.getEmoji())
                );
            }

            for (Map.Entry<String, String> entry : statuses.entrySet()) {
                incidentInformation = incidentInformation
                        .addField(entry.getKey(), entry.getValue(), false);
            }

            returned.add(incidentInformation.build());
            components.add(ActionRow.of(
                    StringSelectMenu.create(createId(event.getUser(), "unitselectinfo"))
                            .addOptions(options)
                            .build()
            ));
        }
//
//        returned.add(new EmbedBuilder()
//                .setTitle("Extra Information")
//                .addField("Unit Assignments:", unit.getAssignments().stream().map(Object::toString).collect(Collectors.joining("\n")), false)
//                .build()
//        );

        event.replyEmbeds(returned)
                .setComponents(components)
                .setEphemeral(true)
                .queue();
    }

    private final List<String> incidents = new ArrayList<>();
    private long lastUpdated = Long.MAX_VALUE;

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
                                                (i.getLocation().isSet() ? " @ " + i.getLocation().format() : "")
                                )
                                .map(s -> DiscordMessages.truncate(s, 100, "..."))
                                .toList()
                );
            }
            return incidents;
        }
        return null;
    }

    public static class UnitInfoDetailsListener extends ListenerAdapter {
        @Override
        public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
            String button = event.getComponentId();
            Log.info(event.getUser().getName() + " (" + event.getUser().getId() + ") pressed button '" + button + "'");
            try {
                if (!button.startsWith("firegenuser")) {
                    return;
                }

                if (!button.split("-")[2].equalsIgnoreCase("explain")) {
                    return;
                }

                String typeStr = button.split("-")[3];
                UnitType type = Main.config.get(ConfigUnitTypes.class).fromId(typeStr);
                if (type == null) {
                    DiscordMessages.error(event, "That UnitType does not exist: " + typeStr);
                    return;
                }

                String names = Stream.of(
                        "ID: `" + type.getId() + "`",
                        "Short: `" + type.getShorthand() + "`",
                        "Long: `" + type.getLonghand() + "`",
                        "Format: `" + type.getFormatted() + "`"
                        ).filter(s -> !s.contains("null"))
                        .collect(Collectors.joining("\n"));
                event.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(new Color(100, 180, 100))
                                .setAuthor("Unit Type View")
                                .setTitle(type.getId().replace("_", " "))
                                .setDescription(type.getDescription())
                                .addField("Names", names, false)
                                .build()
                ).setEphemeral(true).queue();
            } catch (Exception exception) {
                DiscordMessages.error(event, "Can't display the UnitType information.", exception);
            }
        }

        @Override
        public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {
            String input = event.getSelectedOptions().getFirst().getValue();
            try {
                String command = input.split("-")[2];
                if (!command.equalsIgnoreCase("unitselectinfo")) {
                    return;
                }

                String incidentStr = input.split("-")[3];
                Incident incident = Main.incidents.getIncidentBy(Long.parseLong(incidentStr));
                if (incident == null) {
                    throw new IllegalArgumentException("No incident exists by the ID '" + incidentStr + "'");
                }

                String unitStr = input.split("-")[4];
                Unit unit = Main.config.get(ConfigUnits.class).fromShorthand(unitStr);
                if (unit == null) {
                    throw new IllegalArgumentException("No unit exists by the shorthand '" + unitStr + "'");
                }

                showUnitIncidentStatus(event, (UnitImpl) unit, incident.getFormattedId());
            } catch (Exception exception) {
                Log.error("Button press error with input: \"" + input + "\"");
                DiscordMessages.error(event, "An error occurred while processing your Unit Info button press: " + exception, exception);
            }
        }
    }
}
