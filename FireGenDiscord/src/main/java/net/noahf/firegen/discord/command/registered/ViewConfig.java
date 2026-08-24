package net.noahf.firegen.discord.command.registered;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.InteractPage;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.model.Paginator;
import com.github.ygimenez.model.PaginatorBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.config.SingleObjectConfiguration;
import net.noahf.firegen.discord.utilities.Log;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ViewConfig extends Command {

    public ViewConfig() {
        super("view-config", "View a specific configuration file's options.",
                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "file", "The file to select.", true, true)
                        })
                        .finish()
                );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        OptionMapping fileMapping = event.getOption("file");
        if (fileMapping == null) {
            return;
        }
        String file = fileMapping.getAsString();

        @SuppressWarnings({"unchecked"})
        SingleObjectConfiguration<?> configuration =
                Main.config.getConfigs().stream()
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

        List<String> pagesStrings = new ArrayList<>();

        String entireMessage = configuration.get().toString();
        int amountPerPage = 0;
        if (configuration instanceof MultiObjectConfiguration<?> multi) {
            amountPerPage = 24;
            List<String> objects = new ArrayList<>(multi.get().stream().map(Object::toString).toList());
            for (int i = 0; i < Math.ceil((double) objects.size() / amountPerPage); i++) {
                List<String> sublist = objects.subList(i * amountPerPage, Math.min((i+1)*amountPerPage, objects.size()));
                pagesStrings.add(String.join("\n", sublist));
            }
        } else {
            amountPerPage = 1024;
            for (int i = 0; i < Math.ceil((double) entireMessage.length() / amountPerPage); i++) {
                pagesStrings.add(entireMessage.substring(i * amountPerPage, Math.min((i+1)*amountPerPage, entireMessage.length())));
            }
        }

        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < pagesStrings.size(); i++) {
            String pageString = pagesStrings.get(i);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("File `" + configuration.getPath() + "`:")
                    .setDescription(pageString)
                    .setColor(new Color(0, 50, 100));
            if (pagesStrings.size() > 1) {
                String pageText = "Page " + (i + 1) + " / " + pagesStrings.size();
                embed = embed
                        .setAuthor(pageText);
            }
            pages.add(InteractPage.of(embed.build()));
        }

        event.getHook()
                .editOriginalEmbeds(
                    (MessageEmbed) pages.getFirst().getContent()
                )
                .queue((s) -> {
                    if (pages.size() > 1) {
                        Pages.paginate(s, pages, true, true);
                    }
                });
    }

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
        return null;
    }
}
