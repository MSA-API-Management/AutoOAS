package at.aau.serg.specgenerationsimple.controllers;


import at.aau.serg.specgenerationsimple.models.AnotherSimple;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/othersimples")
public class AnotherController {

    @GetMapping
    public ResponseEntity<List<AnotherSimple>> getAll() {
        ArrayList<AnotherSimple> al = new ArrayList<>();
        al.add(new AnotherSimple().withAvgGrade(9.11).withSsn("1324").withId(1));
        return ResponseEntity.ok(al);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<AnotherSimple> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new AnotherSimple().withAvgGrade(2.5).withSsn("2511").withId(1));
    }

    @GetMapping("/{id}/{ssn}")
    public ResponseEntity<AnotherSimple> getByIdAndSsn(@PathVariable("id") Long id, @PathVariable("ssn") String ssn) {
        return ResponseEntity.ok(new AnotherSimple().withAvgGrade(2.5).withSsn("2511").withId(1));
    }

    @GetMapping("/string-test")
    @ResponseBody
    public String getFoos(@RequestParam(required = false) String id) {
        return "ID: " + id;
    }

}
