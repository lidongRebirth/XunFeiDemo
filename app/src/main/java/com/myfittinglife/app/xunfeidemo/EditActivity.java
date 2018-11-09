package com.myfittinglife.app.xunfeidemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
/**
*  作者    LD
*  时间    2018.11.9  11:51
*  描述    对文本的编辑
*/
public class EditActivity extends AppCompatActivity {

    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.btn_send)        //发送
    Button btnSend;


    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        str = getIntent().getStringExtra("data");
        etContent.setText(str);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);      //让软键盘始终在控件的下方

    }

    @OnClick(R.id.btn_send)
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.btn_send:
                Intent intent = new Intent();
                intent.putExtra("data", etContent.getText().toString());
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }
}
