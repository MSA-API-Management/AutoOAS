package at.aau.serg;

import jakarta.ws.rs.NotFoundException;

public class AnotherSimple {
    public void handle() {
        throw new NotFoundException();
    }

}
