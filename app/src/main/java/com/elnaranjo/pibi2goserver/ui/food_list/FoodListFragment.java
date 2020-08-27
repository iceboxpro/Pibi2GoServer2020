package com.elnaranjo.pibi2goserver.ui.food_list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elnaranjo.pibi2goserver.EventBus.AddonSizeEvent;
import com.elnaranjo.pibi2goserver.EventBus.ChangeMenuClick;
import com.elnaranjo.pibi2goserver.EventBus.ToastEvent;
import com.elnaranjo.pibi2goserver.R;
import com.elnaranjo.pibi2goserver.SizeAddonEditActivity;
import com.elnaranjo.pibi2goserver.adapter.MyFoodListAdapter;
import com.elnaranjo.pibi2goserver.common.Common;
import com.elnaranjo.pibi2goserver.common.MySwiperHelper;
import com.elnaranjo.pibi2goserver.model.FoodModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private FoodListViewModel foodListViewModel;
    private List<FoodModel> foodModelList;
    Unbinder unbinder;

    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri=null;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //Event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchFood(s);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {return false;}
        });

        //Clear text when click to Clear Button on Search View

        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(view -> {
           EditText ed = (EditText)searchView.findViewById(R.id.search_src_text);
           //Clear Text
            ed.setText("");
            //clear query
            searchView.setQuery("",false);
            //Collapse the action view
            searchView.onActionViewCollapsed();
            //Collapse the search widget
            menuItem.collapseActionView();
            //Restore result to original
            foodListViewModel.getListMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());
        });

    }

    private void startSearchFood(String s) {
        List<FoodModel> resultFood = new ArrayList<>();
        for(int i=0;i<Common.categorySelected.getFoods().size();i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(s.toLowerCase())) {
                foodModel.setPositionInList(i);
                resultFood.add(foodModel);
            }
        }
        foodListViewModel.getListMutableLiveDataFoodList().setValue(resultFood);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        foodListViewModel.getListMutableLiveDataFoodList().observe(getViewLifecycleOwner(), foodModels -> {
            if(foodModels!= null) {
                foodModelList = foodModels;
                adapter = new MyFoodListAdapter(getContext(), foodModelList);
                recycler_food_list.setAdapter(adapter);
                recycler_food_list.setLayoutAnimation(layoutAnimationController);
            }
        });

        return root;
    }

    private void initViews() {
        setHasOptionsMenu(true); //Enable Menu in fragment

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());
        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(),recycler_food_list,width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Borrar",30,0, Color.parseColor("#9b0000"),
                        pos -> {

                        if (foodModelList != null)
                            Common.selectedFood = foodModelList.get(pos);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Borrar")
                                    .setMessage("¿Quieres eliminar el producto?")
                                    .setNegativeButton("Cancelar",  (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setPositiveButton("Borrar", ((dialogInterface, i) -> {
                                        FoodModel foodModel = adapter.getItemAtPosistion(pos);
                                        if (foodModel.getPositionInList() == -1) // Si == -1 es por default, no hagas nada
                                            Common.categorySelected.getFoods().remove(pos);
                                        else
                                            Common.categorySelected.getFoods().remove(foodModel.getPositionInList());
                                        updateFood(Common.categorySelected.getFoods(),true);
                                    }));
                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();


                        }));
                buf.add(new MyButton(getContext(),"Actualizar",30,0, Color.parseColor("#560027"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosistion(pos);
                            if (foodModel.getPositionInList() == -1)
                                showUpdateDialog(pos,foodModel);
                            else
                                showUpdateDialog(foodModel.getPositionInList(),foodModel);
                        }));
                buf.add(new MyButton(getContext(),"Tamaño",30,0, Color.parseColor("#12005e"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosistion(pos);
                            if (foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            //change POS
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEvent(false,pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEvent(false,foodModel.getPositionInList()));

                        }));
                buf.add(new MyButton(getContext(),"Complemento",30,0, Color.parseColor("#336699"),
                        pos -> {
                            FoodModel foodModel = adapter.getItemAtPosistion(pos);
                            if (foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pos);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            if (foodModel.getPositionInList() == -1)
                                EventBus.getDefault().postSticky(new AddonSizeEvent(true,pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEvent(true,foodModel.getPositionInList()));
                        }));
            }
        };

    }

    private void showUpdateDialog(int pos, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Actualizar");
        builder.setMessage("Llene toda la información");

        View itemview = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemview.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemview.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemview.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemview.findViewById(R.id.img_food_image);

        //Set data
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));
        Glide.with(getContext()).load(foodModel.getImage())
                .into(img_food);
        img_food.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"),PICK_IMAGE_REQUEST);
        });
        builder.setNegativeButton("Cancelar", ((dialogInterface, i) -> dialogInterface.dismiss()))
                .setPositiveButton("Actualizar", ((dialogInterface, i) -> {
                    FoodModel updateFood = foodModel;
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_food_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                            Long.parseLong(edt_food_price.getText().toString()));
                    if (imageUri != null){
                        {
                            // In this, we will use Firebase Storage to Upload Image
                            dialog.setMessage("Cargando...");
                            dialog.show();

                            String unique_name = UUID.randomUUID().toString();
                            StorageReference imageFolder = storageReference.child("images/"+unique_name);

                            imageFolder.putFile(imageUri)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }).addOnCompleteListener(task -> {
                                dialog.dismiss();
                                imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    updateFood.setImage(uri.toString());
                                    updateFood(Common.categorySelected.getFoods(),false);
                                });
                            }).addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                dialog.setMessage(new StringBuilder("Cargando...")
                                        .append(progress)
                                        .append("%"));

                            });

                        }

                    }
                    else{
                        Common.categorySelected.getFoods().set(pos,updateFood);
                        updateFood(Common.categorySelected.getFoods(),false);
                    }

                }));

        builder.setView(itemview);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            imageUri = data.getData();
            img_food.setImageURI(imageUri);
        }
    }

    private void updateFood(List<FoodModel> foods, boolean isDelete) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener((e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        foodListViewModel.getListMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(!isDelete,true));

                    }

                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}