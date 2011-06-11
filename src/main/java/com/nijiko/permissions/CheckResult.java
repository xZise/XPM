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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((checked == null) ? 0 : checked.hashCode());
        result = prime * result + ((mostRelevantNode == null) ? 0 : mostRelevantNode.hashCode());
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + (valid ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CheckResult other = (CheckResult) obj;
        if (checked == null) {
            if (other.checked != null)
                return false;
        } else if (!checked.equals(other.checked))
            return false;
        if (mostRelevantNode == null) {
            if (other.mostRelevantNode != null)
                return false;
        } else if (!mostRelevantNode.equals(other.mostRelevantNode))
            return false;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (valid != other.valid)
            return false;
        return true;
    }
}