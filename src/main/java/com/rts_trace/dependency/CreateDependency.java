package com.rts_trace.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateDependency {

    private File d = new File("data/selogger/dataids.txt");
    //private File m = new File("data/selogger/methods.txt");
    private File c = new File("data/selogger/classes.txt");
    private List<String> classes = readClasses(c);
    //private List<String> methods = readMethods(m);

    
    public void startCreate() {
        List<TestInfo> test = new ArrayList<TestInfo>();
        ReadDataIdResult result = readDataId(d);
        List<List<String>> ids = result.getIds();
        List<String> range = result.getRange();

        File f1 = new File("data/selogger/");
        int eventFreqNum = f1.list().length - 7;
        for (int i = 1; i < eventFreqNum; i++) {
            test.add(getTestInfo(i, ids, range));
        }
        /*ファイルの中身削除してる */
        try {
            FileOutputStream fos1 = new FileOutputStream("data/json/dependency.txt", false);
            FileOutputStream fos2 = new FileOutputStream("data/json/testMethod.txt", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         * ListをJSON形式でファイルに出力
         */
        ObjectMapper objectMapper = new ObjectMapper();
        for(TestInfo t : test){
            try {
                String testNameJson = objectMapper.writeValueAsString(t);
                String classInfoJson = objectMapper.writeValueAsString(t.classInfoList);
                try {
                    File dFile = new File("data/json/dependency.txt");
                    File tFile = new File("data/json/testMethod.txt");
                    FileWriter filewriter1 = new FileWriter(dFile,true);
                    FileWriter filewriter2 = new FileWriter(tFile,true);
                    filewriter1.write(classInfoJson);
                    filewriter2.write(testNameJson);
                    filewriter1.close();
                    filewriter2.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

    }

    public ReadDataIdResult readDataId(File f) {
        List<List<String>> ids = new ArrayList<List<String>>();
        List<String> range = new ArrayList<>();
        try {
            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String content;
                List<String> token = new ArrayList<String>();
                String currentId = "";
                while ((content = br.readLine()) != null) {
                    token = Arrays.asList(content.split(","));
                    List<String> newToken = new ArrayList<>(token);
                    ids.add(newToken);
                    /*
                     * クラスに該当するdataIDのレンジを求める
                     * クラスの始まりのIDを管理．
                     */
                    if (!newToken.get(1).equals(currentId)) {
                        currentId = newToken.get(1);
                        range.add(newToken.get(0));
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        ReadDataIdResult result = new ReadDataIdResult(ids, range);
        return result;
    }

    public List<String> readClasses(File f) {
        List<String> c = new ArrayList<String>();
        try {
            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String content;
                List<String> token = new ArrayList<String>();
                while ((content = br.readLine()) != null) {
                    token = Arrays.asList(content.split(","));
                    c.add(token.get(2));
                }
                br.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return c;
    }

    /* クラス名#メソッド名で返す．リストのインデックスがmethodId */
    public List<String> readMethods(File f) {
        List<String> c = new ArrayList<String>();
        try {
            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String content;
                List<String> token = new ArrayList<String>();
                while ((content = br.readLine()) != null) {
                    token = Arrays.asList(content.split(","));
                    c.add(token.get(2) + "#" + token.get(3));
                }
                br.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return c;
    }

    public TestInfo getTestInfo(int i, List<List<String>> ids, List<String> range) {
        List<String> idOnly = new ArrayList<>();
        idOnly = getIdOnly(i);
        /* dataIDからクラス名と行数を取得．クラス単位で． */
        List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
        List<String> lines = new ArrayList<>();
        String currentClassId = getClassId(idOnly.get(0), range);

        /*テストメソッド名を加工 */
        String t = ids.get(Integer.parseInt(idOnly.get(0)) - 1).get(7);
        String testMethodName = t.substring(0,t.indexOf("#",t.indexOf("#")+1));

        /*初めの行　どうせ0 */
        //lines.add(ids.get(Integer.parseInt(idOnly.get(0))).get(3));


        for (int j = 1; j < idOnly.size(); j++) {
            String classId = getClassId(idOnly.get(j), range);
            if (currentClassId.equals(classId)) {
                /* 行数をclassinfoのlistに入れる */
                lines.add(ids.get(Integer.parseInt(idOnly.get(j))).get(3));
            } else {
                /* 新たなclassinfoを作り，行数をlistに入れる */
                Set<String> tmpSet = new LinkedHashSet<String>(lines);
                List<String> lines2 = new ArrayList<String>(tmpSet);
                ClassInfo ci = new ClassInfo(classes.get(Integer.parseInt(currentClassId)),lines2);
                classInfoList.add(ci);
                lines.clear();
                lines.add(ids.get(Integer.parseInt(idOnly.get(j))).get(3));
                currentClassId = classId;
            }
        }
        /*最後のクラスの情報を格納 */
        Set<String> tmpSet = new LinkedHashSet<String>(lines);
        List<String> lines2 = new ArrayList<String>(tmpSet);
        ClassInfo ci = new ClassInfo(classes.get(Integer.parseInt(currentClassId)), lines2);
        classInfoList.add(ci);


        TestInfo result = new TestInfo(testMethodName, classInfoList);
        return result;
    }

    public List<String> getIdOnly(int i) {
        List<String> result = new ArrayList<>();
        try {
            String origin = "data/selogger/eventfreq-";
            Integer ii = Integer.valueOf(i);
            String filePath = origin + ii.toString() + ".txt";
            File f = new File(filePath);
            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String content;
                String[] token;
                while ((content = br.readLine()) != null) {
                    token = content.split(",");
                    result.add(token[0]);
                }
                br.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }


    /* dataidから該当するクラスのIDを返す */
    public String getClassId(String id, List<String> range) {
        for (int i = 0; i < classes.size() - 1; i++) {
            if ((Integer.parseInt(range.get(i)) <= Integer.parseInt(id))
                    & (Integer.parseInt(id) < Integer.parseInt(range.get(i + 1)))) {
                return Integer.toString(i);
            }
        }
        return Integer.toString(classes.size() - 1);
    }

    public class TestInfo {
        private String testName;
        private List<ClassInfo> classInfoList;

        TestInfo(String testName, List<ClassInfo> classInfoList) {
            this.testName = testName;
            this.classInfoList = classInfoList;
        }

        public String getTestName() {
            return this.testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public void setClass(List<ClassInfo> classInfoList) {
            this.classInfoList = classInfoList;
        }
    }

    public class ClassInfo {
        private String className;
        private List<String> line;

        ClassInfo(String className, List<String> line) {
            this.className = className;
            this.line = line;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setLine(List<String> line) {
            this.line = line;
        }

        public String getClassName() {
            return this.className;
        }

        public List<String> getLine() {
            return this.line;
        }
    }

    public class ReadDataIdResult {
        private List<List<String>> ids;
        private List<String> range;

        ReadDataIdResult(List<List<String>> ids, List<String> range) {
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
}
