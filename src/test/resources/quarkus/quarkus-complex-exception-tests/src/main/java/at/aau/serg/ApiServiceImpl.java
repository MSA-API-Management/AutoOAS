package at.aau.serg;

import at.aau.serg.interfaces.ApiService;
import jakarta.ws.rs.InternalServerErrorException;

public class ApiServiceImpl implements ApiService {
    @Override
    public void start(String id) {
        if(id.equals("error")) throw new InternalServerErrorException();
    }
}
