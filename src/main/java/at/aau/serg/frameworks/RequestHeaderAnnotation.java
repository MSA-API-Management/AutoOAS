package at.aau.serg.frameworks;

public interface RequestHeaderAnnotation {
    String value();
    String name();
    boolean required();
}
