package at.aau.serg.interceptors;

import org.springframework.http.HttpStatus;
import spoon.reflect.declaration.CtType;

public class ExceptionHandlerResponseStatusMapping {

    public final CtType<?> exceptionType;
    public final HttpStatus returnStatus;

    public ExceptionHandlerResponseStatusMapping(CtType<?> exceptionType, HttpStatus returnStatus) {
        this.exceptionType = exceptionType;
        this.returnStatus = returnStatus;
    }
}

