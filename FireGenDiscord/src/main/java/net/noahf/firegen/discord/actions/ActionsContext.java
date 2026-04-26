package net.noahf.firegen.discord.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.incidents.IncidentManager;

import java.util.List;

/**
 * Represents a context in which a {@link FireGenAction} occurs in.
 * @see ActionsContext#getManager()
 * @see ActionsContext#getIncident()
 * @see ActionsContext#getCommand()
 * @see ActionsContext#getParameters()
 */
@Getter
@AllArgsConstructor
public class ActionsContext {

    /**
     * Represents the current FireGen initiated of {@link IncidentManager} for quick-reference reasons.
     * This is also obtainable at {@link net.noahf.firegen.discord.Main#incidents Main.incidents}
     * @see ActionsContext
     */
    private IncidentManager manager;

    /**
     * Represents the current {@link Incident} that an action is being applied to.
     * @see ActionsContext
     */
    private Incident incident;

    /**
     * Represents the current {@link String command} that is being executed. This is usually provided in the class by
     * the method {@link FireGenAction#getName() getName()}.
     * @see ActionsContext
     */
    private String command;

    /**
     * Represents any additional parameters provided by the calling method. This can include additional information and
     * context to facilitate easy transfer-of-information.
     * @see ActionsContext
     */
    private List<String> parameters;

}
