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
import com.rts_trace.dependency.CreateDependency.ClassInfo;
import com.rts_trace.dependency.CreateDependency.TestInfo;

public class TestSelect {
    private File gitDiffFile = new File("data/gitdiff.txt");

    public void startSelect() {
        List<DiffInfo> gitDiff = getDiff();
        List<String> executeTest = selectTest(gitDiff);

        try {
            File f = new File("test.txt");
            FileWriter fw = new FileWriter(f, false);
            for (String s : executeTest) {
                fw.write(s + "\n");
            }
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
    public List<DiffInfo> getDiff() {
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
                    if (d.className.indexOf(c.getClassName()) > -1) {
                        if (isContain(c.getLine(), d.lineInfo)) {
                            /*
                             * 該当するテストケースを選択
                             * 必要ないループは抜けるように処理記述
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
                    /*
                     * TODO
                     * 直前行が実行されているテストケースを選択する際，1行前だけでなく，それより前も探索する必要あり．
                     */
                    int i = 1;
                    while (c - i > 0) {
                        if (c - i == Integer.parseInt(l)) {
                            return true;
                        }
                        i++;
                    }
                }
            }

        }
        return false;
    }

    public class DiffInfo {
        private String className;
        private List<DiffLineInfo> lineInfo;

        DiffInfo(String className, List<DiffLineInfo> lineInfo) {
            this.className = className;
            this.lineInfo = lineInfo;
        }
    }

    /*
     * @@ a,b c,d @@
     * aは元ファイル始まり行
     * bは元ファイルのdiff hunkの行数
     * cは新ファイル始まり行
     * dは新ファイルのdiff hunkの行数
     */
    public class DiffLineInfo {
        private String preStartLine;
        private String preHunkLine;
        private String curStartLine;
        private String curHunkLine;

        DiffLineInfo(String preStartLine, String preHunkLine, String curStartLine, String curHunkLine) {
            this.preStartLine = preStartLine;
            this.preHunkLine = preHunkLine;
            this.curStartLine = curStartLine;
            this.curHunkLine = curHunkLine;
        }

        public String getPreStartLine() {
            return this.preStartLine;
        }

        public String getPreHunkLine() {
            return this.preHunkLine;
        }

        public String getCurStartLine() {
            return this.curStartLine;
        }

        public String getCurHunkLine() {
            return this.curHunkLine;
        }
    }
}
