package com.jdhaworthwheatman.unition;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Adapter_Custom_Search extends BaseAdapter {

    Context context;
    List<Row_Item_Search> search_row_items ;

    Adapter_Custom_Search(Context context, List<Row_Item_Search> search_row_items ){
        this.context = context;
        this.search_row_items = search_row_items;
    }

    @Override
    public int getCount(){ return search_row_items.size();}

    @Override
    public Object getItem(int position){ return search_row_items.get(position);}

    @Override
    public long getItemId(int position){return search_row_items.indexOf(getItem(position));}

    private class SearchViewHolder {
        TextView tv_search_user_name;
        TextView tv_search_user_cost;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        SearchViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item_search,null);
            holder = new SearchViewHolder();

            holder.tv_search_user_name = convertView.findViewById(R.id.tv_search_name);
            holder.tv_search_user_cost = convertView.findViewById(R.id.tv_search_cost);

            Row_Item_Search search_row_pos = search_row_items.get(position);

            holder.tv_search_user_name.setText(search_row_pos.getSearch_user_name());
            holder.tv_search_user_cost.setText(Integer.toString(search_row_pos.getSearch_user_cost()));

            convertView.setTag(holder);
        } else {
            holder = (SearchViewHolder) convertView.getTag();
        }

        return convertView;
    }

}