package net.noahf.firegen.discord.incidents.structure.types;

import lombok.*;
import net.noahf.firegen.api.incidents.types.IncidentTypeTagQualifierList;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@ToString
public class IncidentTypeTagQualifierListImpl implements AutofilledCharSequence, IncidentTypeTagQualifierList {
    private @Getter(value = AccessLevel.NONE) long id;

    protected final boolean required;
    protected final boolean unique;
    protected final String syntax;
    protected final List<String> qualifiers;

}