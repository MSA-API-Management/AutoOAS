package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleRequestPartDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/request-part")
public class RequestPartController {

    @PostMapping("/dto")
    public ResponseEntity<SimpleRequestPartDto> handleDto(@RequestPart("dto") SimpleRequestPartDto dto) {

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/string")
    public ResponseEntity<String> handleString(@RequestPart("name") String name) {

        return ResponseEntity.ok(name);
    }

    @PostMapping("/integer")
    public ResponseEntity<Integer> handleInteger(@RequestPart("age") Integer age) {

        return ResponseEntity.ok(age);
    }

    @PostMapping("/mixed")
    public ResponseEntity<SimpleRequestPartDto> handleMixed(@RequestPart("dto") SimpleRequestPartDto dto,
                                                            @RequestPart("comment") String comment,
                                                            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(dto);
    }
}