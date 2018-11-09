package com.myfittinglife.app.xunfeidemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
*  作者    LD
*  时间    2018.11.9
*  描述    讯飞语音识别（日后增添唤醒等操作）
*/
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_startspeech)         //官方UI语音转文字
    Button btn_startspeech;
    @BindView(R.id.et_input)              //文字内容
    TextView et_input;
    private static final String TAG = "MainActivity_ceshi";

    //*----
    @BindView(R.id.tv_panel)        //存放语音消息
    TextView tvPanel;
    @BindView(R.id.iv_voice)        //按住说话按钮
    ImageView ivVoice;
    @BindView(R.id.tv_clear)        //清空
    TextView tvClear;
    @BindView(R.id.tv_send)         //发送
    TextView tvSend;
    @BindView(R.id.ll_textpanel)     //清空发送面板隐藏
    LinearLayout llTextpanel;
    @BindView(R.id.tv_hint)         //按住说话文字
    TextView tvHint;
    @BindView(R.id.iv_canel)         //对勾取消
    ImageView ivCanel;
    @BindView(R.id.speechPanel)     //按住说话布局
    ConstraintLayout speechPanel;
    @BindView(R.id.tv_send_content)
    TextView tvSendContent;
    //*----

    // 用HashMap存储听写结果使用官方dialog识别框
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    //1. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        initSpeech();   //初始化操作       此处因为application中配置了，此处就不需要配置；额
    }


    //讯飞SDK初始化
    private void initSpeech() {
        // 将“12345678”替换成您申请的 APPID，申请地址： http://www.xfyun.cn
        // 请勿在 “ =”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5bdf992a");
    }

    @OnClick({R.id.btn_startspeech,R.id.tv_clear, R.id.tv_send,R.id.tv_panel,R.id.iv_voice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_startspeech:      //官方UI语音转文字
                if(xunFeiPermissionCheck()){        //需要有录音权限
                    startSpeechDialog();
                }
                break;
            case R.id.tv_clear:         //清空面板文字
                tvPanel.setText("");
                ivCanel.setVisibility(View.VISIBLE);
                llTextpanel.setVisibility(View.GONE);
                tvPanel.setHint("");
                break;
            case R.id.tv_send:          //发送
                showTip("发送");
                tvSendContent.append("\n"+tvPanel.getText());
                tvPanel.setText("");
                ivCanel.setVisibility(View.VISIBLE);
                llTextpanel.setVisibility(View.GONE);
                break;
            case R.id.tv_panel:     //点击文字，进入编辑界面
                if(!isEmpty(tvPanel.getText().toString())){
                    Intent intent = new Intent(this,EditActivity.class);
                    intent.putExtra("data",tvPanel.getText().toString());
                    startActivityForResult(intent,1);
                }
                break;
            case R.id.iv_voice:
                if(xunFeiPermissionCheck()){
                    voiceTouch();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    tvSendContent.append("\n"+data.getStringExtra("data"));
                }
                break;
        }

    }

    //官方文档：https://doc.xfyun.cn/msc_android/%E8%AF%AD%E9%9F%B3%E5%90%AC%E5%86%99.html
    //方式一：使用官方Dialog样式的语音监听
    private void startSpeechDialog() {
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//方言：普通话
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");    识别语义/翻译参数设置 通过此参数，设置识别接口进行语音语义或者翻译操作，并返回相应结果 是否必须设置：否 默认值：0 值范围：{ null, 0, 1 }
        // mDialog.setParameter("nlp_version", "2.0");  语义版本 通过此参数，设置开放语义协议版本号。
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4. 显示dialog，接收语音输入
        mDialog.show();
    }

    //官方Dialog样式的监听器
    class MyRecognizerDialogListener implements RecognizerDialogListener {
        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //未解析的
            showTip(result);
            Log.i(TAG, " 没有解析的 :" + result);

            String text = JsonParser.parseIatResult(result);//解析过后的
            Log.i(TAG, " 解析后的 :" + text);


            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(result);
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);//没有得到一句，添加到

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            et_input.setText(resultBuffer.toString());// 设置输入框的文本
        }

        @Override
        public void onError(SpeechError speechError) {
            if (speechError.getErrorCode() == 20006) {
                showTip("请在系统中开启录音权限");
            }else if(speechError.getErrorCode()==10118){
                showTip("您好像没有说话哦");
            }else if(speechError.getErrorCode()==20001){
                showTip("请开启网络权限");
            }
            else {
                showTip("发生错误");
            }
        }
    }

    //初始化配置的监听
    class MyInitListener implements InitListener {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败");
            }
        }
    }

    //toast操作
    public void showTip(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    //------------------------------------------------------------------------------------------------------
    //方式二：自定义样式的监听
    //监听语音
    public void startSpeech() {
        //初始化识别无UI识别对象
        //使用SpeechRecognizer对象，可根据回调消息自定义界面；
        //1. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener
        SpeechRecognizer mySpeechRecognizer = SpeechRecognizer.createRecognizer(this, new MyInitListener());
        mySpeechRecognizer.setParameter(SpeechConstant.DOMAIN, "iat");// 短信和日常用语： iat (默认)
        mySpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置中文
        mySpeechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");// 设置普通话

        //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mySpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”

        //2. 设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
        mySpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
        //设置语音输入语言，zh_cn为简体中文
        mySpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mySpeechRecognizer.setParameter(SpeechConstant.ACCENT, "zh_cn");

        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mySpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");

        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mySpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, "1000");

        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mySpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, "1");
        //开始识别，并设置监听器
        mySpeechRecognizer.startListening(mRecogListener);
    }

    // 听写监听器        //用的cloud而不是speech
    private RecognizerListener mRecogListener = new RecognizerListener() {

        //音量变化 当开始识别，到停止录音（停止写入音频流）或SDK返回最后一个结果自动结束识别为止， SDK检测到音频数据（正在录音或写入音频流）的音量变化时，会多次通过此函数回调，告知应用层当前的音量值
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {      //音量变化

        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始录音了");
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束录音了");
            Log.i(TAG, "onEndOfSpeech: " + "结束录音");
            if (isEmpty(tvPanel.getText().toString())) {      //空
//                            ivCanel.setVisibility(View.VISIBLE);
                Log.i(TAG, "onTouch: 语音内容为空");
                ivCanel.setVisibility(View.VISIBLE);

            } else {                                         //不为空
                Log.i(TAG, "onTouch: 语音内容不为空");
                llTextpanel.setVisibility(View.VISIBLE);
                ivCanel.setVisibility(View.GONE);
            }

        }

        //返回结果 返回的结果可能为null，请增加判断处理。
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {       //isLast代表是否是最后一句话，表示全部结束
            Log.i(TAG, "onResult: " + recognizerResult.getResultString());
            //在这里而不在onEndOfSpeech()中赋值，是因为他会先执行onEndOfSpeech然后执行onResult,所以会导致标点符号赋值不上
            tvPanel.append(JsonParser.parseIatResult(recognizerResult.getResultString()));

        }

        @Override
        public void onError(SpeechError speechError) {
            if (speechError.getErrorCode() == 20006) {
                showTip("请在系统中开启录音权限");
            }else if(speechError.getErrorCode()==10118){
                showTip("您好像没有说话哦");
            }else if(speechError.getErrorCode()==20001){
                showTip("请开启网络权限");
            } else {
                showTip("发生错误"+speechError);
            }
        }

        /**
         * 扩展用接口，由具体业务进行约定。例如eventType为0显示网络状态，agr1为网络连接值。
         * @param eventType     消息类型
         * @param arg1          参数一
         * @param arg2          参数二
         * @param obj           消息内容
         */
        @Override
        public void onEvent(int eventType, int arg1 , int arg2 , Bundle obj ) {

        }
    };

    //底部话筒的触摸事件
    private void voiceTouch() {
        ivVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tvPanel.setHint("请说话...");
                        Log.i(TAG, "onTouch: 请说话");
                        startSpeech();      //识别语音
                        tvHint.setVisibility(View.GONE);            //按住说话提示音
                        ivVoice.setPressed(true);

                        break;
                    case MotionEvent.ACTION_UP:
                        tvHint.setVisibility(View.VISIBLE);
                        Log.i(TAG, "onTouch: 结束说话");
                        tvPanel.setHint("");
                        ivVoice.setPressed(false);
                        break;
                }
                return true;//不返回true则该行为没有反应
            }
        });
    }


    //为空判断
    public boolean isEmpty(String input) {
        if (input == null || "".equals(input) || "null".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    //权限申请
    public boolean xunFeiPermissionCheck() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            Log.i("xunfeiceshi", "xunFeiPermissionCheck: 没录音权限");
            return false;
        } else {
            Log.i("xunfeiceshi", "xunFeiPermissionCheck: 有录音权限");
            return true;
        }
    }



}
