package com.example.tuan17;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login_Activity extends AppCompatActivity {

    private Database database;
    private String tendn;
    private Handler handler = new Handler();
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 30000; // 30 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        EditText tdn = findViewById(R.id.tdn);
        EditText mk = findViewById(R.id.mk);
        TextView dangki = findViewById(R.id.dangki);
        TextView qmk = findViewById(R.id.qmk);

        qmk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoiMatKhau_Activity.class);
                startActivity(intent);
            }
        });

        database = new Database(this, "banhang.db", null, 1);

        // Chuyển đến activity đăng ký tài khoản
        dangki.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), DangKiTaiKhoan_Activity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện đăng nhập
        btnLogin.setOnClickListener(v -> {
            String username = tdn.getText().toString();
            String password = mk.getText().toString();

            if (kiemTraDangNhap(username, password)) {
                tendn = username;

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("tendn", tendn);
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                // Khởi động bộ đếm thời gian tự động đăng xuất
//                khoiDongBoDemDangXuat();

                // Chuyển đến activity phù hợp theo quyền
                String quyen = layQuyenNguoiDung(username);

                // Kiểm tra giá trị của biến quyen
                Log.d("Login_Activity", "Quyền của người dùng: " + quyen);

                Intent intent;

                if (quyen.equals("admin")) {
                    intent = new Intent(Login_Activity.this, TrangchuAdmin_Activity.class);
                    Toast.makeText(this, "Đăng nhập với quyền Admin", Toast.LENGTH_SHORT).show();
                } else if (quyen.equals("user")) {
                    intent = new Intent(Login_Activity.this, TrangchuNgdung_Activity.class);
                    intent.putExtra("tendn", tendn);
                    Toast.makeText(this, "Đăng nhập với quyền User", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Quyền không xác định", Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login_Activity.this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm kiểm tra thông tin đăng nhập
    private boolean kiemTraDangNhap(String username, String password) {
        Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT * FROM taikhoan WHERE LOWER(tendn) = LOWER(?) AND matkhau = ?",
                new String[]{username, password});

        boolean isValid = cursor.getCount() > 0;
        Log.d("Login_Activity", "Xác thực người dùng: " + (isValid ? "Thành công" : "Thất bại"));

        cursor.close();
        return isValid;
    }

    // Hàm lấy quyền người dùng
    private String layQuyenNguoiDung(String username) {
        String quyen = "";
        Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT quyen FROM taikhoan WHERE tendn = ?",
                new String[]{username});

        if (cursor.moveToFirst()) {
            int quyenColumnIndex = cursor.getColumnIndex("quyen");
            if (quyenColumnIndex != -1) {
                quyen = cursor.getString(quyenColumnIndex);
            } else {
                Log.e("Error", "Cột 'quyen' không tìm thấy trong tập kết quả");
            }
        } else {
            Log.e("Error", "Không tìm thấy người dùng với tên đăng nhập: " + username);
        }
        cursor.close();
        return quyen;
    }

    // Hàm khởi động bộ đếm thời gian tự động đăng xuất
    private void khoiDongBoDemDangXuat() {
        handler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.putString("tendn", null);
                editor.apply();

                Intent intent = new Intent(Login_Activity.this, TrangchuNgdung_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        };

        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
//        khoiDongBoDemDangXuat();
    }
}
