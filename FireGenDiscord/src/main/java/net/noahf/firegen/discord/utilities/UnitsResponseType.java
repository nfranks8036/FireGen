package net.noahf.firegen.discord.utilities;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import net.noahf.firegen.discord.utilities.ansi.AnsiTableBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.command.registered.Units.*;

public enum UnitsResponseType {
    TABLE("Table View", assignments -> {
        AnsiTableBuilder table = new AnsiTableBuilder()
                .header("time", "unit", "status", "incident type", "incident location")
                .maximumRows(MAX_UNIT_ROWS_TABLE)
                .disallowDuplicateOnColumn(1);
        long refreshed = Time.getUnix();
        for (UnitAssignment assignment : assignments) {
            Unit unit = assignment.getUnit();
            Incident incident = assignment.getIncident();
            AssignmentEvent e = assignment.getLatestAssignment();
            LocalDateTime time = e.getTimestamp();
            AssignmentStatusImpl status = (AssignmentStatusImpl) e.getStatus();

            table = table
                    .row(time.toEpochSecond(ZoneOffset.UTC), status.getAnsiColor(),
                            time.format(DateTimeFormatter.ofPattern(
                                    Main.incidents.getFireGenVariables().dateFormat() + " " +
                                            Main.incidents.getFireGenVariables().longTimeFormat()
                            )),
                            unit.getShorthand(),
                            status.getName(),
                            DiscordMessages.truncate(incident.getType().toString(), 21, "..."),
                            DiscordMessages.truncate(
                                    ((IncidentLocationImpl)incident.getLocation()).getRequiredData(null),
                                    24, "..."
                            )
                    );
        }

        ImmutablePair<String, Integer> returned = table.build(25);
        int amount = returned.getSecondElement();

        return MessageGenericData.fromMessage(
                DiscordMessages.truncate(
                        "Returned `" + (amount >= MAX_UNIT_ROWS_TABLE ? MAX_UNIT_ROWS_TABLE + "`/`" + MAX_UNIT_ROWS_TABLE : amount) + "` unit status" + (amount == 1 ? "" : "es") + " from FireGen as of <t:" + refreshed + ":R>." +
                                "\n```ansi\n" + returned.getFirstElement() + "```",
                        Message.MAX_CONTENT_LENGTH, "``` *[unable to show any more content]*"
                )
        );
    },
            () -> MessageGenericData.fromMessage(
                    "Returned `0` unit statuses from FireGen as of <t:" + Time.getUnix() + ":R>.\n" +
                            "```ansi\n" + AnsiColor.BACKGROUND_RED.wrap("No units have been assigned to any incidents right now. Try again later.") +
                            "```\n"
            )
            ),


    EMBED("Embed View", assignments -> {
        EmbedBuilder returned = new EmbedBuilder()
                .setColor(new Color(58, 70, 98));
        Map<Incident, java.util.List<UnitAssignment>> incidentUnits = assignments.stream()
                .collect(Collectors.groupingBy(UnitAssignment::getIncident));

        int unitAmounts = 0;
        for (Map.Entry<Incident, List<UnitAssignment>> entry : incidentUnits.entrySet()) {
            if (unitAmounts > MAX_UNIT_ROWS_EMBED) {
                break;
            }

            Incident incident = entry.getKey();

            String fieldTitle =
                    (incident.getType() != null ? incident.getType().getSelectedName() : " ")
                            + (incident.getLocation() != null && incident.getLocation().isSet()
                            ? " (" + ((IncidentLocationImpl)incident.getLocation()).getRequiredData(null) + ")"
                            : " "
                    );
            StringJoiner fieldDescription = new StringJoiner("\n");
            final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(Main.incidents.getFireGenVariables().longTimeFormat());

            List<UnitAssignment> sortedAssignments = entry.getValue();
            sortedAssignments.sort(Comparator.comparing(o -> o.getLatestAssignment().getTimestamp()));
            for (UnitAssignment assignment : sortedAssignments.reversed()) {
                if (unitAmounts > MAX_UNIT_ROWS_EMBED) {
                    break;
                }

                if (fieldDescription.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                    break;
                }

                AssignmentEvent status = assignment.getLatestAssignment();
                UnitImpl unit = (UnitImpl) assignment.getUnit();
                Emoji emojiObj = ((AssignmentStatusImpl) status.getStatus()).getEmoji();
                String emoji = (emojiObj != null ? emojiObj.getFormatted() : BLANK_EMOJI);
                String timestamp = status.getTimestamp().format(TIME_FORMAT);

                String next = emoji + " " + (unit.getEmoji() != null ? unit.getEmoji().getFormatted() + " " : "") + unit.getShorthand() + " @ `" + timestamp + "`";
                if (fieldDescription.length() + next.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                    break;
                }

                fieldDescription.add(next);
                unitAmounts++;
            }

            returned = returned
                    .addField(fieldTitle, fieldDescription.toString(), false);
        }

        returned.setDescription("Returned `" + unitAmounts + "` unit status" + (unitAmounts == 1 ? "" : "es") + " from FireGen as of <t:" + Time.getUnix() + ":R>");

        return MessageGenericData.fromEmbed(returned.build());
    },
            () -> MessageGenericData.fromEmbed(new EmbedBuilder()
                            .setDescription("Returned `0` unit statuses from FireGen as of <t:" + Time.getUnix() + ":R>.")
                            .setFooter("No units have been assigned to any incidents right now. Try again later.")
                    .build())
            ),

    NOT_SET("<InvalidValue>", (i) -> null, () -> MessageGenericData.fromMessage("<NoData>"));


    @Getter private final String descriptor;
    private final Function<Set<UnitAssignment>, MessageGenericData> function;
    private final Supplier<MessageGenericData> noDataFunction;

    UnitsResponseType(String descriptor, Function<Set<UnitAssignment>, MessageGenericData> data, Supplier<MessageGenericData> noData) {
        this.descriptor = descriptor;
        this.function = data;
        this.noDataFunction = noData;
    }

    public MessageGenericData applyData(Set<UnitAssignment> assignments) {
        return this.function.apply(assignments);
    }

    public MessageGenericData applyNone() {
        return this.noDataFunction.get();
    }

    public UnitsResponseType nextOnList() {
        return switch (this) {
            case EMBED -> TABLE;
            case TABLE -> EMBED;
            default -> NOT_SET;
        };
    }
}
