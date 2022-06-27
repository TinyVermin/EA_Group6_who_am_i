package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.core.impl.AnsweringPlayer;
import com.eleks.academy.whoami.core.impl.Entry;

public interface History {

    void addNewEntry(Entry entry);

    void addAnswerToEntry(AnsweringPlayer player);
}
