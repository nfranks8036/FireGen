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
import net.noahf.firegen.discord.utilities.*;
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
    public static final int MAX_UNITS_TABLE = 16;

    private static final Map<Long, UnitsResponseType> useMobileMode = new ConcurrentHashMap<>();

    public Units() {
        super("units", "View the list of units that were most recently active on calls.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply(createReply(event.getMember()).asCreate())
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh")
                        )
                )
                .queue();
    }

    public static MessageGenericData createReply(Member user) {
        Set<UnitAssignment> assignments = Main.incidents.getAssignments();;

        if (assignments.isEmpty()) {
            return MessageGenericData.fromMessage(
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

        return type.apply(assignments);
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
            UnitsResponseType currentView = useMobileMode.getOrDefault(user.getIdLong(), UnitsResponseType.NOT_SET);

            try {
                event.editMessage(createReply(event.getMember()).asEdit())
                        .setComponents(
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh (Wait 5s)").asDisabled()
                                ),
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-changetype", "Refresh (Wait 5s)").asDisabled()
                                )
                        )
                        .complete().editOriginalComponents(
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asEnabled()
                                ),
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-changetype", "Change to " + ).asEnabled()
                                )
                        ).completeAfter(5, TimeUnit.SECONDS);
                ;
            } catch (Exception exception) {
                DiscordMessages.error(event, "An error occurred processing your button press", exception);
            }
        }
    }

}
