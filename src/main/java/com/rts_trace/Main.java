package com.rts_trace;

import com.rts_trace.dependency.CreateDependency;

public class Main {
    public static void main(String[] args) throws Exception {
        /*１回目 */
        CreateDependency c = new CreateDependency();
        c.startCreate();
        /*
         * ２回目以降 
         * テスト選択と依存関係の更新を実施
         */
    }
    
}