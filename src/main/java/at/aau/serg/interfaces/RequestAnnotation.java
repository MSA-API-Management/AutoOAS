package at.aau.serg.interfaces;

import at.aau.serg.parsers.HttpMethod;

public interface RequestAnnotation {
    String name();
    String[] produces();
    String[] consumes();
    String[] value();
    String[] path();
    HttpMethod[] method();
}
