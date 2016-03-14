package com.veyndan.generic.attach;

public class Photo {
    private String path;
    private int count;

    public Photo(String path) {
        this.path = path;
        this.count = -1;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isSelected() {
        return count != -1;
    }

}
