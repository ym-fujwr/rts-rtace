package com.rts_trace;

import com.rts_trace.dependency.CreateDependency;
import com.rts_trace.dependency.UpdateLineInfo;
import com.rts_trace.selection.TestSelect;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length !=1){
            System.out.println("引数がありません");
        }
        if(args[0].equals("create")){
            System.out.println("create dependency");
            CreateDependency c = new CreateDependency();
            c.startCreate();
        }else if(args[0].equals("select")){
            System.out.println("select test and update line info dependency");
            TestSelect t = new TestSelect();
            t.startSelect();
            UpdateLineInfo uli = new UpdateLineInfo();
            uli.startUpdateLineInfo();
        }
    }

}