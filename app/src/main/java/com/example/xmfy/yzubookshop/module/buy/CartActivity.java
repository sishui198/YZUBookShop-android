package com.example.xmfy.yzubookshop.module.buy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xmfy.yzubookshop.R;
import com.example.xmfy.yzubookshop.model.BookInfo;
import com.example.xmfy.yzubookshop.model.CartCollection;
import com.example.xmfy.yzubookshop.model.FormedData;
import com.example.xmfy.yzubookshop.net.AsyncResponse;
import com.example.xmfy.yzubookshop.net.CartAsyncTask;
import com.example.xmfy.yzubookshop.utils.LoginUtils;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private ListView lv_cartList;

    private List<CartCollection> cList;

    private TextView tv_cart_price;

    private Button btn_back;

    private Button btn_cart_order;

    CartCollectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        bindView();
        loadData();
        initClickEvents();
    }

    private void initClickEvents() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CartActivity.this.finish();
            }
        });
        adapter.setOnDeleteItemListern(new CartCollectionAdapter.OnDeleteItemListerner() {
            @Override
            public void onDelete(BookInfo bookInfo) {
                int id = bookInfo.getId();
                CartAsyncTask<Integer> task = new CartAsyncTask<>(CartAsyncTask.TYPE_DELETE);
                task.execute(bookInfo.getId()+"");
                for (int i=0;i<cList.size();i++){
                    CartCollection c = cList.get(i);
                    List<BookInfo> books = c.getBooks();
                    for (int j=0;j<books.size();j++){
                        if (books.get(j).getId() == id){
                            books.remove(j);
                            break;
                        }
                    }
                    if (books.size() == 0)
                        cList.remove(i);
                }
                adapter.update(cList);
                initPrice(cList);
            }
        });

        btn_cart_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<CartCollection> list = adapter.getCheckedBooks();
                if (list.size() == 0){
                    Toast.makeText(CartActivity.this, "请添加需要购买的图书", Toast.LENGTH_SHORT).show();
                }else {
                    Log.e("cart", list.toString());
                }
            }
        });

    }

    private void bindView(){
        lv_cartList = (ListView) findViewById(R.id.lv_cartList);
        tv_cart_price = (TextView) findViewById(R.id.tv_cart_price);
        btn_back = (Button) findViewById(R.id.toolbar_left_btn);
        btn_cart_order = (Button) findViewById(R.id.btn_cart_order);
    }

    private void loadData(){
        cList = new ArrayList<>();
        adapter = new CartCollectionAdapter(CartActivity.this, cList);
        adapter.setOnPriceChangeListener(new BookInfoAdapter.OnPriceChangeListener() {
            @Override
            public void onChange(float price) {
                float origin = Float.parseFloat(tv_cart_price.getText().toString());
                tv_cart_price.setText(String.format("%.2f", origin + price));
            }
        });
        lv_cartList.setAdapter(adapter);
        CartAsyncTask<List<CartCollection>> task = new CartAsyncTask<>(CartAsyncTask.TYPE_QUERY, new AsyncResponse<List<CartCollection>>() {
            @Override
            public void onDataReceivedSuccess(FormedData<List<CartCollection>> formedData) {
                if (formedData.isSuccess()){
                    cList = formedData.getData();
                    adapter.update(cList);
                    initPrice(cList);
                }else {
                    Toast.makeText(CartActivity.this, formedData.getError(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDataReceivedFailed() {

            }
        });
        task.execute(LoginUtils.getAccount(getSharedPreferences("User", MODE_PRIVATE)));
    }

    private void initPrice(List<CartCollection> list){
        float price = 0f;
        for (CartCollection c : list){
            for (BookInfo b : c.getBooks()){
                price += b.getPrice();
            }
        }
        tv_cart_price.setText(String.format("%.2f", price));
    }
}
