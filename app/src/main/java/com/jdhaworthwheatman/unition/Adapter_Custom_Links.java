package com.jdhaworthwheatman.unition;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class Adapter_Custom_Links extends BaseAdapter {

    Context context;
    List<Row_Item_Links> links_row_items ;

    Adapter_Custom_Links(Context context, List<Row_Item_Links> links_row_items ){
        this.context = context;
        this.links_row_items = links_row_items;
    }

    @Override
    public int getCount(){ return links_row_items.size();}

    @Override
    public Object getItem(int position){ return links_row_items.get(position);}

    @Override
    public long getItemId(int position){return links_row_items.indexOf(getItem(position));}

    private class LinksViewHolder {
        TextView tv_links_user_name;
        TextView tv_links_user_cost;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LinksViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item_links,null);
            holder = new LinksViewHolder();

            holder.tv_links_user_name = (TextView) convertView.findViewById(R.id.tv_links_user_name);
            holder.tv_links_user_cost = (TextView) convertView.findViewById(R.id.tv_links_user_cost);

            Row_Item_Links links_row_pos = links_row_items.get(position);

            holder.tv_links_user_name.setText(links_row_pos.getLinks_user_name());
            holder.tv_links_user_cost.setText(Integer.toString(links_row_pos.getLinks_user_cost()));

            convertView.setTag(holder);
        } else {
            holder = (LinksViewHolder) convertView.getTag();
        }

        return convertView;
    }

}