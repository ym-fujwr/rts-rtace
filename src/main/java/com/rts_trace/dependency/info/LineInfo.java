package com.rts_trace.dependency.info;

import java.util.List;

public class LineInfo {
    private String className;
    private List<String> startLine;
    private List<String> numValue;

    public LineInfo(String className, List<String> startLine, List<String> numValue) {
        this.className = className;
        this.startLine = startLine;
        this.numValue = numValue;
    }

    public String getClassName() {
        return this.className;
    }

    public List<String> getStartLine() {
        return this.startLine;
    }

    public List<String> getNumValue() {
        return this.numValue;
    }
}
