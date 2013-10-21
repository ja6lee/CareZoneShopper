package com.lee.jeff.shopper.zone.care;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ShoppingListAdapter extends ArrayAdapter<ShoppingListItem> {

    private Context context;
    private Map<String, ArrayList<ShoppingListItem>> categories;
    private int _size;

    public ShoppingListAdapter(Context context, ArrayList<ShoppingListItem> shoppingItems) {
        super(context, R.layout.shopping_item, shoppingItems);
        this.context = context;
        categories = new HashMap<String, ArrayList<ShoppingListItem>>();
        _size = 0;
        // puts the shopping items into a hash map so we can display the data by category (with headers)
        for (int i = 0; i < shoppingItems.size(); i++) {
            _size++;
            if (categories.containsKey(shoppingItems.get(i).getCategory())) {
                categories.get(shoppingItems.get(i).getCategory()).add(shoppingItems.get(i));
            } else {
                _size++;
                ArrayList<ShoppingListItem> items = new ArrayList<ShoppingListItem>();
                items.add(shoppingItems.get(i));
                categories.put(shoppingItems.get(i).getCategory(), items);
            }
        }
    }

    @Override
    public int getCount() {
        return _size;
    }

    public void add(ShoppingListItem item) {
        // + 1 for the element
        _size++;
        // see if the category already exists
        if (categories.containsKey(item.getCategory())) {
            ArrayList<ShoppingListItem> items = categories.get(item.getCategory());
            categories.get(item.getCategory()).add(item);
        } else {
            // + 1 more for the new category header
            _size++;
            ArrayList<ShoppingListItem> items = new ArrayList<ShoppingListItem>();
            items.add(item);
            categories.put(item.getCategory(), items);
        }
        notifyDataSetChanged();
    }

    @Override
    public ShoppingListItem getItem(int position) {
        int count = 0;
        for (Map.Entry<String, ArrayList<ShoppingListItem>> entry: categories.entrySet()) {
            if (position == count) {
                return null; // we don't want to return headers as an item
            }
            count += entry.getValue().size();
            if (position <= count) {
                // means we have found the right category - pick the name
                return entry.getValue().get(count - position);
            }
            // add one for the next category
            count++;
        }
        return null;
    }

    public ShoppingListItem getItemWithId(long id) {
        for (Map.Entry<String, ArrayList<ShoppingListItem>> entry: categories.entrySet()) {
            for (ShoppingListItem item : entry.getValue()) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    public void remove(ShoppingListItem item) {
        // remove the item from the category
        categories.get(item.getCategory()).remove(item);
        // - 1 for removing the item
        _size--;
        // see if we need to remove the category as well
        if (categories.get(item.getCategory()).size() == 0) {
            categories.remove(item.getCategory());
            _size--;
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int count = 0;
        for (Map.Entry<String, ArrayList<ShoppingListItem>> entry: categories.entrySet()) {
            if (position == count) {
                // means we have a category
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.header, parent, false);
                TextView category = (TextView) rowView.findViewById(R.id.header);
                category.setText(entry.getKey());
                return rowView;
            }
            count += entry.getValue().size();
            if (position <= count) {
                // means we have found the right category - pick the name
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.shopping_item, parent, false);
                TextView title = (TextView) rowView.findViewById(R.id.name);
                // going to be in reverse order
                title.setText(entry.getValue().get(count - position).getName());
                Log.e("!!__!!", "Row view: " + entry.getValue().get(count - position).getName());
                return rowView;
            }
            // add one for the next category
            count++;
        }
        // something went wrong ... (this should never happen)
        return null;
    }
}
