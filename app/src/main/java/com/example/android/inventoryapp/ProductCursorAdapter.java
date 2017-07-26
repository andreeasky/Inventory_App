package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productName = (TextView) view.findViewById(R.id.product_name_text_view);
        TextView productQuantity = (TextView) view.findViewById(R.id.quantity_text_view);
        TextView productPrice = (TextView) view.findViewById(R.id.price_text_view);

        //Extract values from Cursor object
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow((ProductEntry._ID))));


        // Populate TextViews with values extracted from Cursor object
        productName.setText(name);
        productPrice.setText(context.getString(R.string.label_price) + " " + price);
        productQuantity.setText(quantity + " " + context.getString(R.string.label_quantity));

        // Find sale Button
        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        // Set click listener on the Button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity in stock is higher than zero
                if (quantity > 0) {
                    // Assign a new quantity value of minus one to represent one product sold
                    int newQuantity = quantity - 1;
                    // Create and initialise a new ContentValue object with the new quantity
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    //Update the database
                    context.getContentResolver().update(uri, contentValues, null, null);
                } else {
                    // Inform the user that the quantity is zero and that it can't be updated
                    Toast.makeText(context, context.getString(R.string.toast_product_out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}




