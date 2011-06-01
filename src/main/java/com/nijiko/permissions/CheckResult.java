package com.nijiko.permissions;

class CheckResult {
    private final Entry source;
    private final String mostRelevantNode;
    private final Entry checked;
    private final String node;
    private boolean valid;
    
    public CheckResult(Entry source, String mrn, Entry checked, String node) {
        this.source = source;
        this.mostRelevantNode = mrn;
        this.checked = checked;
        this.node = node;
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
        return this.mostRelevantNode == null ? false : !this.mostRelevantNode.startsWith("-");
    }
        
    public CheckResult setChecked(Entry e) {
        if(!valid || e == null) return null;
        return new CheckResult(source, mostRelevantNode, e, node);        
    }
    
    @Override
    public String toString() {
        return "Checked: " + checked.toString() + " , Node: " + node + " , Source: " + source.toString() + " , MRN: " + mostRelevantNode + " , Valid: " + valid;
    }
    
    public CheckResult setNode(String node) {
        if(!valid || node == null) return null;
        return new CheckResult(source, mostRelevantNode, checked, node); 
    }
}