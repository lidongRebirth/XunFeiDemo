[APK](https://github.com/myfittinglife/MarkDown-Resource/blob/master/%E8%AE%AF%E9%A3%9E%E8%AF%AD%E9%9F%B3%E8%AF%86%E5%88%ABDemo.apk)

## 1、创建应用，下载SDK

登录讯飞开放平台-控制台创建应用-添加新服务-下载SDK-选择自己的应用以及想要的AI能力

![1541745162121](https://github.com/myfittinglife/MarkDown-Resource/blob/master/XunFeiDemo%E6%88%AA%E5%9B%BE1.png)

![1541745213277](https://github.com/myfittinglife/MarkDown-Resource/blob/master/XunFeiDemo%E6%88%AA%E5%9B%BE2.png)

##2、配置

将Msc.jar和Sunflower.jar放入libs文件夹下，在main文件夹下创建jniLibs文件夹，将剩余的arm64-v8a、armeabi、armeabi-v7a、mips、mips64、x86、x86_64放入其中；在main文件夹下创建assets文件夹，将下载的assets目录下的iflytek目录全部放入其中

## 3、使用

* **SDK初始化**

  ```
  SpeechUtility.createUtility(this, SpeechConstant.APPID + "=********");
  将*号改为你的APPID，别忘了等号！！
  ```

* **创建识别对象并监听**

  官方提供两种方式，一种是使用官方的Dialog语音识别框进行识别，一种是无UI的识别

  **方式一（官方有UI识别）：**

  1. 创建RecognizerDialog对象

     ```java
     RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
     ```
     MyInitListener()为初始化配置的监听

     ```java
     class MyInitListener implements InitListener {
         @Override
         public void onInit(int code) {
             if (code != ErrorCode.SUCCESS) {
                 showTip("初始化失败");
             }
         }
     }
     ```

  2. 设置accent(方言)、language等参数

     [更多设置](http://mscdoc.xfyun.cn/android/api/)

     ```java
     mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置中文
     mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//方言：普通话
     ```

  3. 设置回调接口，监听器

     ```java
     mDialog.setListener(new MyRecognizerDialogListener());
     ```
     MyRecognizerDialogListener()为语音识别的监听器

     ```java
     // 用HashMap存储听写结果使用官方dialog识别框
     private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
     class MyRecognizerDialogListener implements RecognizerDialogListener {
         /**
          * @param results
          * @param isLast  是否说完了
          */
         @Override
         public void onResult(RecognizerResult results, boolean isLast) {
             String result = results.getResultString(); //未解析的
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
     ```

  4. 显示dialog框

     ```
     mDialog.show();
     ```

  

  **方式二（无UI识别）：**

  1. 创建识别对象SpeechRecognizer

     ```java
     SpeechRecognizer mySpeechRecognizer = SpeechRecognizer.createRecognizer(this, new MyInitListener());		
     ```

  2. 设置识别的相关参数

     [更多参数](http://mscdoc.xfyun.cn/android/api/)

     ```java
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
     ```

  3. 开始识别并设置监听器

     ```java
     //开始识别，并设置监听器
     mySpeechRecognizer.startListening(mRecogListener);
     ```

  4. 监听器

     ```java
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
                     Log.i(TAG, "onTouch: 语音内容为空");
                 } else {                                         //不为空
                     Log.i(TAG, "onTouch: 语音内容不为空");
                 }
             }
             //返回结果 返回的结果可能为null，请增加判断处理。
             @Override
             public void onResult(RecognizerResult recognizerResult, boolean isLast) {       		//isLast代表是否是最后一句话，表示全部结束
                 Log.i(TAG, "onResult: " + recognizerResult.getResultString());
            //在这里而不在onEndOfSpeech()中赋值，是因为他会先执行onEndOfSpeech然后执行onResult,所以会导致标点符号赋值不上
                 tvPanel.append(jsonTrans(recognizerResult.getResultString()));
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
     ```
## 4、返回的Json字符串及解析

```java
/**
 * sn : 1
 * ls : false
 * bg : 0
 * ed : 0
 * ws : [{"bg":0,"cw":[{"sc":0,"w":"你好"}]}]
 */
```

直接通过GsonFormat插件进行转化为类即可

```java
public class JsonData {
    /**
     * sn : 1
     * ls : false
     * bg : 0
     * ed : 0
     * ws : [{"bg":0,"cw":[{"sc":0,"w":"你好"}]}]
     */

    private int sn;             //第几句
    private boolean ls;         //是否最后一句
    private int bg;             //开始
    private int ed;             //结束
    private List<WsBean> ws;    //词

    public int getSn() {
        return sn;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }

    public boolean isLs() {
        return ls;
    }

    public void setLs(boolean ls) {
        this.ls = ls;
    }

    public int getBg() {
        return bg;
    }

    public void setBg(int bg) {
        this.bg = bg;
    }

    public int getEd() {
        return ed;
    }

    public void setEd(int ed) {
        this.ed = ed;
    }

    public List<WsBean> getWs() {
        return ws;
    }

    public void setWs(List<WsBean> ws) {
        this.ws = ws;
    }

    public static class WsBean {
        /**
         * bg : 0
         * cw : [{"sc":0,"w":"你好"}]
         */

        private int bg;                 //开始
        private List<CwBean> cw;        //中文分词

        public int getBg() {
            return bg;
        }

        public void setBg(int bg) {
            this.bg = bg;
        }

        public List<CwBean> getCw() {
            return cw;
        }

        public void setCw(List<CwBean> cw) {
            this.cw = cw;
        }

        public static class CwBean {
            /**
             * sc : 0             //分数
             * w : 你好           //单字
             */

            private int sc;
            private String w;

            public int getSc() {
                return sc;
            }

            public void setSc(int sc) {
                this.sc = sc;
            }

            public String getW() {
                return w;
            }

            public void setW(String w) {
                this.w = w;
            }
        }
    }
}
```

**解析：**

```java
public String jsonTrans(String dataStr){
    JsonData jsonData = new Gson().fromJson(dataStr, JsonData.class);
    StringBuffer stringBuffer = new StringBuffer();
    for(int i = 0;i<jsonData.getWs().size();i++){
        for(int j = 0;j<jsonData.getWs().get(i).getCw().size();j++){
            stringBuffer.append(jsonData.getWs().get(i).getCw().get(j).getW());
        }
    }
    return stringBuffer.toString();
}
```

本Demo中使用了两种解析方法，第一种是有UI的，使用的是`JsonParser.parseIatResult(result)`JsonParser类中的parseIatResult()方法,另一种是没有UI的，使用Gson方式解析，调用jsonTrans()方法即可。

##5、注意事项

1、每个应用的SDK都不一样，都需要自己去下载，不要用别人的，否则会出现`SpeechRecognizer`为null的错误

2、APPID那里也要注意书写正确，别删掉那个等号，否则也会出现`SpeechRecognizer`为null的状况

3、要注意动态的申请`Manifest.permission.RECORD_AUDIO`权限，不然会出现20006的错误

4、`SpeechUtility.createUtility(this, SpeechConstant.APPID + "=********");`

​	SDK的初始化可以在活动中,也可以在Application中，但是要注意某些第三方的SDK会调用Application的onCreate()两次，也会导致为null的现象发生（SpeechSynthesizer.createSynthesizer(this, this)等于null）

具体参考[文章](https://blog.csdn.net/cly19940419/article/details/79269603)





