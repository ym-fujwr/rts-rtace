package com.rts_trace.dependency;

import java.util.ArrayList;
import java.util.List;

import com.rts_trace.dependency.info.LineInfo;
import com.rts_trace.diffinfo.DiffInfo;
import com.rts_trace.diffinfo.DiffLineInfo;
import com.rts_trace.diffinfo.GetDiffInfo;

public class UpdateLineInfo  {
    GetDiffInfo g = new GetDiffInfo();
    List<DiffInfo> diffInfo =  g.getDiff();
    public void startUpdateLineInfo(){
        List<LineInfo> lineInfo = getLineInfo();

        for(DiffInfo d : diffInfo){
            for(DiffLineInfo dl : d.getLineInfo()){
                System.out.println(dl.getPreStartLine()+","+dl.getPreHunkLine()+","+dl.getCurStartLine()+","+dl.getCurHunkLine());
            }
        }
        for(LineInfo l : lineInfo){
            System.out.println(l.getClassName());
            System.out.println(l.getStartLine());
            System.out.println(l.getNumValue());
        }
        
    }
    public List<LineInfo> getLineInfo(){
        List<LineInfo> result = new ArrayList<>();

        for(DiffInfo d : diffInfo){
            String className = d.getClassName();
            List<String> startLineTmp = new ArrayList<>();
            List<String> numValueTmp = new ArrayList<>();
            for(DiffLineInfo dl : d.getLineInfo()){
                if(!dl.getCurHunkLine().equals("0") && dl.getPreHunkLine().equals("0")){
                    /*
                     * 追加のみの場合．
                     * 追加行の始まり(gurStartLine)以降の行は，
                     * 追加された行数(curHunkLine) + 
                     * だけ変更する． 
                     */
                    if(numValueTmp.isEmpty()){
                        startLineTmp.add(dl.getCurStartLine());
                        numValueTmp.add(dl.getCurHunkLine());
                    }else{
                        int cstmp = Integer.parseInt(dl.getCurStartLine()) - Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1));
                        startLineTmp.add((Integer.valueOf(cstmp)).toString());
                        int cntmp = Integer.parseInt(dl.getCurHunkLine())  + Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1));
                        numValueTmp.add(Integer.valueOf(cntmp).toString());
                    }
                }
                else if(dl.getCurHunkLine().equals("0") && !dl.getPreHunkLine().equals("0")){
                    /*
                     * 削除のみの場合
                     * 削除されたHunkの次の行以降は，
                     * これまでの変更量(numValueTmpの直近の値) - 削除された行数(preHunkLine) 
                     * だけ変更する．
                     */
                    int pstmp = Integer.parseInt(dl.getPreStartLine()) + Integer.parseInt(dl.getPreHunkLine());
                    if(numValueTmp.isEmpty()){
                        startLineTmp.add(Integer.valueOf(pstmp).toString());
                        numValueTmp.add("-"+dl.getPreHunkLine());
                    }else{
                        startLineTmp.add(Integer.valueOf(pstmp).toString());
                        int pntmp = Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1)) - Integer.parseInt(dl.getPreHunkLine());
                        numValueTmp.add(Integer.valueOf(pntmp).toString());
                    }
                }else{
                    /*
                     * 追加と削除が同時に行われた場合
                     * 削除されたHunkの次の行以降は，
                     * これまでの変更量(numValueTmpの直近の値) - 削除された行数(preHunkLine) + 追加された行数(curHunkLine)
                     * だけ変更する．
                     */
                    int stmp = Integer.parseInt(dl.getPreStartLine()) + Integer.parseInt(dl.getPreHunkLine());
                    int ntmp = Integer.parseInt(dl.getCurHunkLine()) + Integer.parseInt(numValueTmp.get(numValueTmp.size() - 1)) - Integer.parseInt(dl.getPreHunkLine());
                    startLineTmp.add(Integer.valueOf(stmp).toString());
                    numValueTmp.add(Integer.valueOf(ntmp).toString());
                }
            }
            LineInfo tmp = new LineInfo(className,startLineTmp,numValueTmp);
            result.add(tmp);
        }
        return result;
    }
}
