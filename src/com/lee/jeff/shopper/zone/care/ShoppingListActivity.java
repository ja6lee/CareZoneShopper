package com.lee.jeff.shopper.zone.care;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import eu.erikw.PullToRefreshListView;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShoppingListActivity extends Activity {

    private PullToRefreshListView mListView;
    private Button mAdd;
    private ShoppingListDataSource mDatasource;
    private ShoppingListAdapter mAdapter;
    private ArrayList<ShoppingListItem> mShoppingItems;
    public static final String BASE_URL = "http://czshopper.herokuapp.com/";
    public static final String URL_GET = "/items.json";
    public static final String URL_POST = "/items.json";
    public static final String URL_PUT = "/items/$.json";
    public static final String URL_DELETE = "";
    public static final String AUTH_KEY = "JTp5cAXrLxf8Hx843UGg";
    public static final String LOG_TAG = "!!__!!";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // initialize UI elements
        mListView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
        mAdd = (Button) findViewById(R.id.add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the add/edit activity
                Intent intent = new Intent(ShoppingListActivity.this, ShoppingItemActivity.class);
                startActivity(intent);
            }
        });


        // load any locally saved data from the db
        // TODO: move this to an asynchtask for when we get lots of data!
        mDatasource = new ShoppingListDataSource(this);
        mDatasource.open();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingListItem item = mAdapter.getItem(position);
                if (item != null) {
                    // start the add/edit activity add info to the intent
                    Intent intent = new Intent(ShoppingListActivity.this, ShoppingItemActivity.class);
                    intent.putExtra("id", item.getId());
                    startActivity(intent);
                }
            }
        });

        mListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // get data from server
                update();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        // close the data source
        if (mDatasource != null) {
            mDatasource.close();
        }
    }

    public void onResume() {
        super.onResume();
        // get items from the data base
        mShoppingItems = mDatasource.getAllShoppingListItems();
        // setup the adapter
        mAdapter = new ShoppingListAdapter(this, mShoppingItems);
        mListView.setAdapter(mAdapter);
        mListView.setRefreshing();
        update();
    }

    public void update() {
        GetShoppingList getShoppingList = new GetShoppingList();
        getShoppingList.execute();
    }

    public class GetShoppingList extends AsyncTask<Void, Void, Integer> {

        // converts the date string to a date object
        private Date convertToDate(String dateString) {
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).parse(dateString.replaceAll("Z", "+0000"));
                return date;
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Failed to convert date string: " + dateString);
            }
            return null;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            StatusLine statusLine = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet((BASE_URL + URL_GET));
                httpGet.addHeader("Accept", "application/json");
                httpGet.addHeader("X-CZ-Authorization", AUTH_KEY);
                HttpResponse response = httpclient.execute(httpGet);
                statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    JSONArray allItems = new JSONArray(out.toString());
                    for (int i = 0; i < allItems.length(); i++) {
                        JSONObject jsonObject = allItems.getJSONObject(i);
                        final long id = jsonObject.getLong("id");
                        // check and see if the item already exists
                        if (mDatasource.getShoppingListItemWithId(id) != null) {
                            final ShoppingListItem oldItem = mAdapter.getItemWithId(id);
                            // check and see if the item needs to be updated
                            if (convertToDate(oldItem.getUpdatedAt()).compareTo(convertToDate(jsonObject.getString("updated_at"))) < 0) {
                                mDatasource.updateShoppingListItem(jsonObject.getLong("id"), jsonObject.getString("category"),
                                                jsonObject.getString("name"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"));
                                final ShoppingListItem item = mDatasource.getShoppingListItemWithId(jsonObject.getLong("id"));
                                Log.e(LOG_TAG, "item: " + item.getName());
                                // changing the adapter has to be done on the main thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    mAdapter.remove(oldItem);
                                    mAdapter.add(item);
                                    }
                                });
                            }
                        } else {
                            // add the item to the db and the adapter
                            final ShoppingListItem item = mDatasource.createShoppingListItem(jsonObject.getLong("id"), jsonObject.getString("category"),
                                    jsonObject.getString("name"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"));
                            // change to the adapter has to be done on the main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.add(item);
                                }
                            });
                        }
                    }

                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: " + e.getMessage());
            }

            if (statusLine != null) {
                return statusLine.getStatusCode();
            } else {
                return -1;
            }
        }

        public void onPostExecute(Integer result) {
            // stops the refresh animation
            if (mListView.isRefreshing()) {
                mListView.onRefreshComplete();
            }
            // displays an error message if an error occurred
            String errorMsg = null;
            switch (result) {
                case 0:
                case 200:
                    mListView.invalidate();
                    break;
                case 500:
                    errorMsg = "Server error. Error code: " + result;
                    break;
                default:
                    errorMsg = "Unknown error. Error code: " + result;
            }
            if (errorMsg != null) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ShoppingListActivity.this);
                adb.setTitle("Error");
                adb.setMessage(errorMsg);
                adb.setNeutralButton("Okay", null);
                adb.show();
            } else {
                // let the adapter know that data has changed
                mAdapter.notifyDataSetChanged();
            }
        }
    }

}
