package at.aau.serg;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

public class Simple {
    public void handleError() {
        throw new ForbiddenException();
    }

    public static void handleOtherError() {
        throw new NotFoundException();
    }
}
