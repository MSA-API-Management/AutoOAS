package at.aau.serg.varioustypes.models;

public class ResponseImpl implements Response {
    private int baseValue;
    private String otherValue;

    public ResponseImpl(int baseValue, String otherValue) {
        this.baseValue = baseValue;
        this.otherValue = otherValue;
    }

    @Override
    public String getOtherValue() {
        return otherValue;
    }

    @Override
    public int getBaseValue() {
        return baseValue;
    }
}
