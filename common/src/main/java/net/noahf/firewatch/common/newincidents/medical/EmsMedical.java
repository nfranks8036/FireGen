package net.noahf.firewatch.common.newincidents.medical;

import java.util.List;
import java.util.Map;

public record EmsMedical(Map<String, List<EmsFieldDetails>> dynamicFields) {

    public List<EmsFieldDetails> get(String key) {
        return this.dynamicFields.getOrDefault(key, null);
    }

}
