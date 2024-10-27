package com.example.tuan17;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class DonHang_Adapter extends ArrayAdapter<Order> {
    public DonHang_Adapter(Context context, List<Order> orders) {
        super(context, 0, orders);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ds_donhang, parent, false);
        }

        // Find views
        Order order = getItem(position);
        TextView txtMadh = convertView.findViewById(R.id.txtMahd);
        TextView txtTenKh = convertView.findViewById(R.id.txtTenKh);
        TextView txtDiaChi = convertView.findViewById(R.id.txtDiaChi);
        TextView txtSdt = convertView.findViewById(R.id.txtSdt);
        TextView txtTongThanhToan = convertView.findViewById(R.id.txtTongThanhToan);
        TextView txtNgayDatHang = convertView.findViewById(R.id.txtNgayDatHang);
        Button btnCancelOrder = convertView.findViewById(R.id.btnCancelOrder);

        // Set data to views
        if (order != null) {
            txtTenKh.setText(order.getTenKh());
            txtDiaChi.setText(order.getDiaChi());
            txtSdt.setText(order.getSdt());
            txtTongThanhToan.setText(String.valueOf(order.getTongTien()));
            txtNgayDatHang.setText(order.getNgayDatHang());
            txtMadh.setText(String.valueOf(order.getId()));

            // Set onClickListener for the cancel button
            btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmCancellation(order);
                }
            });
        }

        return convertView;
    }

    private void confirmCancellation(Order order) {
        new AlertDialog.Builder(getContext())
                .setTitle("Hủy Đơn Hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelOrder(order);
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder(Order order) {
        try {
            // Delete order from database
            Database database = new Database(getContext(), "banhang.db", null, 1);
            database.deleteOrderById(order.getId());

            // Remove the order from the list and update the adapter
            remove(order);
            notifyDataSetChanged();

            Toast.makeText(getContext(), "Đơn hàng đã được hủy", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hủy đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
