package com.lee.jeff.shopper.zone.care;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShoppingItemActivity extends Activity {

    private Button mSave, mCancel, mDelete;
    private EditText mCategory, mName;
    private ShoppingListItem shoppingItem;
    private ShoppingListDataSource mDatasource;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.item);

        // initialize variables
        mSave = (Button) findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingItem == null) {
                    SaveShoppingItem saveShoppingItem = new SaveShoppingItem(ShoppingItemActivity.this);
                    saveShoppingItem.execute(true); // do a post
                } else {
                    SaveShoppingItem saveShoppingItem = new SaveShoppingItem(ShoppingItemActivity.this);
                    saveShoppingItem.execute(false); // do  a put
                }
            }
        });
        mCancel = (Button) findViewById(R.id.cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDelete = (Button) findViewById(R.id.delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: delete from server and from db (on success)
            }
        });

        mCategory = (EditText) findViewById(R.id.category);
        TextWatcher t = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                canSave(); // disable / enable the save button
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        mCategory.addTextChangedListener(t);
        mName = (EditText) findViewById(R.id.name);
        mName.addTextChangedListener(t);
        mDatasource = new ShoppingListDataSource(this);
        mDatasource.open();
        // get the id to see if this is an ad or edit
        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            shoppingItem = mDatasource.getShoppingListItemWithId(id);
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("Edit item");
            mCategory.setText(shoppingItem.getCategory());
            mName.setText(shoppingItem.getName());
        } else {
            shoppingItem = null;
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("Add item");
            LinearLayout ll = (LinearLayout) findViewById(R.id.button_layout);
            ll.setWeightSum(2);
            mDelete.setVisibility(View.GONE);

        }
        canSave();

    }

    public void onDestroy() {
        super.onDestroy();
        mDatasource.close();
    }

    // checks to see if either category or name is empty
    private void canSave() {
        if (mCategory.getText().toString().length() != 0 && mName.getText().toString().length() != 0) {
            mSave.setEnabled(true);
        } else {
            mSave.setEnabled(false);
        }
    }

    public class SaveShoppingItem extends AsyncTask<Boolean, Void, Integer> {

        private ProgressDialog pd;

        public SaveShoppingItem(Context context) {
            // show a dialog while saving
            pd = new ProgressDialog(context);
            pd.setIndeterminate(true);
            pd.setMessage("Saving...");
            pd.show();
        }

        // converts the date string to a date object
        private Date convertToDate(String dateString) {
            try {
                Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).parse(dateString.replaceAll("Z", "+0000"));
                return date;
            } catch (ParseException e) {
                Log.e(ShoppingListActivity.LOG_TAG, "Failed to convert date string: " + dateString);
            }
            return null;
        }

        // creates the json object to pass to the server
        public String createJsonString() {
            JSONObject object = new JSONObject();
            try {
                JSONObject item = new JSONObject();
                item.put("category", mCategory.getText().toString());
                item.put("name", mName.getText().toString());
                object.put("item", item);
            } catch (Exception e) {
                Log.e(ShoppingListActivity.LOG_TAG, "Error: " + e.getMessage());
            }
            return object.toString();
        }

        @Override
        protected Integer doInBackground(Boolean... params) {
            StatusLine statusLine = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;
                // do a post
                if (params[0]) {
                    HttpPost httpPost = new HttpPost((ShoppingListActivity.BASE_URL + ShoppingListActivity.URL_POST));
                    httpPost.addHeader("Accept", "application/json");
                    httpPost.addHeader("Content-type", "application/json");
                    httpPost.addHeader("X-CZ-Authorization", ShoppingListActivity.AUTH_KEY);
                    httpPost.setEntity(new StringEntity(createJsonString(), "UTF8"));
                    response = httpclient.execute(httpPost);
                // do a put
                } else {
                    HttpPut httpPut = new HttpPut((ShoppingListActivity.BASE_URL + ShoppingListActivity.URL_PUT.replace("$", Long.toString(shoppingItem.getId()))));
                    Log.e("!!__!!", ShoppingListActivity.BASE_URL + ShoppingListActivity.URL_PUT.replace("$", Long.toString(shoppingItem.getId())));
                    httpPut.addHeader("Accept", "application/json");
                    httpPut.addHeader("Content-type", "application/json");
                    httpPut.addHeader("X-CZ-Authorization", ShoppingListActivity.AUTH_KEY);
                    httpPut.setEntity(new StringEntity(createJsonString(), "UTF8"));
                    response = httpclient.execute(httpPut);
                }

                statusLine = response.getStatusLine();
                // get the return to make sure it was succesful so we can update the db as well
                if (statusLine.getStatusCode() == 201 || statusLine.getStatusCode() == 200) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    JSONObject jsonObject = new JSONObject(out.toString());
                    final long id = jsonObject.getLong("id");
                    // check and see if the item already exists
                    if (mDatasource.getShoppingListItemWithId(id) != null) {
                        final ShoppingListItem oldItem = mDatasource.getShoppingListItemWithId(id);
                        // check and see if the item needs to be updated
                        if (convertToDate(oldItem.getUpdatedAt()).compareTo(convertToDate(jsonObject.getString("updated_at"))) < 0) {
                            mDatasource.updateShoppingListItem(jsonObject.getLong("id"), jsonObject.getString("category"),
                                    jsonObject.getString("name"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"));
                        }
                    } else {
                        // add the item to the db
                        final ShoppingListItem item = mDatasource.createShoppingListItem(jsonObject.getLong("id"), jsonObject.getString("category"),
                                jsonObject.getString("name"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"));
                    }

                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                }
            } catch (Exception e) {
                Log.e(ShoppingListActivity.LOG_TAG, "Exception: " + e.getMessage());
            }

            if (statusLine != null) {
                return statusLine.getStatusCode();
            } else {
                return -1;
            }
        }

        public void onPostExecute(Integer result) {
            pd.cancel();

            // displays an error message if an error occurred
            String errorMsg = null;
            switch (result) {
                case 0:
                case 200:
                case 201:
                    break;
                case 422:
                    errorMsg = "Errors during create or update. Error code: " + result;
                    break;
                case 500:
                    errorMsg = "Server error. Error code: " + result;
                    break;
                default:
                    errorMsg = "Unknown error. Error code: " + result;
            }
            if (errorMsg != null) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ShoppingItemActivity.this);
                adb.setTitle("Error");
                adb.setMessage(errorMsg);
                adb.setNeutralButton("Okay", null);
                adb.show();
            } else {
                finish();
            }
        }
    }
}
