package com.rts_trace.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rts_trace.dependency.info.ClassInfo;
import com.rts_trace.dependency.info.FieldVarInfo;
import com.rts_trace.dependency.info.ReadDataIdInfo;
import com.rts_trace.dependency.info.TestInfo;


public class CreateDependency {

    private File c = new File("data/selogger/classes.txt");
    private List<String> classes = readClasses(c);
    private Json j = new Json();
    public void startCreate() {
        File d = new File("data/selogger/dataids.txt");
        File v = new File("data/json/fieldVarInfo.json");
        List<TestInfo> test = new ArrayList<TestInfo>();
        ReadDataIdInfo result = readDataId(d);
        List<List<String>> ids = result.getIds();
        List<FieldVarInfo> fieldVarInfo = null;
        List<FieldVarInfo> fieldVarInfoTmp = getFieldVarInfo(ids);
        List<String> range = result.getRange();
        File f1 = new File("data/selogger/");
        int eventFreqNum = f1.list().length - 7;
        File dep = new File("data/json/dependency.json");
        List<TestInfo> dependency = null;

        /* 
         * fieldVarInfoが既にあれば更新．
         */
        if(v.exists()){
            fieldVarInfo = j.readFieldVarInfoJson();
            for(FieldVarInfo f:fieldVarInfo){
                for(FieldVarInfo ff : fieldVarInfoTmp){
                    if(f.getVarName() == ff.getVarName()){
                        f.setClassInfo(ff.getClassInfo());
                    }
                }
            }
        }else{
            fieldVarInfo = fieldVarInfoTmp;
        }
        /*
         * fieldVarInfo の中身削除
         */
        try {
            FileOutputStream fos1 = new FileOutputStream("data/json/fieldVarInfo.json", false);
            fos1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        j.writeFiledVarInfoJson(fieldVarInfo);
        
        
        for (int i = 1; i < eventFreqNum; i++) {
            TestInfo tmp = getTestInfo(i, ids, range,fieldVarInfo);
            /*
             * テスト情報に重複があれば，既存の情報に追加する．
             */
            for(int j=0;j<test.size();j++){
                if(test.get(j).getTestName().equals(tmp.getTestName())){
                    for(int k=0;k<tmp.getClassInfoList().size();k++){
                        for(int l=0;l<test.get(j).getClassInfoList().size();l++){
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
            /* dependency.jsonの中身 */
            dependency = j.readDependencyJson();
            /* depencencyを更新 */
            for (TestInfo t : test) {
                for (int i=0; i<dependency.size();i++) {
                    if (t.getTestName().equals(dependency.get(i).getTestName())) {
                        dependency.set(i,t);
                        break;
                    }
                }
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
         j.writeDependencyJson(dependency);
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

    /* 
     * クラス名#メソッド名で返す．リストのインデックスがmethodId 
     */
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

    public TestInfo getTestInfo(int i, List<List<String>> ids, List<String> range,List<FieldVarInfo> fieldVarInfo) {
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
 
            /* 今探索中のテストメソッドかどうか確認． */
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

        /*
         * 追加すべきフィールド変数情報を特定
         */
        List<Integer> idx = new ArrayList<>();
        for(int j=0;j<fieldVarInfo.size();j++){
            LOOPF:for(ClassInfo fc:fieldVarInfo.get(j).getClassInfo()){
                for(ClassInfo c:classInfoList){
                    if(fc.getClassName().equals(c.getClassName())){
                        for(String l:c.getLine()){
                            if(fc.getLine().contains(l)){
                                idx.add(j);
                                break LOOPF;
                            }
                        }
                    }
                }
            }
        }
        /*
         * フィールド変数情報を追加．
         */
        for(Integer x:idx){
            for(ClassInfo f:fieldVarInfo.get(x).getClassInfo()){
                int flag = 0;
                for(int j=0;j<classInfoList.size();j++){
                    if(f.getClassName().equals(classInfoList.get(j).getClassName())){
                        //重複除いて行情報を結合
                        List<String> lineTmp = Stream.concat(f.getLine().stream(), classInfoList.get(j).getLine().stream())
                        .distinct()
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList());
                        classInfoList.get(j).setLine(lineTmp);
                        flag = 1;
                        break;
                    }
                }
                if(flag == 0){
                    classInfoList.add(f);
                }
            }
        }
        TestInfo result = new TestInfo(testMethodName, classInfoList);
        return result;
    }

    /*
     * eventfreq (dataid,実行回数)　から，dataidだけ取ってきて返す．
     */
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

    /*
     * dataidから該当するクラスのIDを返す 
     */
    public String getClassId(String id, List<String> range) {
        for (int i = 0; i < classes.size() - 1; i++) {
            if ((Integer.parseInt(range.get(i)) <= Integer.parseInt(id))
                    & (Integer.parseInt(id) < Integer.parseInt(range.get(i + 1)))) {
                return Integer.toString(i);
            }
        }
        return Integer.toString(classes.size() - 1);
    }


    /*
     * フィールド変数名とそのフィールド変数を呼び出している箇所の情報（クラス名，行数）を取得
     */
    public List<FieldVarInfo> getFieldVarInfo(List<List<String>> ids){
        List<FieldVarInfo> result = new ArrayList<>();
        for(List<String> id :ids){
            if(id.get(5).indexOf("FIELD")>-1){
                String varName;
                if(id.get(5).indexOf("FIELD_")>-1 && id.get(5).indexOf("INITIALIZATION")==-1){
                    varName = id.get(9).substring(10);
                }else{
                    varName = id.get(8).substring(10);
                }
                String className = classes.get(Integer.parseInt(id.get(1)));
                if(result.size()==0){
                    List<String> lines = new ArrayList<>();
                    lines.add(id.get(3));
                    ClassInfo c = new ClassInfo(className,lines);
                    List<ClassInfo> ctmp = new ArrayList<>();
                    ctmp.add(c);
                    FieldVarInfo tmp = new FieldVarInfo(varName,ctmp);
                    result.add(tmp);
                }else{
                    int varNameFlag = 0;
                    LOOP_RESULT:for(int i = 0;i<result.size();i++){
                        if(varName.equals(result.get(i).getVarName())){
                            varNameFlag = 1;
                            int classNameFlag = 0;
                            for(int j=0;j<result.get(i).getClassInfo().size();j++){
                                if(className.equals(result.get(i).getClassInfo().get(j).getClassName())){
                                    /*該当するクラスの行数情報に追加 */
                                    List<String> ltmp = result.get(i).getClassInfo().get(j).getLine();
                                    ltmp.add(id.get(3));
                                    Collections.sort(ltmp, (s1, s2) -> Integer.parseInt(s1) - Integer.parseInt(s2));
                                    /* 重複を取り除く */
                                    List<String> lines = new ArrayList<String>(new LinkedHashSet<>(ltmp));
                                    result.get(i).getClassInfo().get(j).setLine(lines);
                                    classNameFlag = 1;
                                    break LOOP_RESULT;
                                }
                            }
                            if(classNameFlag == 0){
                                /* 該当するクラスがなかった場合，ClassInfoを作って格納 */
                                List<String> lines = new ArrayList<>();
                                lines.add(id.get(3));
                                ClassInfo ctmp = new ClassInfo(className,lines);
                                List<ClassInfo> citmp = result.get(i).getClassInfo();
                                citmp.add(ctmp);
                                result.get(i).setClassInfo(citmp);
                                break;
                            }
                        }                        
                    }
                    if(varNameFlag == 0){
                        /* 該当する変数名が存在しない場合，新しくFieldVarInfoを作成し，resultに追加． */
                        List<String> lines = new ArrayList<>();
                        lines.add(id.get(3));
                        ClassInfo c = new ClassInfo(className,lines);
                        List<ClassInfo> ctmp = new ArrayList<>();
                        ctmp.add(c);
                        FieldVarInfo tmp = new FieldVarInfo(varName,ctmp);
                        result.add(tmp);
                    }
                }
            }
        }
        return result;
    }
}
