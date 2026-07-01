package net.noahf.firegen.discord.utilities;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.utilities.ansi.AnsiTableBuilder;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.command.registered.Units.BLANK_EMOJI;

public enum UnitsResponseType {
    TABLE("Table View", assignments -> {
        AnsiTableBuilder table = new AnsiTableBuilder()
                .header("time", "unit", "status", "incident type", "incident location")
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
                        "Returned `" + (amount >= 25 ? "25`/`25" : amount) + "` unit status" + (amount == 1 ? "" : "es") + " from FireGen as of <t:" + refreshed + ":R>." +
                                "\n```ansi\n" + returned.getFirstElement() + "```",
                        Message.MAX_CONTENT_LENGTH, "``` *[unable to show any more content]*"
                )
        );
    }),


    EMBED("Embed View", assignments -> {
        EmbedBuilder returned = new EmbedBuilder()
                .setColor(new Color(58, 70, 98));
        Map<Incident, java.util.List<UnitAssignment>> incidentUnits = assignments.stream()
                .collect(Collectors.groupingBy(UnitAssignment::getIncident));

        for (Map.Entry<Incident, List<UnitAssignment>> entry : incidentUnits.entrySet()) {
            Incident incident = entry.getKey();

            String fieldTitle =
                    (incident.getType() != null ? incident.getType().getSelectedName() : " ")
                            + (incident.getLocation() != null && incident.getLocation().isSet()
                            ? " (" + ((IncidentLocationImpl)incident.getLocation()).getRequiredData(null) + ")"
                            : " "
                    );
            StringJoiner fieldDescription = new StringJoiner("\n");
            final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(Main.incidents.getFireGenVariables().longTimeFormat());

            for (UnitAssignment assignment : entry.getValue()) {
                AssignmentEvent status = assignment.getLatestAssignment();
                Unit unit = assignment.getUnit();
                Emoji emojiObj = ((AssignmentStatusImpl) status.getStatus()).getEmoji();
                String emoji = (emojiObj != null ? emojiObj.getFormatted() : BLANK_EMOJI);
                String timestamp = status.getTimestamp().format(TIME_FORMAT);

                fieldDescription.add(
                        emoji + " " + unit.getShorthand() + " @ `" + timestamp
                );
            }

            returned = returned
                    .addField(fieldTitle, fieldDescription.toString(), false);
        }

        return MessageGenericData.fromEmbed(returned.build());
    }),

    NOT_SET("<InvalidValue>", (i) -> null);


    @Getter private final String descriptor;
    private final Function<Set<UnitAssignment>, MessageGenericData> function;

    UnitsResponseType(String descriptor, Function<Set<UnitAssignment>, MessageGenericData> func) {
        this.descriptor = descriptor;
        this.function = func;
    }

    public MessageGenericData apply(Set<UnitAssignment> assignments) {
        return this.function.apply(assignments);
    }

    public UnitsResponseType nextOnList() {
        return switch (this) {
            case EMBED -> TABLE;
            case TABLE -> EMBED;
            default -> NOT_SET;
        };
    }
}
