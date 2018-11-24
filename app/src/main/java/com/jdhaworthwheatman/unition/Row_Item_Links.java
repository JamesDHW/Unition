package com.jdhaworthwheatman.unition;

public class Row_Item_Links {

    private String links_user_name;
    private String links_user_id;
    private int links_user_cost;


    public Row_Item_Links(String links_user_name, String links_user_id, int links_user_cost){
        this.links_user_name = links_user_name;
        this.links_user_id = links_user_id;
        this.links_user_cost = links_user_cost;
    }

    public String getLinks_user_name(){return links_user_name;}

    public void setLinks_user_name(String links_user_name){this.links_user_name = links_user_name;}

    public String getLinks_user_id(){return links_user_id;}

    public void setLinks_user_id(String links_user_id){this.links_user_id = links_user_id;}

    public int getLinks_user_cost(){return links_user_cost;}

    public void setLinks_user_cost(int links_user_cost){this.links_user_cost = links_user_cost;}


}

