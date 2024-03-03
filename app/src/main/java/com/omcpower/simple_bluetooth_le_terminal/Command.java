package com.omcpower.simple_bluetooth_le_terminal;

public class Command {
    private String command;
    private String app_id;
    private String dateentry;
    private String flag;
    private int value;
    private String dName;
    private String dAddress;

    public Command(String command, String app_id, String dateentry,
                   String flag, int value, String dName, String dAddress) {
        this.command = command;
        this.app_id = app_id;
        this.dateentry = dateentry;
        this.flag = flag;
        this.value = value;
        this.dName = dName;
        this.dAddress = dAddress;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getDateentry() {
        return dateentry;
    }

    public void setDateentry(String dateentry) {
        this.dateentry = dateentry;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
    }

    public String getdAddress() {
        return dAddress;
    }

    public void setdAddress(String dAddress) {
        this.dAddress = dAddress;
    }
}
