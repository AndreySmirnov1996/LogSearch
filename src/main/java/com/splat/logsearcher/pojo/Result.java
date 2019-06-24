package com.splat.logsearcher.pojo;

public class Result {
    private String path;
    private Long startByte;
    private String line;

    public Result(String path, Long startByte, String line) {
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

    public Long getStartByte() {
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
