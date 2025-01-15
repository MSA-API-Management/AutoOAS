package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.Simple;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entity")
@Profile("dev")
public class DevController {

    @GetMapping
    public ResponseEntity<List<Simple>> getAll() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return ResponseEntity.ok(al);
    }

    @PostMapping("/run")
    public ResponseEntity<Simple> run(@RequestBody String str) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
