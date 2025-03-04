package at.aau.serg.parsers;

public interface RequestParamAnnotation {
    String value();
    String name();
    boolean required();
}
