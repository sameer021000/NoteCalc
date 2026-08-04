package com.example.notecalc.ncagent.parser;

import com.example.notecalc.ncagent.RecordCandidate;

public class CandidateBuilder {
    public RecordCandidate build(FieldExtractor.ExtractedFields fields) {
        RecordCandidate candidate = new RecordCandidate();
        candidate.setDescription(fields.description);
        candidate.setAmount(fields.amount);
        candidate.setDate(fields.date);
        candidate.setCategory(fields.category);
        candidate.setRemarks(fields.remarks);
        return candidate;
    }
}
