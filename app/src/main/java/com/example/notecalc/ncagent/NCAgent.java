package com.example.notecalc.ncagent;

import com.example.notecalc.Record;
import com.example.notecalc.ncagent.parser.InputParser;

import java.util.ArrayList;
import java.util.List;

public class NCAgent {
    private final IntentDetectionEngine intentEngine;
    private final InputParser parser;
    private final RecordMatchingEngine matcher;
    private final ActionExecutor executor;

    public NCAgent() {
        this.intentEngine = new IntentDetectionEngine();
        this.parser = new InputParser();
        this.matcher = new RecordMatchingEngine();
        this.executor = new ActionExecutor();
    }

    public List<NCAction> process(String rawInput, List<Record> databaseRecords) {
        NCAgentIntent intent = intentEngine.detectIntent(rawInput);
        List<RecordCandidate> candidates = parser.parse(rawInput);
        
        List<NCAction> actions = new ArrayList<>();
        
        // If ambiguous intent, we just return one generic action error, 
        // no need to map over all parsed candidates.
        if (intent == NCAgentIntent.AMBIGUOUS) {
            NCAction ambiguousAction = executor.execute(intent, new RecordCandidate(), databaseRecords, matcher);
            actions.add(ambiguousAction);
            return actions;
        }

        for (RecordCandidate candidate : candidates) {
            NCAction action = executor.execute(intent, candidate, databaseRecords, matcher);
            actions.add(action);
        }
        
        return actions;
    }
}
