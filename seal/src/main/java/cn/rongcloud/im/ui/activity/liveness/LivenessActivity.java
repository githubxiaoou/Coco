package cn.rongcloud.im.ui.activity.liveness;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.BaseActivity;

/**
 * 不活跃群成员
 */
public class LivenessActivity extends BaseActivity implements View.OnClickListener {

    private ConstraintLayout mClThree;
    private ConstraintLayout mClWeek;
    private ConstraintLayout mClMonth;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveness);
        initView();
    }

    private void initView() {
        setTitle("不活跃群成员");
        mClThree = (ConstraintLayout) findViewById(R.id.cl_three);
        mClThree.setOnClickListener(this);
        mClWeek = (ConstraintLayout) findViewById(R.id.cl_two);
        mClWeek.setOnClickListener(this);
        mClMonth = (ConstraintLayout) findViewById(R.id.cl_three);
        mClMonth.setOnClickListener(this);
        groupId = getIntent().getStringExtra("GroupId");
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            default:
                break;
            case R.id.cl_three:
                intent = new Intent(mContext, LivenessListActivity.class);
                intent.putExtra("type", "1");
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.cl_two:
                intent = new Intent(mContext, LivenessListActivity.class);
                intent.putExtra("type", "2");
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.cl_three:
                intent = new Intent(mContext, LivenessListActivity.class);
                intent.putExtra("type", "3");
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        finish();
    }
}
