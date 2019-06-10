package cn.rongcloud.im.ui.activity.forward;

import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.BaseActivity;

/**
 * 转发功能
 * 选择一个聊天页面
 */
public class ForwardListActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_list);
        setTitle("选择一个聊天");
        initView();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        findViewById(R.id.ll_create_chat).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_create_chat:
                break;
        }
    }
}
