package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.Simple;
//import io.swagger.annotations.Api;
import at.aau.serg.specgenerationsimple.models.SimpleEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simples")
@Controller
//@Api("very interesting stuff")
public class SimpleController {

    @GetMapping
    public ResponseEntity<List<Simple>> getAll() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return ResponseEntity.ok(al);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simple> getById(@PathVariable("id") Long id) {
        if (id < 1)
            return new ResponseEntity<>(new Simple(), HttpStatus.ALREADY_REPORTED);

        return ResponseEntity.ok(new Simple().withName("Peter").withId(1));
    }

    @GetMapping("/response-status/{id}")
    @ResponseStatus(value = HttpStatus.ALREADY_REPORTED)
    public Simple getById2(@PathVariable("id") Long id) {
        return new Simple().withName("Peter").withId(1);
    }

    @GetMapping("/primitive-list")
    public ResponseEntity<List<Integer>> getAllPrimitiveList() {
        ArrayList<Integer> al = new ArrayList<>();
        al.add(7);
        return ResponseEntity.ok(al);
    }

    @GetMapping("/void-response")
    public void getAllNoReturn() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
    }

    @GetMapping("/raw-list")
    public ResponseEntity<List> getAllRawList() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return ResponseEntity.ok(al);
    }

    @GetMapping("/enum")
    public ResponseEntity<SimpleEnum> getEnum() {
        return ResponseEntity.ok(SimpleEnum.NICE);
    }

    @GetMapping("/simple-superclass-property")
    public ResponseEntity<Simple> getSuperclassSimple() {
        Simple s = new Simple();
        s.id = 50;
        s.name = "I contain superclass field values";
        return ResponseEntity.ok(s);
    }

    @PostMapping("request-body-list")
    public ResponseEntity<List<Simple>> create(@RequestBody List<Simple> simples) { //throws IllegalResourceException {
        return ResponseEntity.ok(simples);
    }

    @PostMapping("request-body-object")
    public ResponseEntity<Simple> create(@RequestBody Simple simple) { //throws IllegalResourceException {
        return ResponseEntity.ok(simple);
    }

    @PostMapping("unique-operation-ids")
    public ResponseEntity<Simple> create(@RequestBody String str) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
