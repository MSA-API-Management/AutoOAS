package at.aau.serg.specgenerationsimple.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleObject {

    public static String SOME_STATIC_FIELD = "default";

    public String name = SOME_STATIC_FIELD;
    public int age;

}
