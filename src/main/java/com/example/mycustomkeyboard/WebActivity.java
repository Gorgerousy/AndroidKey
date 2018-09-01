package com.example.mycustomkeyboard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        Intent intent=getIntent();
        String s1=intent.getStringExtra("URL");

        webView=(WebView)findViewById(R.id.web_View1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 判断url链接中是否含有某个字段，如果有就执行指定的跳转（不执行跳转url链接），如果没有就加载url链接
                Log.e("Main2",url);
                if (url.contains("taobao")){
                    Toast.makeText(WebActivity.this,"这是淘宝",Toast.LENGTH_SHORT).show();
                }
                if (url.contains("tieba")){
                    Toast.makeText(WebActivity.this,"这是贴吧",Toast.LENGTH_SHORT).show();
                }
                return super.shouldOverrideUrlLoading(view,url);
            }
        });



        webView.loadUrl(s1);
        Log.e("WebActivity",s1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            webView.goBack();
            return  true;
        }else{
            onBackPressed();
        }
        return super.onKeyDown(keyCode,event);
    }
}
