package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.History;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryImpl implements History {

    private List<Entry> entries = new ArrayList<>();

    @Override
    public void addNewEntry(Entry entry) {
        var id = entries.size() + 1;
        entry.setId(id);
        this.entries.add(entry);
    }

    @Override
    public void addAnswerToEntry(AnsweringPlayer player) {
        var id = entries.size();
        if (id > 0) {
            this.entries.stream()
                    .filter(entry -> entry.getId().equals(id))
                    .findFirst()
                    .ifPresent(entry -> entry.addPlayerWithAnswer(player));
        }
    }
}
