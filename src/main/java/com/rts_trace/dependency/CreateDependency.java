package com.rts_trace.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rts_trace.dependency.info.ClassInfo;
import com.rts_trace.dependency.info.ReadDataIdInfo;
import com.rts_trace.dependency.info.TestInfo;

public class CreateDependency {

    private File c = new File("data/selogger/classes.txt");
    private List<String> classes = readClasses(c);

    public void startCreate() {
        File d = new File("data/selogger/dataids.txt");
        List<TestInfo> test = new ArrayList<TestInfo>();
        ReadDataIdInfo result = readDataId(d);
        List<List<String>> ids = result.getIds();
        List<String> range = result.getRange();
        File f1 = new File("data/selogger/");
        int eventFreqNum = f1.list().length - 7;
        File dep = new File("data/json/dependency.json");
        List<TestInfo> dependency = null;

        for (int i = 1; i < eventFreqNum; i++) {
            TestInfo tmp = getTestInfo(i, ids, range);
            /*
             * テスト情報に重複があれば，既存の情報に追加する．
             */
            for(int j=0;j<test.size();j++){
                //テストケース情報が被っているか？
                if(test.get(j).getTestName().equals(tmp.getTestName())){
                    for(int k=0;k<tmp.getClassInfoList().size();k++){
                        for(int l=0;l<test.get(j).getClassInfoList().size();l++){
                            //呼び出し先のクラスが被っているか？
                            if(tmp.getClassInfoList().get(k).getClassName().equals(test.get(j).getClassInfoList().get(l).getClassName())){
                                /*重複箇所のライン情報を重複を除いて結合 */
                                if(!tmp.getClassInfoList().get(k).getLine().equals(test.get(j).getClassInfoList().get(l).getLine())){
                                    List<String> lineTmp = Stream.concat(tmp.getClassInfoList().get(k).getLine().stream(), test.get(j).getClassInfoList().get(l).getLine().stream())
                                    .distinct()
                                    .sorted(Comparator.naturalOrder())
                                    .collect(Collectors.toList());
                                    test.get(j).getClassInfoList().get(l).setLine(lineTmp);
                                    break;
                                }
                            }
                            if(l==test.get(j).getClassInfoList().size()-1){
                                //呼び出し先のクラスが被っていなかった場合．
                                test.get(j).getClassInfoList().add(tmp.getClassInfoList().get(k));
                                break;
                            }
                        }
                    }
                    break;
                }
                if(j==test.size()-1){//テストケース情報が被っていなかった場合．
                    test.add(tmp);
                    break;
                }
            }
            if(test.size()==0){//1回目
                test.add(tmp);
            }
        }

        /* 2回目以降の更新フェーズの場合． */
        if (dep.exists()) {
            /*
             * dependency.jsonの中身を取得
             */
            ObjectMapper objectMapper = new ObjectMapper();
            Path dependencyPath = Paths.get("data/json/dependency.json");
            try {
                String dependencyJson = Files.readString(dependencyPath);
                dependency = Arrays.asList(objectMapper.readValue(dependencyJson, TestInfo[].class));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            /*
             * 更新
             */
            for (TestInfo t : test) {
                for (int i=0; i<dependency.size();i++) {
                    if (t.getTestName().equals(dependency.get(i).getTestName())) {
                        dependency.set(i,t);
                        break;
                    }
                }
            }
            /*
             * デバッグ用
             * 更新するテスト情報を取得している
             */
            String tPath = "data/json/update.json";
            String testNameJson = "[";
            try {
                File f = new File(tPath);
                FileWriter filewriter2 = new FileWriter(f, true);
                for (TestInfo t : test) {
                    testNameJson += objectMapper.writeValueAsString(t);
                    testNameJson += ",";
                }
                filewriter2.write(testNameJson.substring(0, testNameJson.length() - 1) + "]");
                filewriter2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            dependency = test;
        }

        /* dependency.jsonのファイルの中身とseloggerのファイル削除してる */
        try {
            FileOutputStream fos1 = new FileOutputStream("data/json/dependency.json", false);
            fos1.close();
            File selogger = new File("data/selogger/");
            File[] files = selogger.listFiles();
            for(int i=0; i<files.length; i++) {
                files[i].delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * ListをJSON形式でファイルに出力
         */
        ObjectMapper objectMapper = new ObjectMapper();

        String tPath = "data/json/dependency.json";
        String testNameJson = "[";
        try {
            File f = new File(tPath);
            FileWriter filewriter2 = new FileWriter(f, true);
            for (TestInfo dd : dependency) {
                testNameJson += objectMapper.writeValueAsString(dd);
                testNameJson += ",";
            }
            filewriter2.write(testNameJson.substring(0, testNameJson.length() - 1) + "]");
            filewriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ReadDataIdInfo readDataId(File f) {
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
        ReadDataIdInfo result = new ReadDataIdInfo(ids, range);
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

        /* テストメソッド名を加工 */
        String t = null;
        String testMethodName = null;
        t = ids.get(Integer.parseInt(idOnly.get(0)) - 1).get(7);
        if (t.indexOf("test") > -1) {
            testMethodName = t.substring(0, t.indexOf("#", t.indexOf("#") + 1));
        }

        for (int j = 1; j < idOnly.size(); j++) {
            String classId = getClassId(idOnly.get(j), range);
            if (currentClassId.equals(classId)) {
                /* 行数をclassinfoのlistに入れる */
                lines.add(ids.get(Integer.parseInt(idOnly.get(j))).get(3));
            } else {
                /* 新たなclassinfoを作り，行数をlistに入れる */
                Set<String> tmpSet = new LinkedHashSet<String>(lines);
                List<String> lines2 = new ArrayList<String>(tmpSet);
                Collections.sort(lines2, (s1, s2) -> Integer.parseInt(s1) - Integer.parseInt(s2));
                ClassInfo ci = new ClassInfo(classes.get(Integer.parseInt(currentClassId)), lines2);
                classInfoList.add(ci);
                lines.clear();
                lines.add(ids.get(Integer.parseInt(idOnly.get(j))).get(3));
                currentClassId = classId;
            }
            /*
             * 今探索中のテストメソッドかどうか確認．
             */
            if (ids.get(Integer.parseInt(idOnly.get(j))).get(5).equals("METHOD_ENTRY")) {
                t = ids.get(Integer.parseInt(idOnly.get(j)) - 1).get(7);
                if (t.indexOf("#test") > -1) {
                    testMethodName = t.substring(0, t.indexOf("#", t.indexOf("#") + 1));
                }
            }
        }
        /* 最後のクラスの情報を格納 */
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

}
