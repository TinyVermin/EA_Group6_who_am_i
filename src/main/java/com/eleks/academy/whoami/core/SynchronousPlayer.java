package com.eleks.academy.whoami.core;

public interface SynchronousPlayer extends Player {

    String getName();

    SynchronousPlayer setName(String name);

    void setCharacter(String character);

    boolean isGuessing();

    void setAnswer(String answer, boolean guessing);

}