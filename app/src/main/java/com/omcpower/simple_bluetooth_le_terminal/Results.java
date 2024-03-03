package com.omcpower.simple_bluetooth_le_terminal;


public class Results {

    private boolean isValid;
    private String device_id;
    private String message;
    private int type;

    public Results(boolean isValid, String device_id, String message, int type) {
        this.isValid = isValid;
        this.device_id = device_id;
        this.message = message;
        this.type = type;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
