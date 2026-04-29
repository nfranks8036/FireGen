package net.noahf.firegen.discord.incidents.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.noahf.firegen.api.incidents.IncidentTypeTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IncidentTypeTagImpl implements IncidentTypeTag {

    public static final IncidentTypeTagImpl DEFAULT;

    static {
        DEFAULT = new IncidentTypeTagImpl(null);
        DEFAULT.tagName = "None";
        DEFAULT.priorities = new ArrayList<>(List.of("1", "2", "3"));
        DEFAULT.qualifier = null;
    }

    private final JsonObject object;

    public @Getter String tagName;
    public @Getter List<String> priorities;

    private @Getter IncidentTypeTag.Qualifier qualifier;

    public IncidentTypeTagImpl(JsonObject object) {
        this.object = object;
        if (object == null) {
            return;
        }

        this.tagName = this.object.get("name").getAsString();
        this.priorities = this.object.get("priorities").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();

        if (!this.object.get("qualifiers").isJsonNull()) {

            JsonObject qualifierObj = this.object.get("qualifiers").getAsJsonObject();
            this.qualifier = new Qualifier(
                    qualifierObj.get("required").getAsBoolean(),
                    qualifierObj.get("unique").getAsBoolean(),
                    qualifierObj.get("syntax").getAsString(),
                    qualifierObj.get("list").getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsString)
                            .toList()
            );
        } else this.qualifier = null;
    }

    @Override
    public List<String> findTypeOptions(String type) {
        List<String> returned = new ArrayList<>();
        if (qualifier == null) {
            returned.add(type);
            return returned;
        }

        if (!qualifier.isRequired()) {
            returned.add(type);
        }

        if (!qualifier.isUnique()) {
            List<String> output = new ArrayList<>();
            int total = 1 << qualifier.getQualifiers().size();
            for (int mask = 1; mask < total; mask++) {
                List<String> combo = new ArrayList<>();

                for (int i = 0; i < qualifier.getQualifiers().size(); i++) {
                    if ((mask & (1 << i)) != 0) {
                        combo.add(qualifier.getQualifiers().get(i));
                    }
                }

                output.add(qualifier.getSyntax()
                        .replace("{T}", type)
                        .replace("{Q}", String.join(", ", combo))
                );
            }

            returned.addAll(output);

            return returned;
        }

        for (String q : qualifier.getQualifiers()) {
            returned.add(qualifier.getSyntax()
                    .replace("{T}", type)
                    .replace("{Q}", q)
            );
        }

        return returned;
    }

    @Override
    @NotNull
    public String toString() {
        return "IncidentTypeTag(name=" + this.tagName + ", priorities=[" + String.join(", ", this.priorities) + "], qualifier=" + this.qualifier + ")";
    }
}
