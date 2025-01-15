package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.Simple;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

public class SimpleControllerBaseBase {

    @GetMapping("/controller-supersuperclass-endpoint")
    public ResponseEntity<Simple> getSuperclass() {
        return ResponseEntity.ok(new Simple().withId(42));
    }

    @PatchMapping("/controller-supersuperclass-endpoint")
    public ResponseEntity<Simple> getSuperclassPatch() {
        return ResponseEntity.ok(new Simple().withId(42));
    }

}
