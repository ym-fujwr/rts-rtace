package com.rts_trace.dependency.info;

import java.util.List;

public class FieldVarInfo {
    private String varName;
    private List<ClassInfo> classInfo;

    public FieldVarInfo(String varName,List<ClassInfo> classInfo){
        this.varName = varName;
        this.classInfo = classInfo;
    }
    public FieldVarInfo(){}
    
    public String getVarName(){
        return this.varName;
    }
    public List<ClassInfo> getClassInfo(){
        return this.classInfo;
    }
    public void setClassInfo(List<ClassInfo> classInfo){
        this.classInfo = classInfo;
    }

}
