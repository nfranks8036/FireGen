package net.noahf.firegen.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.discord.actions.ActionsManager;
import net.noahf.firegen.discord.actions.listeners.ButtonDetector;
import net.noahf.firegen.discord.actions.listeners.ModalDetector;
import net.noahf.firegen.discord.actions.listeners.StringSelectDetector;
import net.noahf.firegen.discord.command.CommandManager;
import net.noahf.firegen.discord.database.DatabaseManager;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.users.UserManager;
import net.noahf.firegen.discord.utilities.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String TOKEN = null;
    public static String MUNICIPALITY_FOLDER = null;

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JDA JDA;

    public static DatabaseManager database;
    public static CommandManager commands;
    public static IncidentManager incidents;
    public static ActionsManager actions;
    public static UserManager users;

    public static List<TextChannel> adminChannels = new ArrayList<>();
    public static List<TextChannel> receiveChannels = new ArrayList<>();

    public static final long botStartTime = System.currentTimeMillis();

    private static void loadChannels(JDA jda) {
//        receiveChannels.add(jda.getTextChannelById(1473433906681221200L)); // Personal - radio-activity-firewatch
//        adminChannels.add(jda.getTextChannelById(1497787094779822170L)); // Personal - admin-chat

        receiveChannels.add(jda.getTextChannelById(1492362595439611925L)); // BFD Tracker - subscriber
        adminChannels.add(jda.getTextChannelById(1492362581623439581L)); // BFD Tracker - management

        receiveChannels.add(jda.getTextChannelById(1436406051112226959L)); // FIREWATCH - radio-activity
        adminChannels.add(jda.getTextChannelById(1498910592822411334L)); // FIREWATCH - admin radio activity
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        try {
            TOKEN = getExternalInfo("TOKEN");
            MUNICIPALITY_FOLDER = getExternalInfo("MUNICIPALITY");
        } catch (Exception exception) {
            Log.error("Failed to find token from environment: " + exception, exception);
        }

        Log.info("Checking for required information...");
        assertPropertyNotNull("TOKEN", TOKEN);
        assertPropertyNotNull("MUNICIPALITY", MUNICIPALITY_FOLDER);

        Log.info("Building JDA...");
        Log.info("-".repeat(20) + " [JDA START] " + "-".repeat(20));
        JDA = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.customStatus("Listening to the radio"))
                .setStatus(OnlineStatus.ONLINE)
                .disableCache(
                        CacheFlag.SCHEDULED_EVENTS, CacheFlag.VOICE_STATE
                )
                .setEnabledIntents(
                        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EXPRESSIONS
                )
                .addEventListeners(new ButtonDetector(), new ModalDetector(), new StringSelectDetector())
                .build()
                .awaitReady();
        loadChannels(JDA);
        Log.info("-".repeat(20) + " [ JDA END ] " + "-".repeat(20));

        Log.info("Importing structure data from municipality '" + MUNICIPALITY_FOLDER + "'");
        database = new DatabaseManager();
        incidents = new IncidentManager();
        actions = new ActionsManager();
        commands = new CommandManager();
        users = new UserManager(JDA, incidents);
//        subscribers = new SubscriberManager();

        String status = getStatus(incidents.getMunicipality());
        JDA.getPresence().setActivity(Activity.customStatus(status));
        Log.info("Set bot status to '" + status + "'");

        Log.info("Started in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private static String getExternalInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }

    private static void assertPropertyNotNull(String objectName, Object object) {
        if (object == null) {
            throw new RuntimeException("Cannot find " + objectName + " (" + objectName + " = null). Try setting an environmental variable or property and re-run the program.");
        }
    }

    private static String getStatus(SystemMunicipality municipality) {
        final String PRIMARY_TEXT = "Listening to the radio";
        String status = PRIMARY_TEXT + " in " + municipality.getName();

        if (status.length() > Activity.MAX_ACTIVITY_NAME_LENGTH) {
            status = PRIMARY_TEXT + " in " + municipality.getShortName();
        }

        if (status.length() > Activity.MAX_ACTIVITY_NAME_LENGTH) {
            return PRIMARY_TEXT;
        }

        return status;
    }

}
