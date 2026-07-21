package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.SingleObjectConfiguration;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ReloadConfig extends Command {

    public ReloadConfig() {
        super("reload-config", "Reloads the configuration files of FireGen.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "file", "Reload specific file.", false, true),
                                new OptionData(OptionType.STRING, "new-municipality", "Change the municipality to this one. NOTE: Will trigger a full reload!", false, true)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if (!Main.users.hasPermission(event.getUser(), net.noahf.firegen.discord.users.Permission.RELOAD_CONFIG)) {
            DiscordMessages.error(event, "You don't have permission to reload FireGen configuration files.");
            return;
        }

        event.deferReply(true).queue();
        OptionMapping newMunicipalityMapping = event.getOption("new-municipality");
        if (newMunicipalityMapping != null) {
            this.changeMunicipality(event, newMunicipalityMapping);
            return;
        }

        OptionMapping fileMapping = event.getOption("file");
        if (fileMapping != null) {
            this.reloadOneConfig(event, fileMapping);
            return;
        }

        this.reloadManyConfig(event);
    }

    private void changeMunicipality(IReplyCallback event, OptionMapping toMunicipalityMapping) {
        String fromMunicipality = Main.bot.getMunicipalityFolder();
        String toMunicipality = toMunicipalityMapping.getAsString();
        if (fromMunicipality.equalsIgnoreCase(toMunicipality)) {
            DiscordMessages.error(event, "The municipality is already set to `" + fromMunicipality + "`!");
            return;
        }

        try {
            Main.bot.changeMunicipality(toMunicipality);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            Main.bot.changeMunicipality(fromMunicipality);
            DiscordMessages.error(event, "An error occurred while changing municipalities from `" +
                    fromMunicipality + "` to `" + toMunicipality + "`, so the program will revert to " + fromMunicipality + "!",
                    exception
            );
            toMunicipality = fromMunicipality;
        }

        StringJoiner reloaded = new StringJoiner(", ");
        for (SingleObjectConfiguration<?> config : Main.config.getConfigs()) {
            reloaded.add("`" + config.getPath() + "`");
        }

        event.getHook().editOriginal("Changed the municipality to '" + toMunicipality + "' and loaded the configuration files: "
                + reloaded
        ).queue();
    }

    private void reloadOneConfig(IReplyCallback event, OptionMapping fileMapping) {
        String file = fileMapping.getAsString();
        SingleObjectConfiguration<?> configuration = Main.config.getConfigs().stream()
                .filter(s -> s.getPath().equalsIgnoreCase(file))
                .findFirst().orElse(null);
        if (configuration == null) {
            DiscordMessages.error(event, "A configuration does not exist at the path '" + file + "'.\n\n"
                    + "Valid paths are: `" + Main.config.getConfigs().stream()
                    .map(SingleObjectConfiguration::getPath)
                    .collect(Collectors.joining("`, `")) + " `"
            );
            return;
        }

        long start = System.currentTimeMillis();
        configuration.reload();

        event.getHook().editOriginal("Reloaded `" + configuration.getPath() + "` in " + (System.currentTimeMillis()-start) + "ms.").queue();
    }

    private String reloadManyConfig(IReplyCallback event) {
        long start = System.currentTimeMillis();
        StringJoiner reloaded = new StringJoiner(", ");
        for (SingleObjectConfiguration<?> config : Main.config.getConfigs()) {
            config.reload();
            reloaded.add("`" + config.getPath() + "`");
        }

        if (event != null)
            event.getHook().editOriginal(
                "Reloaded `" + Main.config.getConfigs().size() + "` configuration files in " + (System.currentTimeMillis()-start) + "ms, " +
                        "including files: " + reloaded
            ).queue();

        return reloaded.toString();
    }



    private List<String> municipalityFolders = null;

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("file")) {
            return new ArrayList<>(
                    Main.config.getConfigs().stream()
                            .map(SingleObjectConfiguration::getPath)
                            .sorted()
                            .toList()
            );
        }
        if (focused.getName().equalsIgnoreCase("new-municipality")) {
            if (municipalityFolders == null) {
                municipalityFolders = new ArrayList<>();
                this.findMunicipalities();
            }

            return municipalityFolders;
        }
        return null;
    }

    private void findMunicipalities() {
        File file = new File(Main.bot.getConfigPrefix());
        File[] listOfFiles = file.listFiles();
        if (!file.exists() || listOfFiles == null) {
            this.municipalityFolders = new ArrayList<>();
            return;
        }

        for (File f : listOfFiles) {
            if (!f.exists() || !f.isDirectory()) {
                continue;
            }

            File[] dirListFiles = f.listFiles();
            if (dirListFiles == null) {
                continue;
            }

            if (Arrays.stream(dirListFiles)
                    .noneMatch(d -> d.getName().equalsIgnoreCase("municipality.json"))
            ) {
                continue;
            }

            this.municipalityFolders.add(f.getName());
        }

        String current = Main.bot.getMunicipalityFolder();
        if (!this.municipalityFolders.contains(current)) {
            this.municipalityFolders.add(current);
        }
    }
}
