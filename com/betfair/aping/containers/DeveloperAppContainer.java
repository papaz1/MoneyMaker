package com.betfair.aping.containers;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.ArrayList;
import java.util.List;
import se.betfair.model.DeveloperApp;

public class DeveloperAppContainer extends Container {

    private List<DeveloperApp> result;

    public DeveloperAppContainer() {
        result = new ArrayList<>();
    }

    public List<DeveloperApp> getResult() {
        return result;
    }

    public void setResult(List<DeveloperApp> result) {
        this.result = result;
    }

    public void addResult(List<DeveloperApp> result) {
        this.result.addAll(result);
    }
}
