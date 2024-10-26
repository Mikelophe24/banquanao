package com.example.tuan17;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import java.util.List;

public class GioHang_Activity extends AppCompatActivity {
    private ListView listView;
    private GioHangAdapter adapter;
    private GioHangManager gioHangManager;
    private Button thanhtoan;
    private Database database;
    private OrderManager orderManager;
    private TextView txtTongTien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gio_hang);

        initializeViews();
        initializeDatabase();
        setupBackButton();
        loadUsername();
        setupNavigationButtons();
        loadCartData();
        setupCheckoutButton();
    }

    private void initializeViews() {
        listView = findViewById(R.id.listtk);
        txtTongTien = findViewById(R.id.tongtien);
        thanhtoan = findViewById(R.id.btnthanhtoan);
    }

    private void initializeDatabase() {
        database = new Database(this, "banhang.db", null, 1);
        database.QueryData("CREATE TABLE IF NOT EXISTS Dathang (" +
                "id_dathang INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tenkh TEXT, diachi TEXT, sdt TEXT, " +
                "tongthanhtoan REAL, ngaydathang DATETIME DEFAULT CURRENT_TIMESTAMP);");
        gioHangManager = GioHangManager.getInstance();
        orderManager = new OrderManager(this);
    }

    private void setupBackButton() {
        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadUsername() {
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String tendn = sharedPrefs.getString("tendn", null);

        if (tendn != null) {
            TextView textTendn = findViewById(R.id.tendn);
            textTendn.setText(tendn);
        } else {
            navigateToLogin();
        }
    }

    private void setupNavigationButtons() {
        ImageButton btnCart = findViewById(R.id.btncart);
        ImageButton btnHome = findViewById(R.id.btntrangchu);
        ImageButton btnOrders = findViewById(R.id.btndonhang);
        ImageButton btnProfile = findViewById(R.id.btncanhan);
        ImageButton btnSearch = findViewById(R.id.btntimkiem);

        btnCart.setOnClickListener(view -> checkLoginAndNavigate(GioHang_Activity.class));
        btnHome.setOnClickListener(view -> navigateTo(TrangchuNgdung_Activity.class));
        btnOrders.setOnClickListener(view -> checkLoginAndNavigate(DonHang_User_Activity.class));
        btnProfile.setOnClickListener(view -> checkLoginAndNavigate(TrangCaNhan_nguoidung_Activity.class));
        btnSearch.setOnClickListener(view -> navigateTo(TimKiemSanPham_Activity.class));
    }

    private void loadCartData() {
        List<GioHang> gioHangList = gioHangManager.getGioHangList();
        adapter = new GioHangAdapter(this, gioHangList, txtTongTien);
        listView.setAdapter(adapter);
        txtTongTien.setText(String.valueOf(gioHangManager.getTongTien()));
    }

    private void setupCheckoutButton() {
        thanhtoan.setOnClickListener(v -> showPaymentDialog());
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
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String tendn = sharedPrefs.getString("tendn", null);

        EditText edtDiaChi = dialog.findViewById(R.id.diachi);
        EditText edtSdt = dialog.findViewById(R.id.sdt);
        Button btnConfirmOrder = dialog.findViewById(R.id.btnxacnhandathang);
        TextView tvTongTien = dialog.findViewById(R.id.tienthanhtoan);

        String totalAmount = txtTongTien.getText().toString();
        tvTongTien.setText(totalAmount);

        btnConfirmOrder.setOnClickListener(v -> {
            if (edtDiaChi.getText().toString().trim().isEmpty() || edtSdt.getText().toString().trim().isEmpty()) {
                showDialog("Vui lòng điền đầy đủ thông tin!");
                return;
            }

            try {
                float finalTotal = Float.parseFloat(totalAmount.replace(",", ""));
                processOrder(dialog, edtDiaChi.getText().toString().trim(), edtSdt.getText().toString().trim(), tendn, finalTotal);
            } catch (NumberFormatException e) {
                showDialog("Có lỗi xảy ra với tổng tiền!");
            }
        });
    }

    private void processOrder(Dialog dialog, String address, String phone, String customerName, float totalAmount) {
        long orderId = orderManager.addOrder(address, phone, customerName, totalAmount);
        if (orderId > 0) {
            saveOrderDetails(orderId);
            showDialog("Đặt hàng thành công!");
            clearCart();
        } else {
            showDialog("Đặt hàng thất bại!");
        }
        dialog.dismiss();
    }

    private void saveOrderDetails(long orderId) {
        List<GioHang> cartItems = gioHangManager.getGioHangList();
        for (GioHang item : cartItems) {
            String productId = item.getSanPham().getMasp();
            int quantity = item.getSoLuong();
            float price = item.getSanPham().getDongia();
            byte[] image = item.getSanPham().getAnh();

            orderManager.addOrderDetails((int) orderId, productId, quantity, price, image);
        }
    }

    private void clearCart() {
        gioHangManager.clearGioHang();
        txtTongTien.setText("0");
        adapter.notifyDataSetChanged();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkLoginAndNavigate(Class<?> activityClass) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            navigateTo(activityClass);
        } else {
            navigateToLogin();
        }
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(getApplicationContext(), activityClass);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(GioHang_Activity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }
}
