package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.server.network.http.HttpException;

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class GroupManageActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        initViews();
        setTitle("群管理");

        SealAppContext.getInstance().pushActivity(this);
    }

    private void initViews() {

    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {}
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {

            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
        }
    }

    @Override
    public void onClick(View v) {

    }


}
