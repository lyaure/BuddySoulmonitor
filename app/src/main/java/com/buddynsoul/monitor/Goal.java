package com.buddynsoul.monitor;

import android.text.TextWatcher;
import android.widget.TextView;

public class Goal {
    private String text;
    private boolean checked;
    private int id;
    static int counter = 0;

    public Goal(String text, boolean checked){
        counter++;
        this.text = text;
        this.checked = checked;
        this.id = counter;
    }

    public void updateText(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getId() {
        return id;
    }
}
