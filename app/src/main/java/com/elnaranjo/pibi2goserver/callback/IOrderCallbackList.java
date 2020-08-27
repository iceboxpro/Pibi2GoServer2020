package com.elnaranjo.pibi2goserver.callback;

import com.elnaranjo.pibi2goserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackList {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);

}
