package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.DtoWithGetters;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dto-with-getters")
public class RequestBodyGetterMethodsController {

    @PutMapping("/dto")
    public ResponseEntity put(@RequestBody DtoWithGetters dto) {
        System.out.println(dto);
        return ResponseEntity.noContent().build();
    }
}
