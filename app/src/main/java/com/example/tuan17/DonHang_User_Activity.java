package com.example.tuan17;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class DonHang_User_Activity extends AppCompatActivity {
    private Database database;
    private ListView listView;
    private DonHang_Adapter donHangAdapter;
    private String tendn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang_user);

        ImageView backButton = findViewById(R.id.back);

        // Thiết lập sự kiện onClick cho ImageView back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kết thúc Activity hiện tại và trở về màn hình trước
            }
        });

        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);


        // Khởi tạo các thành phần
        listView = findViewById(R.id.listViewChiTiet);
        database = new Database(this, "banhang.db", null, 1);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId() != R.id.btnCancelOrder) {
                Order order = donHangAdapter.getItem(position);

                if (order != null) {
                    // Hiển thị Toast với ID đơn hàng
                    Toast.makeText(DonHang_User_Activity.this, "ID đơn hàng: " + order.getId(), Toast.LENGTH_SHORT).show();

                    // Gửi thông tin đơn hàng qua Intent
                    Intent intent = new Intent(DonHang_User_Activity.this, ChiTietDonHang_Activity.class);
                    intent.putExtra("donHangId", String.valueOf(order.getId())); // Đảm bảo rằng ID là chuỗi
                    startActivity(intent);
                }
            }}


        });


        // Tạo bảng nếu chưa tồn tại
        createTableIfNotExists();

        // Lấy giá trị tendn từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        tendn = sharedPreferences.getString("tendn", null);

        // Nếu SharedPreferences không có, lấy từ Intent
        if (tendn == null) {
            tendn = getIntent().getStringExtra("tendn");
        }

        // Kiểm tra giá trị tendn
        if (tendn == null) {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(DonHang_User_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }

        loadDonHang(tendn); // Gọi phương thức loadDonHang với tenDN

        btncard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    // Chưa đăng nhập, chuyển đến trang login
                    Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                    startActivity(intent);
                } else {
                    // Đã đăng nhập, chuyển đến trang 2
                    Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
                    startActivity(intent);
                }
            }
        });
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đã đăng nhập, chuyển đến trang đơn hàng
                Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);

                startActivity(intent);
            }
        });
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                // Đã đăng nhập, chuyển đến trang đơn hàng
                Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);

                startActivity(intent);
            }

        });
        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    // Chưa đăng nhập, chuyển đến trang login
                    Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                    startActivity(intent);
                } else {
                    // Đã đăng nhập, chuyển đến trang 2
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);
                    startActivity(intent);
                }
            }
        });

        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),TimKiemSanPham_Activity.class);
                startActivity(a);
            }
        });
    }


    private void createTableIfNotExists() {
        // Tạo bảng đơn hàng nếu chưa tồn tại
        database.QueryData("CREATE TABLE IF NOT EXISTS Dathang (" +
                "id_dathang INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tenkh TEXT, " +
                "diachi TEXT, " +
                "sdt TEXT, " +
                "tongthanhtoan REAL, " +
                "ngaydathang DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    private void loadDonHang(String tenKh) {
        // Kiểm tra tên khách hàng trước khi truy vấn
        if (tenKh == null || tenKh.isEmpty()) {
            Toast.makeText(this, "Tên khách hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
            return; // Dừng lại nếu tên khách hàng là null hoặc rỗng
        }

//         Lấy danh sách đơn hàng từ cơ sở dữ liệu
        List<Order> orders = database.getDonHangByTenKh(tenKh);
        if (orders.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng cho khách hàng này!", Toast.LENGTH_SHORT).show();
        } else {
            // Sử dụng DonHangAdapter để hiển thị danh sách đơn hàng
            donHangAdapter = new DonHang_Adapter(this, orders);
            listView.setAdapter(donHangAdapter); // Gán adapter cho ListView
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reload the orders when the activity resumes
        loadDonHang(tendn);
    }
}