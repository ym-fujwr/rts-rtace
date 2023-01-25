package com.rts_trace.diffinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetDiffInfo {
    private File gitDiffFile = new File("data/gitdiff.txt");

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
                            b = "1";
                            c = token.get(2).substring(1);
                            if (token.get(3).indexOf("@@") > -1) {
                                d = "1";
                            } else {
                                d = token.get(3);
                            }
                        } else {
                            b = token.get(2);
                            c = token.get(3).substring(1);
                            if (token.get(4).indexOf("@@") > -1) {
                                d = "1";
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
}
