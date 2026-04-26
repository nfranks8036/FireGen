package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@EqualsAndHashCode(of = {"id"})
public class ContributorImpl<T> implements Contributor<T> {

    public static Contributor<User> of(User user) {
        return new ContributorImpl<>(
                user.getIdLong(), user.getEffectiveName(), user.getName(), user
        );
    }

    private final long id;
    private final String displayName, name;
    private final T userObject;

    public ContributorImpl(long id, String displayName, String name, T userObject) {
        this.id = id;
        this.displayName = displayName;
        this.name = name;
        this.userObject = userObject;
    }

    @NotNull @Override
    public String toString() {
        return this.getDisplayName();
    }
}
