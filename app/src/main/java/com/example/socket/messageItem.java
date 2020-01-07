package com.example.socket;

public class messageItem {
    String msg;
    String ip;
    String name;
    String time;

    public messageItem(String msg, String ip, String name,String time) {
        this.msg = msg;
        this.ip = ip;
        this.name =name;
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
