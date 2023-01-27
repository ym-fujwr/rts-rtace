package com.rts_trace.dependency;

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
import com.rts_trace.dependency.info.LineInfo;
import com.rts_trace.dependency.info.TestInfo;
import com.rts_trace.diffinfo.DiffInfo;
import com.rts_trace.diffinfo.DiffLineInfo;
import com.rts_trace.diffinfo.GetDiffInfo;

public class UpdateLineInfo {
    GetDiffInfo g = new GetDiffInfo();
    List<DiffInfo> diffInfo = g.getDiff();

    public void startUpdateLineInfo() {
        List<LineInfo> lineInfo = getLineInfo();
        Update(lineInfo);
    }

    public List<LineInfo> getLineInfo() {
        List<LineInfo> result = new ArrayList<>();

        for (DiffInfo d : diffInfo) {
            String className = d.getClassName();
            List<String> startLineTmp = new ArrayList<>();
            List<String> numValueTmp = new ArrayList<>();
            for (DiffLineInfo dl : d.getLineInfo()) {
                if (!dl.getCurHunkLine().equals("0") && dl.getPreHunkLine().equals("0")) {
                    /*
                     * 追加のみの場合．
                     * 追加行の始まり(gurStartLine)以降の行は，
                     * 追加された行数(curHunkLine) +
                     * だけ変更する．
                     */
                    if (numValueTmp.isEmpty()) {
                        startLineTmp.add(dl.getCurStartLine());
                        numValueTmp.add(dl.getCurHunkLine());
                    } else {
                        int cstmp = Integer.parseInt(dl.getCurStartLine())
                                - Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1));
                        startLineTmp.add((Integer.valueOf(cstmp)).toString());
                        int cntmp = Integer.parseInt(dl.getCurHunkLine())
                                + Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1));
                        numValueTmp.add(Integer.valueOf(cntmp).toString());
                    }
                } else if (dl.getCurHunkLine().equals("0") && !dl.getPreHunkLine().equals("0")) {
                    /*
                     * 削除のみの場合
                     * 削除されたHunkの次の行以降は，
                     * これまでの変更量(numValueTmpの直近の値) - 削除された行数(preHunkLine)
                     * だけ変更する．
                     */
                    int pstmp = Integer.parseInt(dl.getPreStartLine()) + Integer.parseInt(dl.getPreHunkLine());
                    if (numValueTmp.isEmpty()) {
                        startLineTmp.add(Integer.valueOf(pstmp).toString());
                        numValueTmp.add("-" + dl.getPreHunkLine());
                    } else {
                        startLineTmp.add(Integer.valueOf(pstmp).toString());
                        int pntmp = Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1))
                                - Integer.parseInt(dl.getPreHunkLine());
                        numValueTmp.add(Integer.valueOf(pntmp).toString());
                    }
                } else {
                    /*
                     * 追加と削除が同時に行われた場合
                     * 削除されたHunkの次の行以降は，
                     * これまでの変更量(numValueTmpの直近の値) - 削除された行数(preHunkLine) + 追加された行数(curHunkLine)
                     * だけ変更する．
                     */
                    int stmp = Integer.parseInt(dl.getPreStartLine()) + Integer.parseInt(dl.getPreHunkLine());
                    int ntmp = Integer.parseInt(dl.getCurHunkLine())
                            + Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1))
                            - Integer.parseInt(dl.getPreHunkLine());
                    startLineTmp.add(Integer.valueOf(stmp).toString());
                    numValueTmp.add(Integer.valueOf(ntmp).toString());
                }
            }
            LineInfo tmp = new LineInfo(className, startLineTmp, numValueTmp);
            result.add(tmp);
        }
        return result;
    }


    public void Update(List<LineInfo> lineInfo){
        ObjectMapper objectMapper = new ObjectMapper();
        Path dependencyPath = Paths.get("data/json/dependency.json");
        List<TestInfo> dependency = null;
        try {
            String dependencyJson = Files.readString(dependencyPath);
            dependency = Arrays.asList(objectMapper.readValue(dependencyJson, TestInfo[].class));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        for(TestInfo t : dependency){
            for(ClassInfo c : t.getClassInfoList()){
                for(LineInfo l : lineInfo){
                    if(c.getClassName().equals(l.getClassName())){
                LOOPI:  for(int i=0;i<l.getNumValue().size()-1 ;i++){
                            for(int j=0;j<c.getLine().size();j++){
                                if(Integer.parseInt(c.getLine().get(j)) < Integer.parseInt(l.getStartLine().get(i))){
                                    continue;
                                } else if(Integer.parseInt(l.getStartLine().get(i))>=Integer.parseInt(c.getLine().get(j)) && Integer.parseInt(c.getLine().get(j))  < Integer.parseInt(l.getStartLine().get(i+1)) ){
                                    //更新
                                    int newLineTmp = Integer.parseInt(c.getLine().get(j))  + Integer.parseInt(l.getNumValue().get(i));
                                    c.setLineEle(i, Integer.valueOf(newLineTmp).toString());
                                } else if(Integer.parseInt(c.getLine().get(j)) >= Integer.parseInt(l.getStartLine().get(i))){
                                    break LOOPI;
                                }
                            }
                        }
                    }
                }
            }
        }
        //ファイルに書き込み
        String tPath = "data/json/updated.json";
        String testNameJson = "[";
        try {
            File f = new File(tPath);
            FileWriter filewriter2 = new FileWriter(f, true);
            for (TestInfo t : dependency) {
                testNameJson += objectMapper.writeValueAsString(t);
                testNameJson += ",";
            }
            filewriter2.write(testNameJson.substring(0, testNameJson.length() - 1) + "]");
            filewriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
