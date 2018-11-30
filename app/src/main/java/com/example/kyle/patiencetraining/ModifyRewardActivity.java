package com.example.kyle.patiencetraining;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.OpenableColumns;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class ModifyRewardActivity extends AppCompatActivity {
    /**
     * Todo: make top nav bar have back button
     * Todo: make activity work for modify reward (onlongpress)
     * Todo: Only allow longer duration
     *
     */
    Uri imageUri;
    private static final int GET_FROM_GALLERY = 0;
    int hours = 1;
    int days;
    int weeks;
    private TextView fileName;
    private ImageButton clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_reward);

        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });


        fileName = findViewById(R.id.fileName);
        clearButton = findViewById(R.id.removeImageButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = null;
                fileName.setText("");
                clearButton.setVisibility(View.INVISIBLE);
            }
        });

        final Button durationButton = findViewById(R.id.durationPicker);
        durationButton.setText(getString(R.string.duration, hours, days, weeks));

        final DurationDialog dialog = new DurationDialog(this, getString(R.string.missing_input,getString(R.string.missing_duration)), new DurationDialog.OnDurationSetListener() {
            @Override
            public void onDurationSet(int hourSet, int daySet, int weekSet) {
                hours = hourSet;
                days = daySet;
                weeks = weekSet;
                durationButton.setText(getString(R.string.duration, hours, days, weeks));
            }
        });

        durationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setHours(hours);
                dialog.setDays(days);
                dialog.setWeeks(weeks);
                dialog.show();
            }
        });

        final EditText name = findViewById(R.id.nameEditText);
        final EditText price = findViewById(R.id.priceEditText);

        final EditText link = findViewById(R.id.linkEditText);
        final SwitchMaterial notification = findViewById(R.id.notificationSwitch);

        FloatingActionButton fab = findViewById(R.id.modifyFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().length() == 0)
                    name.setError(getString(R.string.missing_input,getString(R.string.missing_name)));
                else
                    createReward(name.getText(),price.getText(), link.getText(),notification.isChecked());

            }
        });
    }

    public void createReward(Editable name, Editable price, Editable link, boolean notification){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.add(Calendar.WEEK_OF_YEAR, weeks);
        Date endDate = calendar.getTime();

        float priceFloat = 0f;
        if(price.length() != 0)
            priceFloat = Float.parseFloat(price.toString());

        Reward reward = new Reward(name.toString(),priceFloat,now,endDate, link.toString(),imageUri,notification);

        Intent intent = new Intent();
        intent.putExtra(MainActivity.REWARD_EXTRA, reward);
        setResult(RESULT_OK,intent);
        finish();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == GET_FROM_GALLERY){
            if(resultCode == RESULT_OK){
                imageUri = data.getData();
                fileName.setText(getFileName(imageUri));
                clearButton.setVisibility(View.VISIBLE);
            }
        }
    }
}