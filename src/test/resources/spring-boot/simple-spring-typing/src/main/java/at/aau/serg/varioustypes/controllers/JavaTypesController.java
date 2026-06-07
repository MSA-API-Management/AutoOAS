package at.aau.serg.varioustypes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/types")
public class JavaTypesController {

    @GetMapping("/getList")
    public ResponseEntity<List<Optional<Integer>>> getList() {
        List<Optional<Integer>> retVal = new ArrayList<>();
        retVal.add(Optional.of(1));
        retVal.add(Optional.of(1));
        retVal.add(Optional.of(2));
        return ResponseEntity.ok().body(retVal);
    }

    @GetMapping("/getSet")
    public ResponseEntity<Set<Integer>> getSet() {
        Set<Integer> retVal = new HashSet<>();
        retVal.add(1);
        retVal.add(1);
        retVal.add(2);
        return ResponseEntity.ok().body(retVal);
    }

    @GetMapping("/getObj")
    public ResponseEntity<Object> getObj() {
        return ResponseEntity.ok().body(new Object());
    }

    @GetMapping("/getVoid")
    public ResponseEntity<Void> getVoid() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getOptionalInt")
    public ResponseEntity<Optional<Integer>> getOptionalInt() {
        return ResponseEntity.ok().body(Optional.of(1));
    }

    @GetMapping("/getMapOfList")
    public ResponseEntity<Map<String, List<Integer>>> getMapOfList() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(1);
        list.add(2);
        HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
        map.put("test", list);
        return ResponseEntity.ok().body(map);
    }

    @GetMapping("/getMapOfMap")
    public ResponseEntity<Map<String, Map<String, Double>>> getMapOfMap() {
        HashMap<String, Double> map = new HashMap<>();
        map.put("test", 1.2);
        HashMap<String, Map<String, Double>> retVal = new HashMap<>();
        retVal.put("test", map);
        return ResponseEntity.ok().body(retVal);
    }

    @GetMapping("/getMapOfObj")
    public ResponseEntity<Map<String, Object>> getMapOfObj() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("test", new Object());
        return ResponseEntity.ok().body(map);
    }

    @GetMapping("/getListOfList")
    public ResponseEntity<List<List<Integer>>> getListOfList() {
        List<Integer> retVal = new ArrayList<Integer>();
        retVal.add(1);
        retVal.add(1);
        retVal.add(2);
        List<List<Integer>> list = new ArrayList<>();
        list.add(retVal);
        return ResponseEntity.ok().body(list);
    }

    @PostMapping("/echoNativeInt")
    public int getNativeInt(@RequestBody int i) {
        return i;
    }

}
