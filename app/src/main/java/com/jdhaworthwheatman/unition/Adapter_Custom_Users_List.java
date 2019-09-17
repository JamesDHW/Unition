package com.jdhaworthwheatman.unition;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Adapter_Custom_Users_List extends BaseAdapter {

    Context context;
    List<Row_Item_User_List> users_row_items;

    Adapter_Custom_Users_List(Context context, List<Row_Item_User_List> users_row_items ){
        this.context = context;
        this.users_row_items = users_row_items;
    }

    @Override
    public int getCount(){ return users_row_items.size();}

    @Override
    public Object getItem(int position){ return users_row_items.get(position);}

    @Override
    public long getItemId(int position){return users_row_items.indexOf(getItem(position));}

    private class UsersViewHolder {
        TextView tv_users_user_name;
        TextView tv_users_user_cost;
        ImageView iv_users_pic;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        UsersViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item_users,null);
            holder = new UsersViewHolder();

            holder.tv_users_user_name = (TextView) convertView.findViewById(R.id.tv_users_name);
            holder.tv_users_user_cost = (TextView) convertView.findViewById(R.id.tv_users_cost);
            holder.iv_users_pic = convertView.findViewById(R.id.iv_profile_list);

            Row_Item_User_List users_row_pos = users_row_items.get(position);

            holder.tv_users_user_name.setText(users_row_pos.getUsers_name());
            holder.tv_users_user_cost.setText("Charges £"+Integer.toString(users_row_pos.getUsers_cost()));

            if(users_row_pos.getUsers_pic()!=null){
                holder.iv_users_pic.setImageBitmap(users_row_pos.getUsers_pic());
            }

            convertView.setTag(holder);
        } else {
            holder = (UsersViewHolder) convertView.getTag();
        }

        return convertView;
    }

}