package at.aau.serg.parsers;

public interface RequestAnnotation {
    String name();
    String[] produces();
    String[] consumes();
    String[] value();
    String[] path();
    HttpMethod[] method();
}
