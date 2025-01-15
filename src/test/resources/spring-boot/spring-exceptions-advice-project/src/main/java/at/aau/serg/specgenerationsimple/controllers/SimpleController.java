package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.exceptions.NotFoundException;
import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class SimpleController {

    @GetMapping("/random-not-found")
    public DeferredResult<ResponseEntity<SimpleObject>> getOrNot() {
        if (Math.random() > 0.5) {
            throw new NotFoundException("not available");
        }

        DeferredResult<ResponseEntity<SimpleObject>> res = new DeferredResult<>();
        res.setResult(ResponseEntity.ok(new SimpleObject("test", 1)));
        return res;
    }

    @GetMapping("/random-not-allowed")
    public DeferredResult<ResponseEntity<SimpleObject>> getOrNotAllowed() {
        if (Math.random() > 0.5) {
            throw new IllegalStateException("not allowed");
        } else if (Math.random() < 0.5) {
            throw new UnsupportedOperationException();
        }

        DeferredResult<ResponseEntity<SimpleObject>> res = new DeferredResult<>();
        res.setResult(ResponseEntity.ok(new SimpleObject("test", 1)));
        return res;
    }
}
