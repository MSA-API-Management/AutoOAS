package at.aau.serg.specgenerationsimple.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseObject {

    public long id = 1;
    public String baseFieldString;

}
