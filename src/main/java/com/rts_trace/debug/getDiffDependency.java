package com.rts_trace.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rts_trace.dependency.info.ClassInfo;
import com.rts_trace.dependency.info.TestInfo;

public class getDiffDependency {

    public void start(){
        ObjectMapper objectMapper = new ObjectMapper();
        Path dependencyPath1 = Paths.get("data/debug/lineInfoUpdated.json");
        Path dependencyPath2 = Paths.get("data/debug/updated.json");
        try {
            String dependencyJson1 = Files.readString(dependencyPath1);
            String dependencyJson2 = Files.readString(dependencyPath2);
            List<TestInfo> dep1 = Arrays.asList(objectMapper.readValue(dependencyJson1, TestInfo[].class));
            List<TestInfo> dep2 = Arrays.asList(objectMapper.readValue(dependencyJson2, TestInfo[].class));
            List<TestInfo> res = getDiff(dep1, dep2);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        
    }
    public List<TestInfo> getDiff(List<TestInfo> dep1,List<TestInfo> dep2){
        List<TestInfo> result = new ArrayList<>();
        for(int i=0;i<dep1.size();i++){
            List<ClassInfo> c1 = new ArrayList<>();
            List<ClassInfo> c2 = new ArrayList<>();
            for(int j=0;j<dep1.get(i).getClassInfoList().size();j++){
                if(!(dep1.get(i).getClassInfoList().get(j).getLine().equals(dep2.get(i).getClassInfoList().get(j).getLine()))){
                    try {
                        File f = new File("data/debug/diff.txt");
                        FileWriter filewriter2 = new FileWriter(f, true);
                        filewriter2.write(dep1.get(i).getTestName().toString());
                        filewriter2.write("\n");
                        filewriter2.write(dep1.get(i).getClassInfoList().get(j).getClassName());
                        filewriter2.write("\n");
                        filewriter2.write("更新後\n");
                        filewriter2.write(dep1.get(i).getClassInfoList().get(j).getLine().toString());
                        filewriter2.write("\n");
                        filewriter2.write("正しい情報\n");
                        filewriter2.write(dep2.get(i).getClassInfoList().get(j).getLine().toString());
                        filewriter2.write("\n");
                        filewriter2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c1.add(dep1.get(i).getClassInfoList().get(j));
                    c2.add(dep2.get(i).getClassInfoList().get(j));
                    
                    break;
                }
            }
            TestInfo tmp1 = new TestInfo(dep1.get(i).getTestName(),c1);
            TestInfo tmp2 = new TestInfo(dep1.get(i).getTestName(),c2);
            result.add(tmp1);
            result.add(tmp2);
        }
        return result;
    }
}
