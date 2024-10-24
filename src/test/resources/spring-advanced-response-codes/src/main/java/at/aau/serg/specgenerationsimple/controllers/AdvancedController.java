package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdvancedController {

    @RequestMapping(value = "/do-nothing", method = {RequestMethod.GET, RequestMethod.POST})
    public void doNothing() {
    }

    @GetMapping("/response-entities-inline")
    public ResponseEntity<SimpleObject> handleResponseEntitiesInline() {
        if (Math.random() < 0.5) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } else if (Math.random() < 0.5) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } else if (Math.random() < 0.5) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } else {
            return ResponseEntity.ok(new SimpleObject());
        }
    }

    @GetMapping("/response-entities-explicit")
    public ResponseEntity<SimpleObject> handleResponseEntitiesExplicit() {
        // w/ generics:
        ResponseEntity<SimpleObject> ok = new ResponseEntity<>(new SimpleObject(), HttpStatus.OK);
        ResponseEntity<SimpleObject> faulty1 = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // w/o generics:
        ResponseEntity faulty2 = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        if (Math.random() < 0.5) {
            return faulty1;

        } else if (Math.random() < 0.5) {
            return faulty2;

        } else {
            return ok;
        }
    }

    @RequestMapping(method = RequestMethod.OPTIONS, value = "/no-content")
    @ResponseBody
    public ResponseEntity noContentResponse() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
