package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;

@RestController
@RequestMapping("/simples")
@Controller
//@Api("very interesting stuff")
public class SimpleController {

    @GetMapping("/response-entity-generic")
    public ResponseEntity getGenericResponseEntity() {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        return responseBuilder.body(new SimpleObject("test", 1));
    }

    @RequestMapping(value = "/response-entity-questionmark-capture", method = RequestMethod.GET)
    public ResponseEntity<?> getResponseEntityGenericCapture() {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        return responseBuilder.build();
    }

    @GetMapping("/deferred-empty")
    public DeferredResult getDeferredResult() {
        DeferredResult<SimpleObject> res = new DeferredResult<>();
        res.setResult(new SimpleObject("test", 1));
        return res;
    }

    @GetMapping("/deferred-questionmark")
    public DeferredResult<?> getDeferredResultGenericCapture() {
        DeferredResult<SimpleObject> res = new DeferredResult<>();
        res.setResult(new SimpleObject("test", 1));
        return res;
    }

    @GetMapping("/deferred-simple-object")
    public DeferredResult<SimpleObject> getDeferredResultStringCapture() {
        DeferredResult<SimpleObject> res = new DeferredResult<>();
        res.setResult(new SimpleObject("test", 1));
        return res;
    }

    @GetMapping("/deferred-response-entity-simple-object")
    public DeferredResult<ResponseEntity<SimpleObject>> getDeferredResultResponseEntityStringCapture() {
        DeferredResult<ResponseEntity<SimpleObject>> res = new DeferredResult<>();
        res.setResult(ResponseEntity.ok(new SimpleObject("test", 1)));
        return res;
    }

}
