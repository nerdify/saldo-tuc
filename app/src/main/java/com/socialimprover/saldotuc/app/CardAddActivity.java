package com.socialimprover.saldotuc.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CardAddActivity extends ActionBarActivity {

    public static final String TAG = CardAddActivity.class.getSimpleName();

    protected CardDataSource mDataSource;
    protected Card mNewCard;

    protected RelativeLayout mNotificationLayout;
    protected EditText mName;
    protected EditText mNumber;
    protected CheckBox mNotificationCheckBox;
    protected EditText mPhone;
    protected Spinner mHourSpinner;
    protected Spinner mAmPmSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_card_add);

        mDataSource = new CardDataSource(this);

        mNotificationLayout = (RelativeLayout) findViewById(R.id.notificationLayout);
        mName = (EditText) findViewById(R.id.nameField);
        mNumber = (EditText) findViewById(R.id.cardField);
        mNotificationCheckBox = (CheckBox) findViewById(R.id.notificationCheckBox);
        mPhone = (EditText) findViewById(R.id.phoneField);
        mHourSpinner = (Spinner) findViewById(R.id.hour_spinner);
        mAmPmSpinner = (Spinner) findViewById(R.id.ampm_spinner);

        mNotificationCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

        setHourAdapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            String name = mName.getText().toString().trim();
            String card = mNumber.getText().toString().trim();
            String phone = mPhone.getText().toString().trim();
            String hour = mHourSpinner.getSelectedItem().toString();
            String ampm = mAmPmSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(card) || card.length() != 8) {
                validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_error_message));
            } else {
                mNewCard = new Card();
                mNewCard.setName(name);
                mNewCard.setNumber(card);

                if (mNotificationCheckBox.isChecked()) {
                    if (TextUtils.isEmpty(phone) || phone.length() != 8) {
                        validationErrorMessage(getString(R.string.error_title), getString(R.string.card_add_phone_error_message));
                    } else {
                        setSupportProgressBarIndeterminateVisibility(true);

                        mNewCard.setPhone(phone);
                        mNewCard.setHour(hour);
                        mNewCard.setAmpm(ampm);

                        SaldoTucService service = new SaldoTucService();
                        service.storeCard(mNewCard, new Callback<Card>() {
                            @Override
                            public void success(Card card, Response response) {
                                removeProgressBar();

                                saveCard(mNewCard);

                                Intent intent = new Intent(CardAddActivity.this, PhoneVerificationActivity.class);
                                intent.putExtra("phone", mNewCard.getPhone());
                                startActivity(intent);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                removeProgressBar();

                                if (error.getResponse().getStatus() == 409) {
                                    validationErrorMessage(getString(R.string.error_title), "Este número de télefono ya esta asociado a otra tarjeta TUC");
                                }

                                Log.e(TAG, "Error: " + error.getMessage());
                            }
                        });
                    }
                } else {
                    saveCard(mNewCard);

                    finish();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setHourAdapters() {
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this, R.array.hours, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mHourSpinner.setAdapter(hourAdapter);

        ArrayAdapter<CharSequence> amPmAdapter = ArrayAdapter.createFromResource(this, R.array.ampm, android.R.layout.simple_spinner_item);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAmPmSpinner.setAdapter(amPmAdapter);
    }

    protected void validationErrorMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void saveCard(Card card) {
        mDataSource.create(card);

        Toast.makeText(this, R.string.card_success, Toast.LENGTH_LONG).show();
    }

    protected void removeProgressBar() {
        setSupportProgressBarIndeterminateVisibility(false);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    protected CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isNetworkAvailable()) {
                if (isChecked) {
                    mNotificationLayout.setVisibility(RelativeLayout.VISIBLE);
                } else {
                    mNotificationLayout.setVisibility(RelativeLayout.INVISIBLE);
                }
            } else {
                validationErrorMessage(getString(R.string.error_title), "Necesitas conexión a internet para habilitar las notificaciones SMS.");
                mNotificationCheckBox.setChecked(false);
            }
        }
    };

}
