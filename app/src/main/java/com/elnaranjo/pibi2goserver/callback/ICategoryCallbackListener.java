package com.elnaranjo.pibi2goserver.callback;

import com.elnaranjo.pibi2goserver.model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategorylLoadFailed(String message);
}
