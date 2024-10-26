package com.example.tuan17;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GioHangAdapter extends ArrayAdapter<GioHang> {
    private Context context;
    private List<GioHang> items;
    private TextView txtTongTien; // Tham chiếu tới TextView tổng tiền
    private GioHangManager gioHangManager;


    public GioHangAdapter(Context context, List<GioHang> items, TextView txtTongTien) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.txtTongTien = txtTongTien;
        this.gioHangManager = GioHangManager.getInstance(); // Khởi tạo GioHangManager
        updateTongTien(); // Cập nhật tổng tiền ban đầu
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position; // Capture position
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ds_giohang, parent, false);
        }

        TextView tensp = convertView.findViewById(R.id.tensp);
        ImageView imgSanPham = convertView.findViewById(R.id.imgsp);
        TextView masp = convertView.findViewById(R.id.masp);
        TextView dongia = convertView.findViewById(R.id.dongia);
        TextView soLuong = convertView.findViewById(R.id.soluongdat);
        ImageButton btnGiam = convertView.findViewById(R.id.btnTru);
        ImageButton btnTang = convertView.findViewById(R.id.btnCong);
        ImageButton btndonhang = convertView.findViewById(R.id.btndonhang);
//        TextView xoasp = convertView.findViewById(R.id.xoasp);
        ImageButton xoasp = (ImageButton) convertView.findViewById(R.id.xoasp);


        GioHang item = items.get(position);
        tensp.setText(item.getSanPham().getTensp());
        dongia.setText(String.valueOf(item.getSanPham().getDongia()));
        soLuong.setText(String.valueOf(item.getSoLuong()));
        masp.setText(item.getSanPham().getMasp());

        byte[] anhByteArray = item.getSanPham().getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganh = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            imgSanPham.setImageBitmap(imganh);
        } else {
            imgSanPham.setImageResource(R.drawable.vest);
        }

        // Thiết lập sự kiện cho nút tăng số lượng
        btnTang.setOnClickListener(v -> {
            gioHangManager.addItem(item.getSanPham());
            notifyDataSetChanged();
            updateTongTien();
        });
//        btndonhang.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //kiểm tra trạng thái đăng nhập của ng dùng
//                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
//
//                // Đã đăng nhập, chuyển đến trang đơn hàng
//                Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);
//
//                startActivity(intent);
//            }
//
//        });

        // Thiết lập sự kiện cho nút giảm số lượng
        btnGiam.setOnClickListener(v -> {
            if (item.getSoLuong() > 1) {
                item.setSoLuong(item.getSoLuong() - 1);
            } else {
                gioHangManager.removeItem(position);
                items.remove(position);
            }
            notifyDataSetChanged();
            updateTongTien();
        });

        // Thiết lập sự kiện cho TextView xoasp
        xoasp.setOnClickListener(v -> {
            gioHangManager.removeItem(pos); // Use pos instead of position
            notifyDataSetChanged();
            updateTongTien();
            Toast.makeText(context, "Sản phẩm đã được xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        });



        return convertView;
    }

    private void updateTongTien() {
        float tongTien = gioHangManager.getTongTien(); // Lấy tổng tiền từ GioHangManager
        txtTongTien.setText(String.valueOf(tongTien)); // Cập nhật TextView
    }








}
