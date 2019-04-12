package miltos.diploma.utility;

public enum ProjectLanguage {
    Java(1),
    CSharp(2);

    private int code;

    ProjectLanguage(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
