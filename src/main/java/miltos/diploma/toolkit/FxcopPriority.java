package miltos.diploma.toolkit;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum FxcopPriority {
    WAITING(0),
    READY(1),
    SKIPPED(-1),
    Completed(5);

    private static final Map<Integer, FxcopPriority> intLookup = new HashMap<>();
    private static final Map<FxcopPriority, Integer> priorityLookup = new HashMap<>();

    static {
        for (FxcopPriority s : EnumSet.allOf(FxcopPriority.class)) {
            intLookup.put(s.getCode(), s);
            priorityLookup.put(s, s.getCode());
        }
    }

    private int code;

    FxcopPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static FxcopPriority get(int code) {
        return intLookup.get(code);
    }

    public static int get(FxcopPriority code) {
        return priorityLookup.get(code);
    }
}