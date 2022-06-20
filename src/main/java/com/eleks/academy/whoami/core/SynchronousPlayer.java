package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.model.response.PlayerState;

public interface SynchronousPlayer extends Player {

    String getName();

    SynchronousPlayer setName(String name);

    void setCharacter(String character);

    PlayerState getState();

    void setAnswer(Answer answer);

}