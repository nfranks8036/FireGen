package net.noahf.firegen.discord;

import com.github.ygimenez.exception.InvalidHandlerException;
import lombok.Getter;
import net.noahf.firegen.discord.actions.ActionsManager;
import net.noahf.firegen.discord.bot.BotManager;
import net.noahf.firegen.discord.command.CommandManager;
import net.noahf.firegen.discord.config.ConfigManager;
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

    private static @Getter boolean loading = true;

    public static void main(String[] args) throws InterruptedException, InvalidHandlerException {
        long start = System.currentTimeMillis();

        bot = new BotManager().startJda();
        config = new ConfigManager(bot).startImport();
        database = new DatabaseManager();
        incidents = new IncidentManager(config);
        actions = new ActionsManager();
        commands = new CommandManager(bot.jda());
        users = new UserManager(bot.jda(), config);
//        subscribers = new SubscriberManager();

        bot.setStatus();
        loading = false;

        Log.info("Started in " + (System.currentTimeMillis() - start) + "ms!");
    }

}
