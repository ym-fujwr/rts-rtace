package com.rts_trace.diffinfo;

import java.util.List;

public class DiffInfo {
    private String className;
    private List<DiffLineInfo> lineInfo;

    public DiffInfo(String className, List<DiffLineInfo> lineInfo) {
        this.className = className;
        this.lineInfo = lineInfo;
    }
    public String getClassName(){
        return this.className;
    }
    public List<DiffLineInfo> getLineInfo(){
        return this.lineInfo;
    }
}
