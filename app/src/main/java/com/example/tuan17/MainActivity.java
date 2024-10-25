package com.example.tuan17;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chuyển ngay sang Login_Activity
        Intent intent = new Intent(MainActivity.this, Login_Activity.class);
        startActivity(intent);
        finish(); // Kết thúc MainActivity nếu không muốn quay lại
    }
}
