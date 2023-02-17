package com.rts_trace.selection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rts_trace.dependency.Json;
import com.rts_trace.dependency.info.ClassInfo;
import com.rts_trace.dependency.info.FieldVarInfo;
import com.rts_trace.dependency.info.TestInfo;
import com.rts_trace.diffinfo.DiffInfo;
import com.rts_trace.diffinfo.DiffLineInfo;

public class TestSelect {
    private File gitDiffFile = new File("data/gitdiff.txt");
    List<String> fieldVar = null;
    Json j = new Json();
    List<FieldVarInfo> fieldVarInfo = j.readFieldVarInfoJson();
    public void startSelect() {
        List<DiffInfo> gitDiff = getGitDiff();
        List<String> executeTest = selectTest(gitDiff);
        
        try {
            File f = new File("test.txt");
            FileWriter fw = new FileWriter(f, false);
            String result = "";
            for (String s : executeTest) {
                result = result + s + ",";
            }
            fw.write(result.replace("/", "."));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * git diff から変更のあったクラス名と行数を取得．
     * 
     * @@ a,b c,d @@
     */
    public List<DiffInfo> getGitDiff() {
        List<DiffInfo> result = new ArrayList<>();
        List<DiffLineInfo> lineInfo = new ArrayList<>();
        String className = null;
        String a;
        String b;
        String c;
        String d;
        try {
            if (gitDiffFile.exists()) {
                FileReader fr = new FileReader(gitDiffFile);
                BufferedReader br = new BufferedReader(fr);
                String content;
                List<String> token = new ArrayList<String>();

                while ((content = br.readLine()) != null) {
                    if (content.indexOf("---") > -1) {
                        if (className == null) {
                            className = content.substring(content.indexOf("/") + 1);
                        } else {
                            List<DiffLineInfo> newLineInfo = new ArrayList<>(lineInfo);
                            DiffInfo diffInfoTmp = new DiffInfo(className, newLineInfo);
                            result.add(diffInfoTmp);
                            className = content.substring(content.indexOf("/") + 1);
                            lineInfo.clear();
                        }
                    } else if (content.indexOf("@@") == 0) {/* 変更箇所を取得 */
                        token = Arrays.asList(content.split("[, ]"));
                        a = token.get(1).substring(1);
                        if (token.get(2).indexOf("+") > -1) {
                            b = "0";
                            c = token.get(2).substring(1);
                            if (token.get(3).indexOf("@@") > -1) {
                                d = "0";
                            } else {
                                d = token.get(3);
                            }
                        } else {
                            b = token.get(2);
                            c = token.get(3).substring(1);
                            if (token.get(4).indexOf("@@") > -1) {
                                d = "0";
                            } else {
                                d = token.get(4);
                            }
                        }
                        DiffLineInfo diffLineInfoTmp = new DiffLineInfo(a, b, c, d);
                        lineInfo.add(diffLineInfoTmp);
                    }
                }
                br.close();
                //最後
                DiffInfo diffInfoTmp = new DiffInfo(className, lineInfo);
                result.add(diffInfoTmp);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

    public List<String> selectTest(List<DiffInfo> gitDiff) {
        List<String> tmp = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Path dependencyPath = Paths.get("data/json/dependency.json");
        List<TestInfo> dependency = null;
        try {
            String dependencyJson = Files.readString(dependencyPath);
            dependency = Arrays.asList(objectMapper.readValue(dependencyJson, TestInfo[].class));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        for (TestInfo t : dependency) {
            /* gitdiffとdependencyを用いて関連するテストケースを抽出 */
            LOOP1: for (ClassInfo c : t.getClassInfoList()) {
                for (DiffInfo d : gitDiff) {
                    if (d.getClassName().indexOf(c.getClassName()) > -1) {
                        if (isContain(c.getLine(), d.getLineInfo())) {
                            /*
                             * 該当するテストケースを選択
                             * 必要ないループは抜ける
                             */
                            tmp.add(t.getTestName());
                            break LOOP1;
                        }
                    }
                }
            }
        }
        /* 重複を取り除く */
        List<String> result = new ArrayList<String>(new LinkedHashSet<>(tmp));
        return result;
    }

    public boolean isContain(List<String> lines, List<DiffLineInfo> diffLines) {
        /* gitdiff情報に関連する行数がaに含まれるかチェック */
        int diff = 0; //追加情報（行数）を変更前の行数にマッチングさせる．
        for (DiffLineInfo dl : diffLines) {
            int a = Integer.parseInt(dl.getPreStartLine());
            int b = Integer.parseInt(dl.getPreHunkLine());
            int c = Integer.parseInt(dl.getCurStartLine());
            int d = Integer.parseInt(dl.getCurHunkLine());
            /* 削除の場合 */
            if (b != 0) {
                for (String l : lines) {
                    if (a <= Integer.parseInt(l) && Integer.parseInt(l) <= a + b) {
                        return true;
                    }
                }
            }
            if (d != 0) {
                for (String l : lines) {
                    int i = 0;
                    if(c + diff < Integer.parseInt(l)){ //行数は昇順に並んでいる．lより小さかったら以降の処理はいらない．
                        break;
                    }
                    while (c + diff- i > 0) {
                        if (c + diff - i == Integer.parseInt(l)) {
                            return true;
                        }
                        if(c + diff - i < Integer.parseInt(l)){
                            break;
                        }
                        i++;
                    }
                }
            }
            diff = diff - d + b;
        }
        return false;
    }

}
