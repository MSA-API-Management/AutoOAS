package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.exceptions.NotFoundException;
import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController()
public class LocalHandlerController {

    @GetMapping("/local-exception-handler")
    public DeferredResult<ResponseEntity<SimpleObject>> getOrNot() {
        if (Math.random() > 0.5) {
            throw new NotFoundException("not available");
        }

        DeferredResult<ResponseEntity<SimpleObject>> res = new DeferredResult<>();
        res.setResult(ResponseEntity.ok(new SimpleObject("test", 1)));
        return res;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public void handleNotFoundException(NotFoundException ex) {

    }

}
