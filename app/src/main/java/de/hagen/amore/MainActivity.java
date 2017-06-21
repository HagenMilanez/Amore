package de.hagen.amore;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private Map<String, String> whatsupContacts;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    public void openDialog1() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.alert_title));
        alertDialog.setMessage(getString(R.string.alert_text));
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.alert_toast), Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about: {
                openDialog1();
                break;
            }
            case R.id.exit: {
                this.finishAffinity();
                System.exit(0);
                break;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        this.whatsupContacts = getWhatsUpContacts();
        setSupportActionBar(myToolbar);
        loadLover();
        final MediaPlayer broken = MediaPlayer.create(this, R.raw.broken_heart);
        final MediaPlayer arrow = MediaPlayer.create(this, R.raw.arrow);


        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }


        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrow.start();
                String name = getContactName();
                String number = searchForContact(name);
                saveLover(name);
                openWhatsappContact(number);
            }
        });

        Button clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broken.start();
                saveLover("");
                setContactName("");
            }
        });

    }


    private void loadLover() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String contactName = sharedPref.getString("contactName", "");
        setContactName(contactName);
    }

    private void saveLover(String contactName) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("contactName", contactName);
        editor.commit();
    }

    private String searchForContact(String name) {
        return getContactByName(name);
    }

    @NonNull
    private String getContactName() {
        EditText nameView = (EditText) findViewById(R.id.name);
        return nameView.getText().toString();
    }

    private void setContactName(String contactName) {
        EditText nameView = (EditText) findViewById(R.id.name);
        nameView.setText(contactName);
    }

    private String getContactByName(String name) {
        return this.whatsupContacts.get(name);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getText(R.string.permission_message), Toast.LENGTH_SHORT).show();
            }
        }
    }


    void openWhatsappContact(String number) {
        Intent sendIntent = new Intent();
        if (number != null) {
            sendIntent.putExtra("jid", number.replace("+", "").replace(" ", "") + "@s.whatsapp.net");
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, getMessage());
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    private String getMessage() {
        Random r = new Random();
        String[] messages = getResources().getStringArray(R.array.messages);
        int randomNumber = r.nextInt(messages.length);
        return messages[randomNumber];
    }

    private Map<String, String> getWhatsUpContacts() {
        Cursor cursor = null;
        Cursor phoneCursor = null;
        String number = null;
        HashMap<String, String> myWhatsappContacts = new HashMap<>();
            try {
                cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.Data.RAW_CONTACT_ID},
                     ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'",
                        new String[]{}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        try {
                                //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                                String whatsappContactId = cursor.getString(0);

                                if (whatsappContactId != null) {
                                    //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                                    Cursor whatsAppContactCursor = getContentResolver().query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{whatsappContactId}, null);

                                    if (whatsAppContactCursor != null && whatsAppContactCursor.moveToFirst()) {
                                        String id = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                        String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                        String phoneNumber = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        whatsAppContactCursor.close();

                                        //Add Number to ArrayList
                                        myWhatsappContacts.put(name, phoneNumber);
                                    }

                                }


                        } finally {
                            if (phoneCursor != null) {
                                phoneCursor.close();
                            }
                        }
                    } while (cursor.moveToNext());

                }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return myWhatsappContacts;
    }

}
