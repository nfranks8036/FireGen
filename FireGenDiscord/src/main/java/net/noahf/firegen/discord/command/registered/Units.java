package net.noahf.firegen.discord.command.registered;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import net.noahf.firegen.discord.utilities.ansi.AnsiTableBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Units extends Command {

    public static final String BLANK_EMOJI = " ".repeat(7);

    private static final Map<Long, UnitsResponseType> useMobileMode = new ConcurrentHashMap<>();

    @AllArgsConstructor
    @Getter
    public enum UnitsResponseType {
        TABLE(assignments -> {
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

            return MessageCreateData.fromContent(
                    DiscordMessages.truncate(
                            "Returned `" + (amount >= 25 ? "25`/`25" : amount) + "` unit status" + (amount == 1 ? "" : "es") + " from FireGen as of <t:" + refreshed + ":R>." +
                                    "\n```ansi\n" + returned.getFirstElement() + "```",
                            Message.MAX_CONTENT_LENGTH, "``` *[unable to show any more content]*"
                    )
            );
        }),


        EMBED(assignments -> {
            EmbedBuilder returned = new EmbedBuilder()
                    .setColor(new Color(58, 70, 98));
            Map<Incident, List<UnitAssignment>> incidentUnits = assignments.stream()
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

            return MessageDatreturned.build());
        }),

        NOT_SET((i) -> null);

        private final Function<Set<UnitAssignment>, MessageData> function;
    }

    public Units() {
        super("units", "View the list of units that were most recently active on calls.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply(createReply(event.getMember()))
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh")
                        )
                )
                .queue();
    }

    public static MessageData createReply(Member user) {
        Set<UnitAssignment> assignments = Main.incidents.getAssignments();;

        if (assignments.isEmpty()) {
            return MessageCreateData.fromContent(
                    "Returned `0` unit statuses from FireGen as of <t:" + Time.getUnix() + ":R>.\n" +
                            "```ansi\n" + AnsiColor.BACKGROUND_RED.wrap("No units have been assigned to any incidents right now. Try again later.") +
                            "```\n"
            );
        }

        UnitsResponseType type = UnitsResponseType.TABLE;

        if (user != null) {
            UnitsResponseType requested = useMobileMode.getOrDefault(user.getIdLong(), UnitsResponseType.NOT_SET);

            if (requested == UnitsResponseType.NOT_SET
                    && user.getOnlineStatus(ClientType.MOBILE) == OnlineStatus.ONLINE
            ) {
                type = UnitsResponseType.EMBED;
            }

            type = switch (requested) {
                case EMBED -> UnitsResponseType.EMBED;
                case TABLE -> UnitsResponseType.TABLE;
                default -> {
                    useMobileMode.put(user.getIdLong(), type);
                    yield type;
                }
            };
        }

        return type.getFunction().apply(assignments);
    }

    public static class UnitsRefreshButtonDetector extends ListenerAdapter {

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            String id = event.getComponentId();
            User user = event.getUser();

            if (!id.startsWith("firegenuser") || !id.endsWith("refreshunits")) {
                return;
            }

            Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");

            try {
                event.editMessage(createReply(event.getMember()))
                        .setComponents(
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh (Wait 5s)").asDisabled()
                                )
                        )
                        .complete().editOriginalComponents(
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asEnabled()
                                )
                        ).completeAfter(5, TimeUnit.SECONDS);
                ;
            } catch (Exception exception) {
                DiscordMessages.error(event, "An error occurred processing your button press", exception);
            }
        }
    }

}
