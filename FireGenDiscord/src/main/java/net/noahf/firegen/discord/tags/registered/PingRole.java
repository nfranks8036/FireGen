package net.noahf.firegen.discord.tags.registered;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.noahf.firegen.api.incidents.tags.IncidentTag;
import net.noahf.firegen.api.incidents.tags.IncidentTagExecution;
import net.noahf.firegen.api.incidents.tags.IncidentTagExecutionContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PingRole implements IncidentTag {

    private long role;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return "Adds a ping to the incident for the specific role with ID '" + role +"'";
    }

    public String getDescription(JDA jda) {
        Role roleObj = jda.getRoleById(role);
        String roleStr = roleObj != null ? roleObj.getName() : "[id " + role + "]";
        return "Adds a ping to the incident for the specific role '" + roleStr + "'";
    }

    @Override
    public List<IncidentTagExecution<?, ?>> getExecutions() {
        return List.of(
                new IncidentTagExecution<Long, Void>() {
                    @Override
                    public Class<Long> getTypeOfInput() {
                        return Long.class;
                    }

                    @Override
                    public Class<Void> getTypeOfOutput() {
                        return null;
                    }

                    @Override
                    public Void execute(IncidentTagExecutionContext context, Long input) {
                        role = input;
                        context.getIncident().update();
                        return null;
                    }
                }
        );
    }

    @Override
    public @NonNull String toString() {
        return "PingRole[long=" + role + "]";
    }
}
