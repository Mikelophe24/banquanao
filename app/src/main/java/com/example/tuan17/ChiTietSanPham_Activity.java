package com.example.tuan17;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ChiTietSanPham_Activity extends AppCompatActivity {

    String masp, tendn;
    Button btndathang, btnaddcart;
    private ChiTietSanPham chiTietSanPham;
    private GioHangManager gioHangManager;
    private Database database;
    private TextView txtTongTien; // Thêm biến để hiển thị tổng tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        ImageView backButton = findViewById(R.id.back);

        // Thiết lập sự kiện onClick cho ImageView back
        backButton.setOnClickListener(v -> finish());

        // Khởi tạo các thành phần giao diện
        btndathang = findViewById(R.id.btndathang);
        btnaddcart = findViewById(R.id.btnaddcart);

        TextView tensp = findViewById(R.id.tensp);
        ImageView imgsp = findViewById(R.id.imgsp);
        TextView dongia = findViewById(R.id.dongia);
        TextView mota = findViewById(R.id.mota);
        TextView soluongkho = findViewById(R.id.soluongkho);
        gioHangManager = GioHangManager.getInstance(); // Sử dụng singleton
        TextView textTendn = findViewById(R.id.tendn); // TextView hiển thị tên đăng nhập

        // Khởi tạo database
        database = new Database(this, "banhang.db", null, 1);

        // Lấy tendn từ SharedPreferences
        SharedPreferences sharedPre = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        tendn = sharedPre.getString("tendn", null); // Assign to member variable

        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            Intent intent = new Intent(ChiTietSanPham_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();

        // Nhận chi tiết sản phẩm nếu có
        chiTietSanPham = intent.getParcelableExtra("chitietsanpham");

        // Nếu không có chi tiết sản phẩm, bạn có thể xử lý mã sản phẩm theo cách của riêng bạn
        if (chiTietSanPham != null) {
            masp = chiTietSanPham.getMasp(); // Lấy mã sản phẩm từ chi tiết
            tensp.setText(chiTietSanPham.getTensp());
            dongia.setText(String.valueOf(chiTietSanPham.getDongia()));
            mota.setText(chiTietSanPham.getMota() != null ? chiTietSanPham.getMota() : "Không có dữ liệu");
            soluongkho.setText(String.valueOf(chiTietSanPham.getSoluongkho()));
            byte[] anhByteArray = chiTietSanPham.getAnh();
            if (anhByteArray != null && anhByteArray.length > 0) {
                Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
                imgsp.setImageBitmap(imganhbs);
            } else {
                imgsp.setImageResource(R.drawable.vest); // Ảnh mặc định
            }
        } else {
            tensp.setText("Không có dữ liệu");
        }

        // Kiểm tra trạng thái đăng nhập và thêm sản phẩm vào giỏ hàng
        btnaddcart.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            if (!isLoggedIn) {
                Intent loginIntent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(loginIntent);
            } else {
                if (chiTietSanPham != null) {  // Kiểm tra nếu sản phẩm không bị null
                    gioHangManager.addItem(chiTietSanPham);
                    new AlertDialog.Builder(ChiTietSanPham_Activity.this)
                            .setTitle("Thông báo")
                            .setMessage("Thêm vào giỏ hàng thành công")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    // Hiển thị dialog thông báo sản phẩm không tồn tại
                    new AlertDialog.Builder(ChiTietSanPham_Activity.this)
                            .setTitle("Lỗi")
                            .setMessage("Sản phẩm không tồn tại")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }
        });

        // Thay đổi OnClickListener cho btndathang
        btndathang.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            if (!isLoggedIn) {
                Intent loginIntent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(loginIntent);
            } else {
                // Hiển thị dialog để nhập thông tin thanh toán
                showPaymentDialog();
            }
        });

        // Các nút điều hướng
        setupNavigationButtons();
    }

    private void showPaymentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_thong_tin_thanh_toan);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        setupPaymentDialog(dialog);
        dialog.show();
    }

    private void setupPaymentDialog(Dialog dialog) {
        EditText edtPhoneNumber = dialog.findViewById(R.id.sdt);
        EditText edtAddress = dialog.findViewById(R.id.diachi);
        Button btnConfirm = dialog.findViewById(R.id.btnxacnhandathang);
        TextView txtTongTienDialog = dialog.findViewById(R.id.tienthanhtoan);

        // Hiển thị tổng tiền
        float tongTien = chiTietSanPham.getDongia();
        txtTongTienDialog.setText(String.valueOf(tongTien));

        btnConfirm.setOnClickListener(v -> {
            String sdt = edtPhoneNumber.getText().toString().trim();
            String diaChi = edtAddress.getText().toString().trim();

            if (sdt.isEmpty() || diaChi.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to place the order
            placeOrder(sdt, diaChi);

            // Dismiss the dialog
            dialog.dismiss();
        });
    }

    private void placeOrder(String sdt, String diaChi) {
        if (chiTietSanPham != null) {
            String tenKh = tendn;
            float tongThanhToan = chiTietSanPham.getDongia();

            // Thêm đơn hàng vào bảng Dathang
            SQLiteDatabase db = database.getWritableDatabase();
            ContentValues orderValues = new ContentValues();
            orderValues.put("tenkh", tenKh);
            orderValues.put("diachi", diaChi);
            orderValues.put("sdt", sdt);
            orderValues.put("tongthanhtoan", tongThanhToan);



            long orderId = db.insert("Dathang", null, orderValues);


            if (orderId != -1) {
                // Thêm chi tiết đơn hàng vào bảng Chitietdathang
                ContentValues orderDetailValues = new ContentValues();
                orderDetailValues.put("id_dathang", orderId);
                orderDetailValues.put("masp", chiTietSanPham.getMasp());
                orderDetailValues.put("soluong", 1);
                orderDetailValues.put("dongia", chiTietSanPham.getDongia());
                orderDetailValues.put("anh", chiTietSanPham.getAnh());

                long detailId = db.insert("Chitietdonhang", null, orderDetailValues);

                if (detailId != -1) {
                    new AlertDialog.Builder(ChiTietSanPham_Activity.this)
                            .setTitle("Thông báo")
                            .setMessage("Đơn hàng đã được thêm vào danh sách")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();

                    // Chuyển đến trang danh sách đơn hàng
                    Intent intent1 = new Intent(ChiTietSanPham_Activity.this, DonHang_User_Activity.class);
                    intent1.putExtra("tendn", tenKh);
                    startActivity(intent1);
                } else {
                    Toast.makeText(this, "Không thể thêm chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không thể thêm đơn hàng", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ChiTietSanPham_Activity.this, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationButtons() {
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btngiohang = findViewById(R.id.btncart);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        btntrangchu.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);
            startActivity(intent);
        });

        btntimkiem.setOnClickListener(view -> {
            Intent a = new Intent(ChiTietSanPham_Activity.this, TimKiemSanPham_Activity.class);
            startActivity(a);
        });

        btngiohang.setOnClickListener(view -> navigateToCart());
        btndonhang.setOnClickListener(view -> navigateToOrder());
        btncanhan.setOnClickListener(view -> navigateToProfile());
    }

    private void navigateToCart() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
            startActivity(intent);
        }
    }

    private void navigateToOrder() {
        // Kiểm tra trạng thái đăng nhập của người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Đã đăng nhập, chuyển đến trang đơn hàng
            Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);

            // Truyền tendn qua Intent
            intent.putExtra("tendn", tendn);

            startActivity(intent);
        } else {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        }
    }

    private void navigateToProfile() {
        // Kiểm tra trạng thái đăng nhập của người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Đã đăng nhập, chuyển đến trang cá nhân
            Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);

            // Truyền tendn qua Intent
            intent.putExtra("tendn", tendn);

            startActivity(intent);
        } else {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        }
    }
}
