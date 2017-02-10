package com.example.dessusdi.myfirstapp.recycler_view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dessusdi.myfirstapp.R;
import com.example.dessusdi.myfirstapp.models.WaqiObject;

import java.util.List;

/**
 * Created by dessusdi on 30/01/2017.
 * DESSUS Dimitri
 */
public class AqcinListAdapter extends RecyclerView.Adapter<AqcinCellView> {

    List<WaqiObject> list;

    public AqcinListAdapter(List<WaqiObject> list) {
        this.list = list;
    }

    @Override
    public AqcinCellView onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false);
        return new AqcinCellView(view);
    }

    @Override
    public void onBindViewHolder(AqcinCellView aqcinCellView, int index) {
        WaqiObject myObject = list.get(index);
        aqcinCellView.bind(myObject);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
