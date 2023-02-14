package com.rts_trace.dependency.info;

import java.util.List;

public class ReadDataIdInfo {
    private List<List<String>> ids;
    private List<String> range;

    public ReadDataIdInfo(List<List<String>> ids, List<String> range) {
        this.ids = ids;
        this.range = range;
    }

    public List<List<String>> getIds() {
        return this.ids;
    }

    public List<String> getRange() {
        return this.range;
    }
}