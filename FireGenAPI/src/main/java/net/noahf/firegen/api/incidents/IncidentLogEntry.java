package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.Identifiable;

import java.time.LocalDateTime;

public interface IncidentLogEntry extends Identifiable {

    LocalDateTime getTime();

    Contributor getUser();

    String getEntry();

    EntryType getType();

    void setType(EntryType newType);

    default boolean isNarrative() {
        return this.getType() == EntryType.NARRATIVE ||
                this.getType() == EntryType.HIDDEN;
    }


    enum EntryType {
        CREATE,
        UPDATE,
        AGENCY,
        NARRATIVE,
        HIDDEN;

//        private static final int MAX_LENGTH = Arrays.stream(EntryType.values())
//                .mapToInt(e -> e.name().length())
//                .max()
//                .orElse(0);
//
//        // -------- [ BELOW THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
//        private static String pad(String s) {
//            StringBuilder builder = new StringBuilder(s);
//            builder.repeat(' ', MAX_LENGTH - s.length());
//            return builder.toString();
//        }
//        // -------- [ ABOVE THIS LINE CONTAINS SOME LLM-WRITTEN OR MODIFIED CODE ] --------
//
//        /**
//         * Finds the longest {@link EntryType}, and any value that's not as long as the longest EntryType will be
//         * centered. E.g., <br>
//         *
//         * `NARRATIVE` remains `NARRATIVE` but <br>
//         * `CREATE` becomes `  CREATE `
//         * @return the centered version of this value, padded with spaces on the left and right
//         */
//        public String padded() {
//            return pad(this.name());
//        }
    }

}
