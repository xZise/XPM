package com.nijiko.permissions;

class CheckResult {
    private final Entry source;
    private final String mostRelevantNode;
    private final Entry checked;
    private final String node;
    private final boolean result;
    private boolean valid;
    
    public CheckResult(Entry source, String mrn, Entry checked, String node, boolean result) {
        this.source = source;
        this.mostRelevantNode = mrn;
        this.checked = checked;
        this.node = node;
        this.result = result;
        this.valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    public Entry getSource() {
        return source;
    }

    public String getMostRelevantNode() {
        return mostRelevantNode;
    }

    public Entry getChecked() {
        return checked;
    }

    public String getNode() {
        return node;
    }

    public boolean getResult() {
        return result;
    }
    
    public CheckResult getNegated() {
        if(!valid) return null;
        return new CheckResult(source, mostRelevantNode, checked, node,!result);
    }
}