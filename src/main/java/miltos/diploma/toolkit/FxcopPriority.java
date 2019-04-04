package miltos.diploma.toolkit;

public enum FxcopPriority {
    WAITING(0),
    READY(1),
    SKIPPED(-1),
    Completed(5);

    private int code;

    FxcopPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}