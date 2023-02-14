package com.rts_trace.dependency.info;

import java.util.List;

public class ClassInfo {
    private String className;
    private List<String> line;

    public ClassInfo(String className, List<String> line) {
        this.className = className;
        this.line = line;
    }
    public ClassInfo(){
        
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setLine(List<String> line) {
        this.line = line;
    }

    public void setLineEle(int idx,String element){
        this.line.set(idx,element);
    }
    public String getClassName() {
        return this.className;
    }

    public List<String> getLine() {
        return this.line;
    }
}