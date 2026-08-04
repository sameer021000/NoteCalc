package com.example.notecalc.ncagent;

import com.example.notecalc.Record;
import java.util.List;

public class NCAction {
    private final NCAgentIntent intent;
    private Record targetRecord;
    private Record validatedRecord;
    
    private boolean valid;
    private String errorMessage;
    
    private boolean needsDisambiguation;
    private List<Record> disambiguationCandidates;

    public NCAction(NCAgentIntent intent) {
        this.intent = intent;
        this.valid = true;
        this.needsDisambiguation = false;
    }

    public NCAgentIntent getIntent() { return intent; }

    public Record getTargetRecord() { return targetRecord; }
    public void setTargetRecord(Record targetRecord) { this.targetRecord = targetRecord; }

    public Record getValidatedRecord() { return validatedRecord; }
    public void setValidatedRecord(Record validatedRecord) { this.validatedRecord = validatedRecord; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isNeedsDisambiguation() { return needsDisambiguation; }
    public void setNeedsDisambiguation(boolean needsDisambiguation) { this.needsDisambiguation = needsDisambiguation; }

    public List<Record> getDisambiguationCandidates() { return disambiguationCandidates; }
    public void setDisambiguationCandidates(List<Record> disambiguationCandidates) { this.disambiguationCandidates = disambiguationCandidates; }
}
