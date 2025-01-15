package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
public class RequestsController {

    @GetMapping("/header-param")
    public SimpleObject header_param(@RequestHeader(value = "req-test", required = false) String test) {
        return new SimpleObject(test, 1);
    }

    @GetMapping("/header-param-required")
    public SimpleObject header_param_required(@RequestHeader(value = "num", required = true) int num) {
        return new SimpleObject("Test", num);
    }

    @GetMapping("/regex/{lastname:^[a-zA-Z0-9]*$}/{firstname:^[A-Za-z]*$}")
    public SimpleObject regexParam(@PathVariable String lastname, @PathVariable String firstname) {
        return new SimpleObject(lastname + " " + firstname, -1);
    }
}
