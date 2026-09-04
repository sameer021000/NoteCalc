package com.example.notecalc;

import java.util.ArrayList;
import java.util.List;

public class RecordUtils {

    public static void resequentializeRecords(List<Record> records) {
        if (records == null || records.isEmpty()) return;
        List<Record> copy = new ArrayList<>(records);
        copy.sort(java.util.Comparator.comparingInt(Record::getOriginalIndex));
        for (int i = 0; i < copy.size(); i++) {
            copy.get(i).setOriginalIndex(i);
        }
    }
}
