package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class MultipleMethodsController {

    /**
     * This allows all http methods
     *
     * @param num
     * @return
     */
    @RequestMapping("/unspecified-method")
    public SimpleObject unspecifiedMethod(@RequestHeader(value = "num") int num) {
        return new SimpleObject("Echo", num);
    }

    @GetMapping("/get-and-post-method")
    @PostMapping("/get-and-post-method")
    public SimpleObject getAndPostMethod(@RequestHeader(value = "num") int num) {
        return new SimpleObject("Echo", num);
    }

    @RequestMapping(value = "/get-and-post-method-reqmapping", method = {RequestMethod.GET, RequestMethod.POST})
    public SimpleObject getAndPostRequestMappingMethod(@RequestHeader(value = "num") int num) {
        return new SimpleObject("Echo", num);
    }

}
