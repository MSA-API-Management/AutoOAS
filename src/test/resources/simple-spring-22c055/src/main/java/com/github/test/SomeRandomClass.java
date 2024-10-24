package com.github.test;

import at.aau.serg.specgenerationsimple.models.Simple;

public class SomeRandomClass {

    private Simple simple;

    /**
     * This class should be ignored.
     */
    public SomeRandomClass(Simple s){
        this.simple = s;
    }

    public Simple getSimple(){
        return simple;
    }
}
