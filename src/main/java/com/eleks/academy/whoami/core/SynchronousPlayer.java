package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.core.impl.Answer;

public interface SynchronousPlayer extends Player {

    String getName();

    SynchronousPlayer setName(String name);

    void setCharacter(String character);

    String getCharacter();

    void setAnswer(Answer answer);

}