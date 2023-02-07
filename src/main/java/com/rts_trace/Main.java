package com.rts_trace;

import java.io.File;

import com.rts_trace.debug.getDiffDependency;
import com.rts_trace.dependency.CreateDependency;
import com.rts_trace.dependency.UpdateLineInfo;
import com.rts_trace.selection.TestSelect;

public class Main {
    public static void main(String[] args) throws Exception {
        File f = new File("data/json/");
        File se = new File("data/selogger");
        /* １回目 */
        if (f.list().length == 1) {
            System.out.println("create dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
        } else if(se.list().length ==0) {
            /*
             * ２回目以降
             * テスト選択と行数情報の更新
             */
            System.out.println("select test and update line info dependency");
            TestSelect t = new TestSelect();
            t.startSelect();
            UpdateLineInfo uli = new UpdateLineInfo();
            uli.startUpdateLineInfo();
        }else{
            System.out.println("update dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
        }

        //debug用
        // getDiffDependency g = new getDiffDependency();
        // g.start();
    }

}