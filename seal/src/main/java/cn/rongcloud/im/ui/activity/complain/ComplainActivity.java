package cn.rongcloud.im.ui.activity.complain;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.BaseActivity;

/**
 * 投诉页面
 */
public class ComplainActivity extends BaseActivity implements View.OnClickListener {

    private ConstraintLayout mClOne;
    private ConstraintLayout mClTwo;
    private ConstraintLayout mClThree;
    private TextView mTvNotice;
    private String mChatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        initView();
    }

    private void initView() {
        setTitle("投诉");
        mChatId = getIntent().getStringExtra("chatId");
        mClOne = (ConstraintLayout) findViewById(R.id.cl_one);
        mClOne.setOnClickListener(this);
        mClTwo = (ConstraintLayout) findViewById(R.id.cl_two);
        mClTwo.setOnClickListener(this);
        mClThree = (ConstraintLayout) findViewById(R.id.cl_three);
        mClThree.setOnClickListener(this);
        mTvNotice = (TextView) findViewById(R.id.tv_notice);
        mTvNotice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String type;
        Intent intent = new Intent(this, ComplainConfirmActivity.class);
        intent.putExtra("chatId", mChatId);
        switch (v.getId()) {
            default:
                break;
            case R.id.cl_one:
                type = "1";
                intent.putExtra("type", type);
                startActivity(intent);
                break;
            case R.id.cl_two:
                type = "2";
                intent.putExtra("type", type);
                startActivity(intent);
                break;
            case R.id.cl_three:
                type = "3";
                intent.putExtra("type", type);
                startActivity(intent);
                break;
            case R.id.tv_notice:
                startActivity(new Intent(mContext, ComplainNoticeActivity.class));
                break;
        }
    }
}
