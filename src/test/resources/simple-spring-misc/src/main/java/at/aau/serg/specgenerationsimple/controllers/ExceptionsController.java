package at.aau.serg.specgenerationsimple.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionsController {

    @GetMapping("/potentially-unsupported")
    public String potentiallyUnsupported() {
        if (Math.random() < 0.5) {
            throw new UnsupportedOperationException("unsupported.");
        }
        return "unsupported";
    }

    @GetMapping("/unsupported")
    public String unsupported() {
        throw new UnsupportedOperationException("unsupported.");
    }

}
