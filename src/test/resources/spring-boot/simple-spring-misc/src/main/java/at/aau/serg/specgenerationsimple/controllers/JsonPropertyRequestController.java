package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleJsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/json-property")
public class JsonPropertyRequestController {
    @PostMapping("/json-response-entity-request-body")
    public ResponseEntity<SimpleJsonProperty> handleJsonPropertyResponseEntityWithRequestBody(@RequestBody SimpleJsonProperty request) {
        return ResponseEntity.ok(request);
    }

    @GetMapping("/json-response-entity")
    public SimpleJsonProperty handleJsonPropertyResponseEntityWithoutRequestBody() {
        return new SimpleJsonProperty("Name", 1);
    }

    @PostMapping("/json-response-object")
    public SimpleJsonProperty handleJsonPropertyObject(@RequestBody SimpleJsonProperty request) {
        return request;
    }
}
