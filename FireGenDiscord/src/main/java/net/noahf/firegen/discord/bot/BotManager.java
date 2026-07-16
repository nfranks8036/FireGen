package net.noahf.firegen.discord.bot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.noahf.firegen.discord.actions.listeners.ButtonDetector;
import net.noahf.firegen.discord.actions.listeners.ContextMenuDetector;
import net.noahf.firegen.discord.actions.listeners.ModalDetector;
import net.noahf.firegen.discord.actions.listeners.StringSelectDetector;
import net.noahf.firegen.discord.bot.channels.ChannelManager;
import net.noahf.firegen.discord.command.registered.Units;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("FieldCanBeLocal")
public class BotManager extends Manager<BotManager> {

    public static final String BUILD_INFO_FILE_NAME = "FireGenDiscord-build.properties";

    private String token = null;
    private @Getter String municipalityFolder = null;
    private @Getter String configPrefix = null;

    @Getter
    ChannelManager channelManager;

    private JDA jda;

    private @Getter final Map<Object, Object> properties;

    public BotManager() {
        super(BotManager.class, "BotManager");

        this.properties = new Properties();
        this.channelManager = new ChannelManager(this);
        try (InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream(BUILD_INFO_FILE_NAME)
        ) {
            ((Properties)this.properties).load(input);
        } catch (Exception exception) {
            Log.error("Failed to import Bot information from " + BUILD_INFO_FILE_NAME, exception);
        }

        Properties prop = (Properties) this.properties;
        Log.info("FireGen environment: " + System.getProperty("os.name") + " (v" + System.getProperty("os.version") + ") and Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        Log.info("FireGen compiled on: " + prop.getProperty("builderOs") + " (v" + prop.getProperty("builderVersion") + ") and Java " + prop.getProperty("currentJavaVersion") + " (" + prop.getProperty("currentJavaVendor") + ")");

        try {
            token = getExternalInfo("TOKEN");
            municipalityFolder = getExternalInfo("MUNICIPALITY");
            configPrefix = getExternalInfo("CONFIG_PREFIX");
        } catch (Exception exception) {
            Log.error("Failed to find token from environment: " + exception, exception);
        }

        Log.info("Checking for required information...");
        assertPropertyNotNull("TOKEN", token);
        assertPropertyNotNull("MUNICIPALITY", municipalityFolder);

        configPrefix = getOr(configPrefix, "");
    }

    public BotManager startJda() throws InterruptedException {
        Log.info("Building JDA...");
        Log.info("-".repeat(20) + " [JDA START] " + "-".repeat(20));
        this.jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Listening to the radio"))
                .setStatus(OnlineStatus.ONLINE)
                .disableCache(
                        CacheFlag.SCHEDULED_EVENTS, CacheFlag.VOICE_STATE
                )
                .setEnabledIntents(
                        GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EXPRESSIONS
                )
                .addEventListeners(
                        new ButtonDetector(), new ModalDetector(),
                        new StringSelectDetector(), new Units.UnitsRefreshButtonDetector(),
                        new ContextMenuDetector()
                )
                .build()
                .awaitReady();
        this.channelManager.startImport();
        Log.info("-".repeat(20) + " [ JDA END ] " + "-".repeat(20));
        return this;
    }

    private String getExternalInfo(String key) {
        return (System.getenv().getOrDefault(key, System.getProperty(key)));
    }

    private void assertPropertyNotNull(String objectName, Object object) {
        if (object == null) {
            throw new RuntimeException("Cannot find " + objectName + " (" + objectName + " = null). Try setting an environmental variable or property and re-run the program.");
        }
    }

    private <T> T getOr(T object, T def) {
        if (object == null) {
            return def;
        }
        return object;
    }

    public JDA jda() {
        return this.jda;
    }

}
