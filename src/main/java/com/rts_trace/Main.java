package com.rts_trace;

import java.io.File;

import com.rts_trace.dependency.CreateDependency;
import com.rts_trace.selection.TestSelect;

public class Main {
    public static void main(String[] args) throws Exception {
        File f = new File("data/json");
        /*１回目 */
        if(f.list().length == 0){
            CreateDependency c = new CreateDependency();
            c.startCreate();
        }else{
            /*
            * ２回目以降 
            * テスト選択と依存関係の更新を実施
            */
            TestSelect t = new TestSelect();
            t.startSelect();
        }


    }
    
}