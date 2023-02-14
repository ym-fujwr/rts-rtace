package com.rts_trace.dependency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rts_trace.dependency.info.FieldVarInfo;
import com.rts_trace.dependency.info.TestInfo;

public class Json {
    ObjectMapper objectMapper = new ObjectMapper();
    public void writeDependencyJson(List<TestInfo> dependency){
        String tPath = "data/json/dependency.json";
        String testNameJson = "[";
        try {
            File file = new File(tPath);
            FileWriter filewriter2 = new FileWriter(file, true);
            for (TestInfo d : dependency) {
                testNameJson += objectMapper.writeValueAsString(d);
                testNameJson += ",";
            }
            filewriter2.write(testNameJson.substring(0, testNameJson.length() - 1) + "]");
            filewriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeFiledVarInfoJson(List<FieldVarInfo> fieldVarInfo){
        String tPath = "data/json/fieldVarInfo.json";
        String testNameJson = "[";
        try {
            File file = new File(tPath);
            FileWriter filewriter2 = new FileWriter(file, true);
            for (FieldVarInfo f : fieldVarInfo) {
                testNameJson += objectMapper.writeValueAsString(f);
                testNameJson += ",";
            }
            filewriter2.write(testNameJson.substring(0, testNameJson.length() - 1) + "]");
            filewriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  List<TestInfo> readDependencyJson(){
        Path path = Paths.get("data/json/dependency.json");
        List<TestInfo> result = null;
        try {
            String json = Files.readString(path);
            result = Arrays.asList(objectMapper.readValue(json, TestInfo[].class));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

    public List<FieldVarInfo> readFieldVarInfoJson(){
        Path path = Paths.get("data/json/fieldVarInfo.json");
        List<FieldVarInfo> result = null;
        try {
            String Json = Files.readString(path);
            result = Arrays.asList(objectMapper.readValue(Json, FieldVarInfo[].class));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return result;
    }
}
