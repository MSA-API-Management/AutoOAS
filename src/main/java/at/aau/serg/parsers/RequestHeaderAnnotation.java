package at.aau.serg.parsers;

public interface RequestHeaderAnnotation {
    String value();
    String name();
    boolean required();
}
