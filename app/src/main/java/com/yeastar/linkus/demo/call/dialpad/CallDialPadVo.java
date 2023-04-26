package com.yeastar.linkus.demo.call.dialpad;

public class CallDialPadVo {
    private boolean pressed = false;
    private boolean enabled = true;
    private int action;

    public CallDialPadVo(int action) {
        this.action = action;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
