package com.rts_trace;

import java.io.File;

import com.rts_trace.dependency.CreateDependency;
import com.rts_trace.selection.TestSelect;

public class Main {
    public static void main(String[] args) throws Exception {
        File f = new File("data/json");
        /* １回目 */
        if (f.list().length == 1) {
            System.out.println("create dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
        } else {
            /*
             * ２回目以降
             * テスト選択と依存関係の更新を実施
             */
            System.out.println("select test and update dependency");
            TestSelect t = new TestSelect();
            t.startSelect();
        }
    }

}