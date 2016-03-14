package com.veyndan.generic.attach;

public class Photo {
    private String path;
    private int count;

    public Photo(String path) {
        this.path = path;
        this.count = 0;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCountOffset(int offset) {
        count += offset;
    }

    public boolean isSelected() {
        return count == 0;
    }

}
