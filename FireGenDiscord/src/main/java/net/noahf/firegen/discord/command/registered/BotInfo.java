package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.utilities.Time;

import java.awt.*;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Properties;

public class BotInfo extends Command {

    public BotInfo() {
        super("bot-info", "View information about the Discord bot for FireGen.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Properties properties = (Properties) Main.bot.getProperties();
        long unix = Long.parseLong(properties.getProperty("buildTimeEpoch")) / 1000L;
        String locale = properties.getProperty("builderLocale");

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Bot Info (" + properties.getProperty("project") + " build)")
                .addField("Build Time", "<t:" + unix + ":f> (<t:" + unix + ":R>) (`" + properties.getProperty("buildTimezone") + "`)", true)
                .addField("Build Information",
                        "OS: `" + properties.getProperty("builderOs") + "` (`" + properties.getProperty("builderVersion") + "`) (`" + properties.getProperty("builderArch") + "`)\n" +
                                "Java: `" + properties.getProperty("currentJavaVersion") + "` by `" + properties.getProperty("currentJavaVendor") + "`\n" +
                                "Locale: `" + Locale.availableLocales()
                                .filter(l -> l.toLanguageTag().toLowerCase().replace("-", "_").equalsIgnoreCase(locale))
                                .findFirst().orElse(Locale.of("(unknown '" + locale + "')"))
                                .getDisplayName()
                                + "`\n" +
                                "Build Tool: Gradle `v" + properties.getProperty("builderGradleVersion") + "`, Groovy `v" + properties.getProperty("builderGroovyVersion") + "`"
                        , true)
                .addField("Classes",
                        "Group: `" + properties.getProperty("group") + "`\n" +
                                "Name: `" + properties.getProperty("name") + "`\n" +
                                "Version: `" + properties.getProperty("version") + "`",
                        true
                        )
                        .setColor(new Color(236, 209, 115))
                .build(),
                new EmbedBuilder()
                        .setTitle("Bot Info (" + properties.getProperty("project") + " current)")
                        .addField("Current Time", "<t:" + Time.getUnix() + ":f> (<t:" + Time.getUnix() + ":R>) (`" + ZoneId.systemDefault().getId() + "`)", true)
                        .addField("Current Information",
                                "OS: `" + System.getProperty("os.name") + "` (`" + System.getProperty("os.version") + "`) (`" + System.getProperty("os.arch") + "`)\n" +
                                        "Java: `" + System.getProperty("java.version") + "` by `" + System.getProperty("java.vendor") + "`\n" +
                                        "Locale: `" + Locale.getDefault().getDisplayName() + "`",
                                true
                                )
                        .addField("Uptime",
                                "The bot has been online for `" + Time.getTimeDifference(System.currentTimeMillis(), Main.botStartTime, Time.TimeDiffStyle.COMPACT) + "`",
                                true
                                )
                        .setColor(
                                new Color(100, 200, 100)
                        )
                        .build()
        ).setEphemeral(true).queue();
    }
}
