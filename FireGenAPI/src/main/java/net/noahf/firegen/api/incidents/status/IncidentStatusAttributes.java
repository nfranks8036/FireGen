package net.noahf.firegen.api.incidents.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class IncidentStatusAttributes {

    private List<StatusAttribute> attributes;

    public boolean contains(StatusAttribute attribute, StatusAttribute... andAttributes) {
        if (!this.attributes.contains(attribute)) {
            return false;
        }

        for (StatusAttribute s : andAttributes) {
            if (!this.attributes.contains(s)) {
                return false;
            }
        }

        return true;
    }

    public boolean isInProgress() {
        return this.contains(StatusAttribute.IN_PROGRESS);
    }

    public boolean isDefault() {
        return this.contains(StatusAttribute.DEFAULT);
    }

    public boolean isActive() { return this.contains(StatusAttribute.ACTIVE); }

    public boolean isClosed() {
        return this.contains(StatusAttribute.CLOSED);
    }

}
