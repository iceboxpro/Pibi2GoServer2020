package com.elnaranjo.pibi2goserver.ui.order;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elnaranjo.pibi2goserver.callback.IOrderCallbackList;
import com.elnaranjo.pibi2goserver.common.Common;
import com.elnaranjo.pibi2goserver.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OrderViewModel extends ViewModel implements IOrderCallbackList {

    private MutableLiveData<List<OrderModel>> orderMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackList listener;

    public OrderViewModel() {
      orderMutableLiveData = new MutableLiveData<>();
      messageError = new MutableLiveData<>();
      listener = this;
    }

    public MutableLiveData<List<OrderModel>> getOrderMutableLiveData() {
        loadOrdeByStatus(0);
        return orderMutableLiveData;
    }

    public void loadOrdeByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    OrderModel orderModel = itemSnapShot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapShot.getKey());
                    tempList.add(orderModel);
                }
                listener.onOrderLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onOrderLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
        if (orderModelList.size()>0)
        {
            Collections.sort(orderModelList, (orderModel, t1)->{
                if(orderModel.getCreateDate() < t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate() == t1.getCreateDate() ?0:1;


            });
        }
        orderMutableLiveData.setValue(orderModelList);

    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}