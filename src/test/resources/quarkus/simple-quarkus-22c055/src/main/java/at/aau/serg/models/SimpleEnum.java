package at.aau.serg.models;

public enum SimpleEnum {
    NICE(1),
    OK(2),
    BAD(3);

    public final int value;

    private SimpleEnum(int val) {
        this.value = val;
    }

}
