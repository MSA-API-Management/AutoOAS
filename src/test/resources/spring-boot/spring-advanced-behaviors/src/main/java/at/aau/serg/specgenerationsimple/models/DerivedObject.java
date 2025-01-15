package at.aau.serg.specgenerationsimple.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DerivedObject extends BaseObject {

    public String derivedFieldString;
    public int derivedFieldInt = -1;

}
