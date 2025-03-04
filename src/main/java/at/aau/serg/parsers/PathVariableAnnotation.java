package at.aau.serg.parsers;

public interface PathVariableAnnotation {
    String value();
    String name();
    boolean required();
}
