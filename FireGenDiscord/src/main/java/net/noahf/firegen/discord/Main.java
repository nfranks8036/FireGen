package net.noahf.firegen.discord;

import net.dv8tion.jda.api.entities.Activity;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.discord.actions.ActionsManager;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.command.CommandManager;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.ConfigMunicipality;
import net.noahf.firegen.discord.database.DatabaseManager;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.users.UserManager;
import net.noahf.firegen.discord.utilities.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static BotManager bot;
    public static ConfigManager config;
    public static DatabaseManager database;
    public static CommandManager commands;
    public static IncidentManager incidents;
    public static ActionsManager actions;
    public static UserManager users;

    public static final long botStartTime = System.currentTimeMillis();

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        bot = new BotManager().startJda();
        config = new ConfigManager(bot).startImport();
        database = new DatabaseManager();
        incidents = new IncidentManager(config);
        actions = new ActionsManager();
        commands = new CommandManager(bot.jda());
        users = new UserManager(bot.jda(), config);
//        subscribers = new SubscriberManager();

        String status = getStatus(config.get(ConfigMunicipality.class).get());
        bot.jda().getPresence().setActivity(Activity.customStatus(status));
        Log.info("Set bot status to '" + status + "'");

        Log.info("Started in " + (System.currentTimeMillis() - start) + "ms!");
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
