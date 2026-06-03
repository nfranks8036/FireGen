package net.noahf.firegen.discord.incidents.structure.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.incidents.types.IncidentTypeTagQualifierList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(force = true)
public class IncidentTypeTagImpl implements IncidentTypeTag {

    public static final IncidentTypeTagImpl DEFAULT;

    static {
        DEFAULT = new IncidentTypeTagImpl(null);
        DEFAULT.tagName = "None";
        DEFAULT.priorities = new ArrayList<>(List.of("1", "2", "3"));
        DEFAULT.qualifiers = null;
    }

    private @Getter(value = AccessLevel.NONE) transient final JsonObject object;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public String tagName;
    public List<String> priorities;

    private @OneToOne(cascade = CascadeType.ALL, targetEntity = IncidentTypeTagQualifierListImpl.class)
            IncidentTypeTagQualifierList qualifiers;

    public IncidentTypeTagImpl(JsonObject object) {
        this.object = object;
        if (object == null) {
            return;
        }

        this.tagName = this.object.get("name").getAsString();
        this.priorities = this.object.get("priorities").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();

        if (!this.object.get("qualifiers").isJsonNull()) {

            JsonObject qualifierObj = this.object.get("qualifiers").getAsJsonObject();
            this.qualifiers = new IncidentTypeTagQualifierListImpl(
                    qualifierObj.get("required").getAsBoolean(),
                    qualifierObj.get("unique").getAsBoolean(),
                    qualifierObj.get("syntax").getAsString(),
                    qualifierObj.get("list").getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsString)
                            .toList()
            );
        } else this.qualifiers = null;
    }

    @Override
    public List<String> findTypeOptions(String type) {
        List<String> returned = new ArrayList<>();
        if (this.qualifiers == null) {
            returned.add(type);
            return returned;
        }

        if (!this.qualifiers.isRequired()) {
            returned.add(type);
        }

        if (!this.qualifiers.isUnique()) {
            List<String> output = new ArrayList<>();
            int total = 1 << this.qualifiers.getQualifiers().size();
            for (int mask = 1; mask < total; mask++) {
                List<String> combo = new ArrayList<>();

                for (int i = 0; i < this.qualifiers.getQualifiers().size(); i++) {
                    if ((mask & (1 << i)) != 0) {
                        combo.add(this.qualifiers.getQualifiers().get(i));
                    }
                }

                output.add(this.qualifiers.getSyntax()
                        .replace("{T}", type)
                        .replace("{Q}", String.join(", ", combo))
                );
            }

            returned.addAll(output);

            return returned;
        }

        for (String q : this.qualifiers.getQualifiers()) {
            returned.add(this.qualifiers.getSyntax()
                    .replace("{T}", type)
                    .replace("{Q}", q)
            );
        }

        return returned;
    }

    @Override
    @NotNull
    public String toString() {
        return "IncidentTypeTag(name=" + this.tagName + ", priorities=[" + String.join(", ", this.priorities) + "], qualifiers=" + this.qualifiers + ")";
    }
}
