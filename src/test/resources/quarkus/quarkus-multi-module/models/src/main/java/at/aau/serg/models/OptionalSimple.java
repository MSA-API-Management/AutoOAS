package at.aau.serg.models;

import java.io.Serializable;
import java.util.Optional;

public class OptionalSimple implements Serializable {

    public int id;
    public Optional<Simple> simple;

}