package net.noahf.firewatch.common.utils;

import java.time.Instant;
import java.util.Random;

public class Identifier {

    public static final int ID_NUMERAL_LENGTH = 5;

    public static Identifier generate(int prefix) {
        return new Identifier(prefix);
    }

    private final Instant created;
    private final int prefix;
    private final int id;

    Identifier(int prefix) {
        if (prefix > 999) {
            throw new IllegalArgumentException("Prefix cannot have more than three digits.");
        }
        if (prefix < 0) {
            throw new IllegalArgumentException("Prefix cannot be negative.");
        }

        this.created = Instant.now();
        this.prefix = prefix;
        this.id = new Random(System.currentTimeMillis()).nextInt((int) Math.pow(10, ID_NUMERAL_LENGTH), (int) Math.pow(10, ID_NUMERAL_LENGTH + 1));
    }

    public String display() {
        String year = String.valueOf(this.yearPrefix());
        String prefix = String.format("%03d", this.prefix);
        String id = String.format("%0" + ID_NUMERAL_LENGTH + "d", this.id);

        return year + "-" + prefix + id;
    }

    public int yearPrefix() { return TimeHelper.getDate(this.created).getYear(); }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Identifier other)) return false;

        return this.prefix == other.prefix
                && this.id == other.id
                && this.yearPrefix() == other.yearPrefix();
    }
}
