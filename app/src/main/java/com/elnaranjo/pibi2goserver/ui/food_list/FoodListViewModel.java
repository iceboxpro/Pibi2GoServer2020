package com.elnaranjo.pibi2goserver.ui.food_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elnaranjo.pibi2goserver.common.Common;
import com.elnaranjo.pibi2goserver.model.FoodModel;

import java.util.List;

public class FoodListViewModel extends ViewModel {
    private MutableLiveData<List<FoodModel>> listMutableLiveDataFoodList;

    public FoodListViewModel() {
    }

    public MutableLiveData<List<FoodModel>> getListMutableLiveDataFoodList() {
        if (listMutableLiveDataFoodList == null)
            listMutableLiveDataFoodList = new MutableLiveData<>();
        listMutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());
        return listMutableLiveDataFoodList;
    }
}
