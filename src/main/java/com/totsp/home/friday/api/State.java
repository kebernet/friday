package com.totsp.home.friday.api;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Created by rcooper on 6/29/15.
 */
public class State implements Serializable {
    private boolean on;
    private int brightness;

    public State(boolean on, int brightness) {
        this.on = on;
        if(brightness < 0){
            brightness = 0;
        } else if(brightness > 100){
            brightness = 100;
        }
        this.brightness = brightness;
    }

    public State(){
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equal(on, state.on) &&
                Objects.equal(brightness, state.brightness);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(on, brightness);
    }
}
