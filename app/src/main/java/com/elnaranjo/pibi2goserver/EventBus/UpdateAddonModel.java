package com.elnaranjo.pibi2goserver.EventBus;

import com.elnaranjo.pibi2goserver.model.AddonModel;

import java.util.List;

public class UpdateAddonModel {
    private List<AddonModel> addonModels;

    public UpdateAddonModel() {
    }

    public List<AddonModel> getAddonModels() {
        return addonModels;
    }

    public void setAddonModels(List<AddonModel> addonModels) {
        this.addonModels = addonModels;
    }
}
