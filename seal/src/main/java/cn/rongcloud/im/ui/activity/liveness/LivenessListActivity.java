package cn.rongcloud.im.ui.activity.liveness;

import android.os.Bundle;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.JinyanResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class LivenessListActivity extends BaseActivity {
    private String type;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveness_list);
        initView();
        initData();
    }

    private void initData() {
        getLivenessList();
    }

    private void getLivenessList() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .groupInactiveMember(groupId, type)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<JinyanResponse>>>() {
                    @Override
                    public void Successful(NetData<List<JinyanResponse>> listNetData) {

                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void initView() {
        setTitle("不活跃群成员");
        type = getIntent().getStringExtra("type");
        groupId = getIntent().getStringExtra("GroupId");
    }

}
