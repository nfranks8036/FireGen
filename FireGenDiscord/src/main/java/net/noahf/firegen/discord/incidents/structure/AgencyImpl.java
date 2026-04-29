package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class AgencyImpl implements net.noahf.firegen.api.incidents.units.Agency {

    private @Getter String shorthand;
    private @Getter String longhand;
    private String formatted;
    private @Getter Emoji emoji;
    private @Getter AgencyType type;
    private @Getter List<Unit> units;

    private @Getter SelectOption selectOption;

    @Override
    @NotNull
    public String toString() {
        return this.shorthand;
    }

    @Override
    public long getId() {
        return IdGenerator.generateAgencyId(this);
    }

    @Override
    public String getFormatted() {
        return "<:" + emoji.getAsReactionCode() + "> " + this.formatted;
    }
}
