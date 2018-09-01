package com.example.mycustomkeyboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.KeyboardView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private KeyBoardEditText text;
    private KeyboardView keyboardView;
    private LinearLayout layout;
    private LinearLayout root;
    private int height = 0;

    //定义WebView的编辑框和发送按钮
    private Button SendButton;
    private EditText httpEdit;

    //定义文本显示控件
    private TextView serverData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ed_main是一个自定义控件的ID，该控件可以触发自定义键盘
        //它继承自系统的EditText
        text = (KeyBoardEditText) findViewById(R.id.ed_main);

        //下面绑定了一个键盘视图的布局文件
        keyboardView = (KeyboardView) findViewById(R.id.view_keyboard);

        //该布局文件中包含上面那个键盘视图文件
        //该布局相当于上面那个布局的父布局，可以为其基本的变量赋值
        layout = (LinearLayout) findViewById(R.id.layout_main);

        root = (LinearLayout) findViewById(R.id.layout_root);

        //绑定编辑框和按钮的xml文件
        httpEdit = (EditText)findViewById(R.id.input_http);
        SendButton = (Button)findViewById(R.id.send_http);

        //绑定服务器显示文本TextView
        serverData = (TextView)findViewById(R.id.server_show);

        //监听按钮的点击行为
        SendButton.setOnClickListener(this);

        text.setKeyboardType(layout, keyboardView, true);

        text.setOnKeyBoardStateChangeListener(new KeyBoardEditText.OnKeyboardStateChangeListener() {
            @Override
            public void show() {
                root.post(new Runnable() {
                    @Override
                    public void run() {

                        int[] pos = new int[2];
                        //获取编辑框在整个屏幕中的坐标
                        text.getLocationOnScreen(pos);
                        //编辑框的Bottom坐标和键盘Top坐标的差
                        height = (pos[1] + text.getHeight()) -
                                (getScreenHeight(MainActivity.this) - keyboardView.getHeight());
                        if (height > 0) {
                            root.scrollBy(0, height + dp2px(MainActivity.this, 16));
                        }
                    }
                });
            }

            @Override
            public void hide() {

                if (height > 0) {
                    root.scrollBy(0, -(height + dp2px(MainActivity.this, 16)));
                }
            }
        });

        //Log.i("zhangdi", getLngAndLat(this));
    }


    //点击事件响应
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.send_http:
                String urlText=httpEdit.getText().toString();

                Log.d("PGNIP", "IP:"+urlText);

                if(isIP(urlText))
                {
                    Log.d("PGNIP", "IP数字检测成功");

                    Toast.makeText(MainActivity.this,"服务器请求已发送",Toast.LENGTH_SHORT).show();

                    HttpUtil.sendOkHttpRequest(urlText, new okhttp3.Callback(){
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            //得到服务器返回的具体内容
                            String responseData = response.body().string();

                            //将返回的数据显示到界面
                            serverData.setText(responseData);
                        }

                        @Override
                        public void onFailure(Call call, IOException e)
                        {
                            //处理异常情况，先不补充
                        }
                    });
                }
                else if (!urlText.contains("http://"))
                {
                    urlText="http://"+urlText;
                    Intent intent1=new Intent (MainActivity.this,WebActivity.class);
                    Toast.makeText(MainActivity.this,"提交成功",Toast.LENGTH_SHORT).show();
                    intent1.putExtra("URL",urlText);
                    Log.e("WebButton",urlText);
                    startActivity(intent1);
                }
        }
    }

    public boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 通过正则表达式判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return dip
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

}
