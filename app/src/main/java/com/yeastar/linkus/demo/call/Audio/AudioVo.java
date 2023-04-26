package com.yeastar.linkus.demo.call.Audio;

public class AudioVo {
    private String name;
    private int iconResId;
    private boolean selected;
    private boolean isLast;
    private int audioChannel;

    public AudioVo(String name, int iconResId, boolean selected, boolean isLast, int audioChannel) {
        this.name = name;
        this.iconResId = iconResId;
        this.selected = selected;
        this.isLast = isLast;
        this.audioChannel = audioChannel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public int getAudioChannel() {
        return audioChannel;
    }

    public void setAudioChannel(int audioChannel) {
        this.audioChannel = audioChannel;
    }
}
