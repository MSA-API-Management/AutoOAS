package at.aau.serg.specgenerationsimple.controllers;


import at.aau.serg.specgenerationsimple.models.AnotherSimple;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/no-profiles")
public class NoProfilesController {

    @GetMapping
    public ResponseEntity<List<AnotherSimple>> getAll() {
        ArrayList<AnotherSimple> al = new ArrayList<>();
        al.add(new AnotherSimple().withAvgGrade(9.11).withSsn("1324").withId(1));
        return ResponseEntity.ok(al);
    }
}
