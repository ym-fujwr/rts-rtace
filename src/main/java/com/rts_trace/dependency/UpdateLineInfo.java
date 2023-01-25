package com.rts_trace.dependency;

import java.util.List;

import com.rts_trace.diffinfo.DiffInfo;
import com.rts_trace.diffinfo.GetDiffInfo;

public class UpdateLineInfo  {
    GetDiffInfo g = new GetDiffInfo();
    List<DiffInfo> diffInfo =  g.getDiff();

    /*
     * TODO
     * git diff　から行数情報取得して，行数を更新
     * ・クラス名<>　
     *      ・行数情報<>
     *          ・何行目か
     *          ・何行分+するか（もしくは-にするか）
     */ 
}
