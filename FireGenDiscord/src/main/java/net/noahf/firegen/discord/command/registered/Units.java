package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.utilities.*;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Units extends Command {

    public static final String BLANK_EMOJI = " ".repeat(7);

    private static final Map<Long, UnitsResponseType> userIdToViewType = new ConcurrentHashMap<>();

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
            UnitsResponseType requested = userIdToViewType.getOrDefault(user.getIdLong(), UnitsResponseType.NOT_SET);

            if (requested == UnitsResponseType.NOT_SET
                    && user.getOnlineStatus(ClientType.MOBILE) == OnlineStatus.ONLINE
            ) {
                type = UnitsResponseType.EMBED;
            }

            type = switch (requested) {
                case EMBED -> UnitsResponseType.EMBED;
                case TABLE -> UnitsResponseType.TABLE;
                default -> {
                    userIdToViewType.put(user.getIdLong(), type);
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

            if (!id.startsWith("firegenuser")
                    || !id.endsWith("refreshunits")
                    || !id.endsWith("changetype")
            ) {
                return;
            }

            Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");
            UnitsResponseType currentView = userIdToViewType.getOrDefault(user.getIdLong(), UnitsResponseType.NOT_SET);
            if (id.endsWith("changetype")) {
                currentView = currentView.nextOnList();
                userIdToViewType.put(user.getIdLong(), currentView);
            }

            String changeText = "Change to " + currentView.nextOnList().getDescriptor();

            try {
                event.editMessage(createReply(event.getMember()).asEdit())
                        .setComponents(
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh (Wait 5s)").asDisabled()
                                ),
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-changetype", changeText).asDisabled()
                                )
                        )
                        .complete().editOriginalComponents(
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asEnabled()
                                ),
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-changetype", changeText).asEnabled()
                                )
                        ).completeAfter(5, TimeUnit.SECONDS);
                ;
            } catch (Exception exception) {
                DiscordMessages.error(event, "An error occurred processing your button press", exception);
            }
        }
    }

}
