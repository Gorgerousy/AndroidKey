package com.example.mycustomkeyboard;

import android.content.Context;
import android.hardware.input.InputManager;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by zd on 2018/4/2.
 */

public class KeyBoardEditText extends AppCompatEditText implements KeyboardView.OnKeyboardActionListener {

    /**数字键盘*/
    private Keyboard keyboardNumber;
    /**字母键盘*/
    private Keyboard keyboardLetter;

    private ViewGroup viewGroup;
    private KeyboardView keyboardView;

    /**是否发生键盘切换*/
    private boolean changeLetter = false;
    /**是否为大写*/
    private boolean isCapital = false;

    private int[] arrays = new int[]{Keyboard.KEYCODE_SHIFT, Keyboard.KEYCODE_MODE_CHANGE,
            Keyboard.KEYCODE_CANCEL, Keyboard.KEYCODE_DONE, Keyboard.KEYCODE_DELETE,
            Keyboard.KEYCODE_ALT, 32};

    //该数组存放所有的功能键
    private List<Integer> noLists = new ArrayList<>();

    //分别建立储存按压和释放按键的数组
    private List<KeyTime> pressList = new ArrayList<>();
    private List<KeyTime> releaseList = new ArrayList<>();

    //分别建立大小写字母和数字的Key值数组
    private List<Keyboard.Key> numberKeyList;
    private List<Keyboard.Key> letterSmallKeyList;
    private List<Keyboard.Key> letterBigKeyList;


    //建立一个键盘监听器
    private OnKeyboardStateChangeListener listener;


    public KeyBoardEditText(Context context) {
        super(context);
        initEditView();
    }

    public KeyBoardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditView();
    }

    public KeyBoardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditView();
    }

    /**初始化数字和字母键盘*/
    private void initEditView() {
        //将键盘布局与键盘对象进行绑定
        keyboardNumber = new Keyboard(getContext(), R.xml.keyboard_num);
        keyboardLetter = new Keyboard(getContext(), R.xml.keyboard_letter);

        //将各种键盘的Key加入到各自的数组中
        numberKeyList = keyboardNumber.getKeys();
        letterSmallKeyList = keyboardLetter.getKeys();

        //将大写键盘也加入数组中
        changeCapital(!isCapital);
        letterBigKeyList = keyboardLetter.getKeys();
        //将键盘再次变为小写字母
        changeCapital(!isCapital);

        for (int i=0; i<arrays.length; i++) {
            noLists.add(arrays[i]);
        }
    }

    /**
     * 设置软键盘刚弹出的时候显示字母键盘还是数字键盘
     * @param vg 包裹KeyboardView的ViewGroup
     * @param kv KeyboardView
     * @param keyboard_num 是否显示数字键盘
     */
    public void setKeyboardType (ViewGroup vg, KeyboardView kv, boolean keyboard_num) {

        viewGroup = vg;
        keyboardView = kv;
        if (keyboard_num) {
            keyboardView.setKeyboard(keyboardNumber);      //设置键盘类型
            changeLetter = false;
        } else {
            keyboardView.setKeyboard(keyboardLetter);
            changeLetter = true;
        }

        //显示预览
        keyboardView.setPreviewEnabled(true);
        //为KeyboardView设置按键监听
        keyboardView.setOnKeyboardActionListener(this);
    }

    public void setOnKeyBoardStateChangeListener(OnKeyboardStateChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 新建立一个储存键盘按键及其按压时间的类
     */
    private class KeyTime
    {
        private int primaryCode;
        private long time;

        public KeyTime(int primaryCode, long time)
        {
            this.primaryCode = primaryCode;
            this.time = time;
        }

        public long getKeyTime()
        {
            return time;
        }

        public int getKeyValue()
        {
            return primaryCode;
        }
    }


    @Override
    public void onPress(int primaryCode) {

        Log.d("PGNkey", "onPress: 触发");

        //如果按压的不是功能键，将按压的时刻储存进数组
        if(primaryCode!=Keyboard.KEYCODE_DELETE && primaryCode!=Keyboard.KEYCODE_MODE_CHANGE
                && primaryCode!= Keyboard.KEYCODE_DONE && primaryCode!= Keyboard.KEYCODE_SHIFT)
        {
            long startTime = System.nanoTime();
            KeyTime key_press_time = new KeyTime(primaryCode, startTime);
            pressList.add(key_press_time);
            //查看按键的飞行时间
            Log.d("PANtimetest", "按键--"+showLabel(primaryCode)+"--按压之前的飞行时间为："+getKeyFlyTime());
        }
        //点击一个按键时，通过判断其是否在列表中来决定是否预览按键
        canShowPreview(primaryCode);
    }
    /**
     * 判断是否需要预览Key
     * @param primaryCode keyCode
     */
    private void canShowPreview(int primaryCode) {

        if (noLists.contains(primaryCode)) {
            keyboardView.setPreviewEnabled(false);
        } else {
            keyboardView.setPreviewEnabled(true);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        Log.d("PGNkey", "onRelease: 触发");
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        Log.d("PGNkey", "onKey:触发 ");

        //获取文本框中已输入的字符，并且以指针方式传递，即改变内容原内容也会改变
        Editable editable = getText();

        int start = getSelectionStart();

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE://删除
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start-1, start);
                }
                break;
            case Keyboard.KEYCODE_MODE_CHANGE://字母键盘与数字键盘切换
                changeKeyBoard(!changeLetter);
                Log.d("PGN键盘切换", "键盘切换: "+changeLetter);
                break;
            case Keyboard.KEYCODE_DONE://完成
                keyboardView.setVisibility(View.GONE);
                viewGroup.setVisibility(GONE);
                if (listener != null) {
                    listener.hide();
                }
                break;
            case Keyboard.KEYCODE_SHIFT://大小写切换
                changeCapital(!isCapital);
                keyboardView.setKeyboard(keyboardLetter);
                break;
            default:
                editable.insert(start, Character.toString((char)primaryCode));

                //当按压的不是功能键时，将按键释放时刻进行储存
                long endTime = System.nanoTime();
                KeyTime key_release_time = new KeyTime(primaryCode,endTime);
                releaseList.add(key_release_time);

                Log.d("PANtimetest", "按键--"+showLabel(keyCodes[0])+"--的按压时间为："+getKeyPressTime());

                randomKeyboardNumber();

                break;
        }

    }

    /**
     * 得到最后一次按键的按压时间
     */
    private long getKeyPressTime()
    {
        int pressListLength = pressList.size();
        int releaseListLength = releaseList.size();

        if(pressListLength!=releaseListLength)
        {
            Log.e("PANtimetest", "getKeyPressTime: 前后时间储存个数不一致，失败！");
            pressList.clear();
            releaseList.clear();
            return 0;
        }
        long release_time = releaseList.get(pressListLength-1).getKeyTime();
        long press_time = pressList.get(releaseListLength-1).getKeyTime();

        return release_time-press_time;
    }

    /**
     * 得到每一次按键中间的飞行时间
     */
    private long  getKeyFlyTime()
    {
        int pressListLength = pressList.size();
        int releaseListLength = releaseList.size();

        if (releaseListLength<=0)
        {
            Log.d("PANtimetest", "getKeyFlyTime: 时间储存列表为空,无飞行时间记录。");
            return 0;
        }
        else if((pressListLength-1)!=releaseListLength)
        {
            Log.e("PANtimetest", "getKeyFlyTime: 时间储存列表个数匹配失败，无法计算飞行时间！");
            pressList.clear();
            releaseList.clear();
            return 0;
        }

        //飞行时间用本次的按压时刻减去前一次的释放时刻
        long press_time = pressList.get(pressListLength-1).getKeyTime();
        long release_time = releaseList.get(releaseListLength-1).getKeyTime();

        return press_time-release_time;
    }

    /**
     * 显示键盘按键的Code显示按键的Label
     */
    @NonNull
    private String showLabel(int code)
    {
        //判断键盘类型，false表示数字键盘，true表示字母键盘
        if(false==changeLetter)
        {
            for(int i=0;i<numberKeyList.size();i++)
            {
                if(numberKeyList.get(i).codes[0]==code)
                    return numberKeyList.get(i).label.toString();
            }
        }
        //确定为字母键盘后，判断字母大小写，false表示小写字母，true表示大写字母
        else if(true==changeLetter && false==isCapital)
        {
            for(int i=0;i<letterSmallKeyList.size();i++)
            {
                if(letterSmallKeyList.get(i).codes[0]==code)
                    return letterSmallKeyList.get(i).label.toString();
            }
        }
        else if(true==changeLetter && true==isCapital)
        {
            for(int i=0;i<letterBigKeyList.size();i++)
            {
                if(letterBigKeyList.get(i).codes[0]==code)
                    return letterBigKeyList.get(i).label.toString();
            }
        }
        Log.e("PANtimetest", "showLabel: Code匹配出错!");
        return "Code匹配出错!";
    }

    /**打乱键盘顺序*/
    private void randomKeyboardNumber()
    {

        Keyboard keyboard;
        keyboard=keyboardView.getKeyboard();


        List<Keyboard.Key> keyList = keyboard.getKeys();

        List<Keyboard.Key> newkeyList = new ArrayList<Keyboard.Key>();


        //changeLetter为true表示此时为字母键盘，false表示为数字键盘

        if(false==changeLetter)
        {
            for (int i = 0; i < keyList.size(); i++)
            {
                Log.d("潘冠男", "按键的CODE值: "+keyList.get(i).codes[0]);
                // Log.d("潘冠男", "按键的显示值: "+keyList.get(i).label.toString());

                //根据label属性选择Key
                //if ((keyList.get(i).label != null && keyList.get(i).codes[0] >= 48 && keyList.get(i).codes[0] <= 57 ))
                if ((keyList.get(i).label != null && isNumber(keyList.get(i)) ))
                {
                    newkeyList.add(keyList.get(i));   //将按键储存到新的List中
                    Log.d("潘冠男", "code值："+keyList.get(i).codes[0]+"已经成功计入");
                }
            }
        }
        else
        //遍历所有布局文件中的Key属性
        {
            for (int i = 0; i < keyList.size(); i++)
            {
                //根据label属性选择Key
                //if ((keyList.get(i).label != null && isKey(keyList.get(i).label.toString())))
                /**
                 * 经过潘冠男精密的测试，源代码中的isKey函数在使用时存在bug,在判断字母时不能准确的的识别字母
                 * 并且会将非字母codes也判断为字母，导致之前的诸多测试均出现问题
                 * 现改用直接判断按键的codes值，目前可以正确运行
                 */
                if ((keyList.get(i).label != null && isLetter(keyList.get(i))))
                {
                    newkeyList.add(keyList.get(i));   //将按键储存到新的List中
                    Log.d("潘冠男", "字母键盘code值："+keyList.get(i).codes[0]+"已经成功计入");
                }
            }
        }

        for(Keyboard.Key key : newkeyList)
            Log.d("潘冠男", "字母键盘扫描后："+key.codes[0]+"");


        // 数组长度,记录键盘按键的总个数
        int count = newkeyList.size();

        for(Keyboard.Key key : newkeyList)
            Log.d("潘冠男", key.codes[0]+"");

        // 结果集
        List<KeyModel> resultList = new ArrayList<KeyModel>();

        // 用一个LinkedList作为中介
        LinkedList<KeyModel> temp = new LinkedList<KeyModel>();

        Log.d("潘冠男", "randomKeyboardNumber: "+count);

        // 初始化temp
        //将0~9存入其中
        if(keyboard==keyboardNumber){
            for (int i = 0; i < count; i++) {
                temp.add(new KeyModel(48 + i, i + ""));
            }
        }

        if(keyboard==keyboardLetter)
        {
            if(true==isCapital)
            {
                for (int i = 0; i < count; i++)
                    temp.add(new KeyModel(65 + i, (char)(65+i)+""));
            }
            else
                for (int i = 0; i < count; i++)
                temp.add(new KeyModel(97 + i, (char)(97+i)+""));
        }

        // 取数
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            //取0<=rand.nextInt(n)<n的随机数
            int num = rand.nextInt(count - i);
            resultList.add(new KeyModel(temp.get(num).getCode(),
                    temp.get(num).getLable()));
            temp.remove(num);
        }
        for (int i = 0; i < newkeyList.size(); i++) {
            newkeyList.get(i).label = resultList.get(i).getLable();
            newkeyList.get(i).codes[0] = resultList.get(i)
                    .getCode();
        }
        /*for (int i = 0; i < newkeyList.size(); i++) {
            newkeyList.get(i).label = "a";
            newkeyList.get(i).codes[0] =97;
        }*/

        //changeLetter为true表示此时为字母键盘，false表示为数字键盘
        if(changeLetter)
            keyboardView.setKeyboard(keyboardLetter);
        else
            keyboardView.setKeyboard(keyboardNumber);
    }

    private class KeyModel {

        private int code;   //code是布局文件中每个字符的ASCII码
        private String lable;    //布局文件中每个按键所代表的字符值

        public KeyModel(int code, String lable) {
            this.code = code;
            this.lable = lable;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getLable() {
            return lable;
        }

        public void setLable(String lable) {
            this.lable = lable;
        }
    }


    /**切换键盘大小写*/
    private void changeCapital(boolean b) {

        isCapital = b;
        List<Keyboard.Key> lists = keyboardLetter.getKeys();

        for (Keyboard.Key key: lists) {
            if (key.label != null && isKey(key.label.toString()))
            {
                if (isCapital)
                {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                else
                {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
            else if (key.label != null && key.label.toString().equals("小写"))
            {
                key.label = "大写";
            }
            else if (key.label != null && key.label.toString().equals("大写"))
            {
                key.label = "小写";
            }
        }
    }

    /** * 判断此key是否正确，且存在
     *  @param key
     * @return */
    private boolean isKey(String key) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        if (lowercase.indexOf(key.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }
    /**判断key是否为字母键*/
    private boolean isLetter(Keyboard.Key key) {
        if(key.codes[0]<0)
            return false;
        else if(key.codes[0] >= 65 && key.codes[0]<= 90)
        {
            return true;
        }
        else if(key.codes[0]>=97 && key.codes[0]<=122)
            return true;
        return false;
    }

    /**判断key是否为数字键*/
    private boolean isNumber(Keyboard.Key key) {
        if(key.codes[0]<0)
            return false;
        else if(key.codes[0] >= 48 && key.codes[0]<= 57)
        {
            return true;
        }
        return false;
    }

    /**切换键盘类型
     * @param b true表示显示字母键盘
     *          false表示显示数字键盘*/
    private void changeKeyBoard(boolean b) {
        //此处将键盘的状态进行翻转改变
        changeLetter = b;
        if (changeLetter) {
            keyboardView.setKeyboard(keyboardLetter);
        } else {
            keyboardView.setKeyboard(keyboardNumber);
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public interface OnKeyboardStateChangeListener {
        void show();
        void hide();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSystemSoftInput();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (keyboardView.getVisibility() != VISIBLE) {
                keyboardView.setVisibility(VISIBLE);
                viewGroup.setVisibility(VISIBLE);
                if (listener != null)
                    listener.show();
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //测试后不知道如何触发
        Log.d("PGNonKeyDown", "onKeyDown:触发 ");

        if (keyCode == KeyEvent.KEYCODE_BACK && (viewGroup.getVisibility() != GONE
        || keyboardView.getVisibility() != GONE)) {
            viewGroup.setVisibility(GONE);
            keyboardView.setVisibility(GONE);
            if (listener != null)
            listener.hide();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hideSystemSoftInput();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hideSystemSoftInput();
    }

    /**隐藏系统软键盘*/
    private void hideSystemSoftInput() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
