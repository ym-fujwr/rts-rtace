package com.rts_trace.dependency.info;

import java.util.List;

public class TestInfo {
    private String testName;
    private List<ClassInfo> classInfoList;

    public TestInfo(String testName, List<ClassInfo> classInfoList) {
        this.testName = testName;
        this.classInfoList = classInfoList;
    }

    public TestInfo(){
        
    }

    public String getTestName() {
        return this.testName;
    }

    public List<ClassInfo> getClassInfoList() {
        return this.classInfoList;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setClass(List<ClassInfo> classInfoList) {
        this.classInfoList = classInfoList;
    }
}