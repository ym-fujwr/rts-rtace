package com.rts_trace.diffinfo;

    /*
     * @@ a,b c,d @@
     * aは元ファイル始まり行
     * bは元ファイルのdiff hunkの行数
     * cは新ファイル始まり行
     * dは新ファイルのdiff hunkの行数
     */
    public class DiffLineInfo {
        private String preStartLine;
        private String preHunkLine;
        private String curStartLine;
        private String curHunkLine;

        public DiffLineInfo(String preStartLine, String preHunkLine, String curStartLine, String curHunkLine) {
            this.preStartLine = preStartLine;
            this.preHunkLine = preHunkLine;
            this.curStartLine = curStartLine;
            this.curHunkLine = curHunkLine;
        }

        public String getPreStartLine() {
            return this.preStartLine;
        }

        public String getPreHunkLine() {
            return this.preHunkLine;
        }

        public String getCurStartLine() {
            return this.curStartLine;
        }

        public String getCurHunkLine() {
            return this.curHunkLine;
        }
    }