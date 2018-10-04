/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.julyfifa.v3inventoryappstage1;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julyfifa.v3inventoryappstage1.data.ProductContract.ProductEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets Languages
 * Enters a word to translate
 * Shows the list of translated words
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int PRODUCT_LOADER = 0;

    /** Adapter for the ListView */
    ProductCursorAdapter mCursorAdapter;

    EditText inputEditText;

    String perevod;

    //Spinner part
    private Spinner spinner1, spinner2;
    private Button btnSubmit;

    /** URL for API */
    private static final String REQUEST_URL_ENDPOINT = "https://translate.yandex.net/api/v1.5/tr.json/translate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        //Spinner part
        addItemsOnSpinner2();

        inputEditText = (EditText) findViewById(R.id.input_field);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Translation language pair choice
                //For example, lang = "en-es"
                String lang = "";
                spinner1 = (Spinner) findViewById(R.id.spinner1);
                spinner2 = (Spinner) findViewById(R.id.spinner2);

                switch (String.valueOf(spinner1.getSelectedItem())) {

                    case "английский":
                        lang = lang + "en";
                        break;

                    case "испанский":
                        lang = lang + "es";
                        break;

                    case "русский":
                        lang = lang + "ru";
                        break;

                }

                lang = lang + "-";

                switch (String.valueOf(spinner2.getSelectedItem())) {

                    case "английский":
                        lang = lang + "en";
                        break;

                    case "испанский":
                        lang = lang + "es";
                        break;

                    case "русский":
                        lang = lang + "ru";
                        break;

                }

                //Log.v("language", lang);

                Uri baseUri = Uri.parse(REQUEST_URL_ENDPOINT);
                Uri.Builder uriBuilder = baseUri.buildUpon();
                String text = inputEditText.getText().toString();
                uriBuilder.appendQueryParameter("text", text);
                uriBuilder.appendQueryParameter("lang", lang);
                uriBuilder.appendQueryParameter("key", "trnsl.1.1.20180917T094414Z.2fbae4c30bc29f61.a3880c665e1f30f0f574163338d32286859cc0b8");

                //Log.v("uri", uriBuilder.toString());

                // Perform the HTTP request for translation data and process the response.
                FetchDataTask task = new FetchDataTask();
                task.execute(uriBuilder.toString(), text);

            }
        });

        // Find the ListView which will be populated with the words data
        ListView wordListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        wordListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no word data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        wordListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link InfoActivity}
                Intent intent = new Intent(CatalogActivity.this, InfoActivity.class);
                // Form the content URI that represents the specific word that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.julyfifa.v3inventoryappstage1/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the {@link InfoActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner2() {

        spinner2 = (Spinner) findViewById(R.id.spinner2);
        List<String> list = new ArrayList<String>();
        list.add("русский");
        list.add("испанский");
        list.add("английский");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter);
    }

    /**
     * Helper method to insert card data into the database.
     */
    private void insertProduct(String word, String translation) {
        // Create a ContentValues object where column names are the keys,
        // and template product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, word );
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 1);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 1);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, translation);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, "Переведено сервисом «Яндекс.Переводчик», translate.yandex.ru/");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all words in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from words database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated card data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Update the UI with the given translation information.
     */
    private void updateUi(Card card) {
        TextView wordTextView = (TextView) findViewById(R.id.word);
        wordTextView.setText(card.word);

        TextView translationTextView = (TextView) findViewById(R.id.translation);
        translationTextView.setText(card.translation);

        TextView yandexAdTextView = (TextView) findViewById(R.id.yandexAd);
        yandexAdTextView.setText(card.yandexAd);
    }

    private class FetchDataTask extends AsyncTask<String, Void, Card> {

        @Override
        protected Card doInBackground(String... params) {

            return Utils.fetchTranslationData(params[0], params[1]);

        }

        @Override
        protected void onPostExecute(Card result) {
            // Update the information displayed to the user.
            updateUi(result);
            insertProduct(result.word, result.translation);
        }

    }

}
