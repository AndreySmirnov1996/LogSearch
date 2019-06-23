package com.splat.logsearcher;

public class ResultOfSearching {
    private String path;
    private long startByte;
    private String line;

    public ResultOfSearching(String path, long startByte, String line) {
        this.path = path;
        this.startByte = startByte;
        this.line = line;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getStartByte() {
        return startByte;
    }

    public void setStartByte(long startByte) {
        this.startByte = startByte;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return path + "\tstartByte=" + startByte +
                "\tline=" + line;
    }
}
