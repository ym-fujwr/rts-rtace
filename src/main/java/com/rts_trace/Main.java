package com.rts_trace;

import java.io.File;
import java.io.FileWriter;

import com.rts_trace.debug.getDiffDependency;
import com.rts_trace.dependency.CreateDependency;
import com.rts_trace.dependency.UpdateLineInfo;
import com.rts_trace.selection.TestSelect;

public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        File f = new File("data/json/");
        File se = new File("data/selogger");
        int flag = 0;
        /* １回目 */
        if (f.list().length == 1) {
            System.out.println("create dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
            flag = 1;
        } else if(se.list().length ==0) {
            /*
             * ２回目以降
             * テスト選択と行数情報の更新
             */
            flag = 2;
            System.out.println("select test and update line info dependency");
            TestSelect t = new TestSelect();
            t.startSelect();
            UpdateLineInfo uli = new UpdateLineInfo();
            uli.startUpdateLineInfo();
        }else{
            flag = 1;
            System.out.println("update dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
        }
        long endTime = System.currentTimeMillis();
        try {
            File t = new File("data/time.txt");
            FileWriter tw = new FileWriter(t,true);
            if(flag == 1){
                tw.write("update,");
            }else{
                tw.write("select,");
            }
            tw.write((int) (endTime-startTime)+",");
            tw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //debug用
        // getDiffDependency g = new getDiffDependency();
        // g.start();
    }

}