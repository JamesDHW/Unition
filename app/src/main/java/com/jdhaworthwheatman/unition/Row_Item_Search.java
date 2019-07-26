package com.jdhaworthwheatman.unition;

public class Row_Item_Search {

    private String search_user_name;
    private String search_user_id;
    private int search_user_cost;


    public Row_Item_Search (String search_user_name,String search_user_id, int search_user_cost){
        this.search_user_name = search_user_name;
        this.search_user_id = search_user_id;
        this.search_user_cost = search_user_cost;
    }

    public String getSearch_user_name(){return search_user_name;}

    public void setSearch_user_name(String search_user_name){this.search_user_name = search_user_name;}

    public String getSearch_user_id(){return search_user_id;}

    public void setSearch_user_id(String search_user_id){this.search_user_id = search_user_id;}

    public int getSearch_user_cost(){return search_user_cost/100;}

    public void setSearch_user_cost(int search_user_cost){this.search_user_cost = search_user_cost;}


}

