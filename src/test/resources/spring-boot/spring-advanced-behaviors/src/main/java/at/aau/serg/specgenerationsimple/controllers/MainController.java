package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.DerivedObject;
import at.aau.serg.specgenerationsimple.models.SimpleObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class MainController {

    // curl -X POST "http://localhost:8080/model-attribute?name=hi&age=5" -H  "accept: */*" -H  "Content-Type: application/json"
    // curl -X GET "http://localhost:8080/model-attribute?name=test&age=15" -H  "accept: */*"
    @RequestMapping(value = "/model-attribute", method = {RequestMethod.GET, RequestMethod.POST})
    public SimpleObject echoModelAttribute(@ModelAttribute @Valid SimpleObject simpleObject) {
        return simpleObject;
    }

//    curl -X 'GET' 'http://localhost:8080/model-attribute-inheritance?derivedFieldString=1&derivedFieldInt=1&id=1&baseFieldString=1' -H 'accept: application/json'
//    curl -X 'POST' 'http://localhost:8080/model-attribute-inheritance?derivedFieldString=1&derivedFieldInt=2&id=3&baseFieldString=4' -H 'accept: application/json' -d ''
    @RequestMapping(value = "/model-attribute-inheritance", method = {RequestMethod.GET, RequestMethod.POST})
    public DerivedObject echoModelAttribute(@ModelAttribute @Valid DerivedObject simpleObject) {
        return simpleObject;
    }


    // todo HttpServletRequest / HttpServletResponse
//    @RequestMapping(value = "/do-nothing-201", method = {RequestMethod.GET, RequestMethod.POST})
//    public void doNothing201(HttpServletResponse response) {
//        response.setStatus(201);
//    }



    // todo test this behaviors with annotation
    //  throws has precedence, how about returning a response entity
//    @GetMapping("/no-content-annotation")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void test() {
//        throw new IllegalArgumentException();
//    }


    // todo Operation hidden has to be ignored, still exposed!
//    @GetMapping("/no-content-annotation")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @Operation(hidden = true)
//    public void test() {
//    }
}
