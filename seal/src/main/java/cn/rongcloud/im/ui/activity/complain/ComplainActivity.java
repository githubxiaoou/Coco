package cn.rongcloud.im.ui.activity.complain;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);
        initView();
    }

    private void initView() {
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
        switch (v.getId()) {
            default:
                break;
            case R.id.cl_one:
                break;
            case R.id.cl_two:
                break;
            case R.id.cl_three:
                break;
            case R.id.tv_notice:
                break;
        }
    }
}
