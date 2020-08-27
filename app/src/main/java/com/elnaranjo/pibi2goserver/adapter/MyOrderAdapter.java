package com.elnaranjo.pibi2goserver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elnaranjo.pibi2goserver.R;
import com.elnaranjo.pibi2goserver.common.Common;
import com.elnaranjo.pibi2goserver.model.OrderModel;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {
    Context context;
    List<OrderModel> orderOrdeModelList;
    SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<OrderModel> orderOrdeModelList) {
        this.context = context;
        this.orderOrdeModelList = orderOrdeModelList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_order_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(orderOrdeModelList.get(position).getCartItemList().get(0).getFoodImage())
                .into(holder.img_food_image);
        holder.txt_order_number.setText(orderOrdeModelList.get(position).getKey());
        Common.setSpanStringColor("Fecha de la orden: ", simpleDateFormat.format(orderOrdeModelList.get(position).getCreateDate()),
                holder.txt_time, Color.parseColor("#333639"));
        Common.setSpanStringColor("Estado de la orden: ", Common.convertStatusToString(orderOrdeModelList.get(position).getOrderStatus()),
                holder.txt_order_status, Color.parseColor("#00579A"));
        Common.setSpanStringColor("A Nombre de: ", orderOrdeModelList.get(position).getUserName(),
                holder.txt_name, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Numero de items: ", orderOrdeModelList.get(position).getCartItemList() == null ? "0":
                String.valueOf(orderOrdeModelList.get(position).getCartItemList().size()),
                holder.txt_num_item, Color.parseColor("#4B647D"));


    }

    @Override
    public int getItemCount() {
        return orderOrdeModelList.size();
    }

    public OrderModel getItemPositionAt(int pos) {
        return orderOrdeModelList.get(pos);
    }

    public void removeItem(int pos) {
        orderOrdeModelList.remove(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_time)
        TextView txt_time;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_num_item)
        TextView txt_num_item;

        private Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
