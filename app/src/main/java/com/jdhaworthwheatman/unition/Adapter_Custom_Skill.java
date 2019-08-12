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

public class Adapter_Custom_Skill extends BaseAdapter {

    Context context;
    List<Row_Item_Skills> skill_row_items;

    Adapter_Custom_Skill(Context context, List<Row_Item_Skills> skill_row_items ){
        this.context = context;
        this.skill_row_items = skill_row_items;
    }

    @Override
    public int getCount(){ return skill_row_items.size();}

    @Override
    public Object getItem(int position){ return skill_row_items.get(position);}

    @Override
    public long getItemId(int position){return skill_row_items.indexOf(getItem(position));}

    private class SkillViewHolder {
        TextView tv_skill_name;
        ImageView iv_skill_icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        SkillViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item_skills,null);
            holder = new SkillViewHolder();

            holder.tv_skill_name = convertView.findViewById(R.id.tv_skill_name);
            holder.iv_skill_icon = convertView.findViewById(R.id.iv_skill_icon);

            Row_Item_Skills skill_row_pos = skill_row_items.get(position);

            holder.tv_skill_name.setText(skill_row_pos.getSkill_name());
            holder.iv_skill_icon.setImageResource(skill_row_pos.getSkill_icon());

            convertView.setTag(holder);
        } else {
            holder = (SkillViewHolder) convertView.getTag();
        }

        return convertView;
    }

}