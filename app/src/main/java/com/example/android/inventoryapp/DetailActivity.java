package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

import static com.example.android.inventoryapp.R.id.image;
import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Constant for e-mail intent
     */
    private static final String URI_EMAIL = "mailto:";
    /**
     * URI loader
     */
    private static final int URI_LOADER = 0;
    // Request code for accessing and using the photos from the storage of the device used
    private static final int READ_REQUEST_CODE = 42;
    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri productUri;
    /**
     * EditText field to enter the product's name
     */
    private EditText nameEditText;
    /**
     * EditText field to enter the product's quantity
     */
    private EditText quantityEditText;
    /**
     * EditText field to enter the product's price
     */
    private EditText priceEditText;
    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean productHasChanged = false;
    /**
     * Variables with Product information
     */
    private String productName;
    private int productQuantity;
    /**
     * TextView to show current product quantity
     */
    private TextView quantityTextView;
    /**
     * Button to order more units from the supplier
     */
    private Button orderButton;
    /**
     * Four Buttons that will be used to update quantity
     */
    private Button increaseQuantityByOneButton;          // Increase by one
    private Button decreaseQuantityByOneButton;          // Decrease by one
    private Button increaseQuantityByManyUnitsButton;    // Increase by many (n)
    private Button decreaseQuantityByManyUnitsButton;    // Decrease by many (n)
    /**
     * Button to select image, ImageView to display selected image
     */

    private ImageView productImageView;

    // Convert from bitmap to byte array
    // Data retrieved from the user gallery that will be converted to byte[] in order to store in database BLOB
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        productUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (productUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.detail_activity_add));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.detail_activity_edit));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(URI_LOADER, null, this);
        }

        // Find all relevant views that we will need to read or show user input
        initialiseViews();

        Button selectImageButton = (Button) findViewById(R.id.button_choose_image);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });

        // Find all relevant views that we will need to read user input from
        nameEditText = (EditText) findViewById(R.id.edit_product_name);
        quantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        priceEditText = (EditText) findViewById(R.id.edit_price);
        productImageView = (ImageView) findViewById(R.id.product_image);


        // Initialise TextView
        quantityTextView = (TextView) findViewById(R.id.quantity_final);


        // Initialise increase Button and set click listener on it
        increaseQuantityByOneButton = (Button) findViewById(R.id.button_increase_by_one);
        increaseQuantityByOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add +1 to product quantity
                productQuantity++;
                //Update UI
                quantityTextView.setText(String.valueOf(productQuantity));
            }
        });

        // Initialise decrease Button and set click listener on it
        decreaseQuantityByOneButton = (Button) findViewById(R.id.button_decrease_by_one);
        decreaseQuantityByOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decrease 1 unit to product quantity if it is higher than 0
                if (productQuantity > 0) {
                    productQuantity--;
                    //Update UI
                    quantityTextView.setText(String.valueOf(productQuantity));
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_invalid_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialise increase large quantity Button and set click listener on it
        increaseQuantityByManyUnitsButton = (Button) findViewById(R.id.button_increase_qty_by_many_units);
        increaseQuantityByManyUnitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity Edit Text is empty and higher than zero
                if (!TextUtils.isEmpty(quantityEditText.getText()) && Integer.valueOf(quantityEditText.getText().toString()) > 0) {
                    // Add the quantity in the Edit Text to the variable keeping track of products stock quantity
                    productQuantity += Integer.valueOf(quantityEditText.getText().toString());
                    // Update to the UI
                    quantityTextView.setText(String.valueOf(productQuantity));
                } else {
                    //Show toast asking user to fill in Edit Text
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_missing_quantity), Toast.LENGTH_SHORT);
                    productQuantity++;
                    //Update UI
                    quantityTextView.setText(String.valueOf(productQuantity));
                }
            }
        });

        // Initialise decrease large quantity Button and set click listener on it
        decreaseQuantityByManyUnitsButton = (Button) findViewById(R.id.button_decrease_qty_by_many_units);
        decreaseQuantityByManyUnitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity Edit Text is empty and higher than zero
                if (!TextUtils.isEmpty(quantityEditText.getText()) && Integer.valueOf(quantityEditText.getText().toString()) > 0) {
                    int newQuantity = productQuantity - Integer.valueOf(quantityEditText.getText().toString());
                    if (newQuantity < 0) {
                        Toast.makeText(DetailActivity.this, getString(R.string.toast_invalid_quantity), Toast.LENGTH_SHORT).show();
                    } else {
                        // Decrease the quantity in the Edit Text to the variable keeping track of product stock quantity
                        productQuantity -= Integer.valueOf(quantityEditText.getText().toString());

                        // Update to the UI
                        quantityTextView.setText(String.valueOf(productQuantity));
                    }
                } else {
                    //Show toast asking user to fill in Edit Text
                    Toast.makeText(DetailActivity.this, getString(R.string.toast_missing_quantity), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void initialiseViews() {
        // Check if there is an existing product to make button visible so the user can order more from the existing product
        if (productUri != null) {
            // Initialise Order Button to order more from the supplier
            orderButton = (Button) findViewById(R.id.button_order);
            // Make Button visible
            orderButton.setVisibility(View.VISIBLE);
            orderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("text/plain");
                    // Defining supplier's e-mail
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"supplier@suppliercompany.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, productName);
                    startActivity(Intent.createChooser(intent, "Send e-mail..."));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri imageUri = null;
            if (resultData != null) {
                imageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());
                productImageView.setImageURI(imageUri);
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Define whether or not EditText fields are empty
        boolean nameIsEmpty = checkFieldEmpty(nameEditText.getText().toString().trim());
        boolean priceIsEmpty = checkFieldEmpty(priceEditText.getText().toString().trim());

        // Check if Name, Quantity or Price are null / zero and inform the user to change to a valid value
        if (nameIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_invalid_name_addition), Toast.LENGTH_SHORT).show();
        } else if (productQuantity <= 0) {
            Toast.makeText(this, getString(R.string.toast_invalid_quantity_addition), Toast.LENGTH_SHORT).show();
        } else if (priceIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_invalid_price_addition), Toast.LENGTH_SHORT).show();
        } else if (productImageView == null) {
            Toast.makeText(this, getString(R.string.toast_invalid_image_addition), Toast.LENGTH_SHORT).show();
        } else {
            // Assuming that all fields are valid, pass the Edit Text value for the name to a String
            String name = nameEditText.getText().toString().trim();
            // Pass the Edit Text value for the price to a double
            double price = Double.parseDouble(priceEditText.getText().toString().trim());

            // Create a ContentValues object where column names are the keys,
            // and product attributes from the editor are the values.
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
            contentValues.put(ProductEntry.COLUMN_PRODUCT_IMAGE, image);

            // Determine if this is a new or existing product by checking if productUri is null or not
            if (productUri == null) {
                // This is a NEW product, so insert a new product into the provider,
                // returning the content URI for the new product.
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

                // Show a toast message to inform the user that the insertion of the new product was successful.
                Toast.makeText(this, getString(R.string.toast_add_product_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If URI is not null, then we are updating and existing product
                int newUri = getContentResolver().update(productUri, contentValues, null, null);

                Toast.makeText(this, getString(R.string.toast_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to define if any of the EditText fields are empty or contain invalid inputs
    // @param string: String received as a parameter to be checked with this method
    private boolean checkFieldEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals(".");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (productUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.add:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the MainActivity.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                } else {

                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the products table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        switch (id) {
            case URI_LOADER:
                return new CursorLoader(this,   // Parent activity context
                        productUri,         // Query the content URI for the current product
                        projection,             // Columns to include in the resulting Cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);                  // Default sort order
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            // Extract out the value from the Cursor for the given column index
            // Update the views on the screen with the values from the database
            productName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            nameEditText.setText(productName);
            productQuantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
            priceEditText.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)));
            if (cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE)) != null) {
                productImageView.setImageBitmap(getImage(cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE))));
            }
        }
    }

    // Convert from byte array to bitmap
    // BLOB from the database converted to Bitmap in order to display in the UI
    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.getText().clear();
        quantityEditText.getText().clear();
        quantityTextView.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.option_leave_without_saving));
        builder.setPositiveButton(getString(R.string.option_yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (productUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the productUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(productUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.toast_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.option_delete_product));
        builder.setPositiveButton(getString(R.string.option_delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();

                // Close the activity
                finish();
            }
        });
        builder.setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}




