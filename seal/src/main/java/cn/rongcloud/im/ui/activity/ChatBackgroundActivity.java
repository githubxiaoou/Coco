package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.BackgroundModel;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static cn.rongcloud.im.server.BaseAction.DOMAIN_APP;

/**
 * 聊天背景
 */
public class ChatBackgroundActivity extends BaseActivity implements View.OnClickListener {
    public final String LOCAL_BG_DEFAULT = DOMAIN_APP + "/avatar/chatbg/local_background_default.png";
    public final String LOCAL_BG_ONE = DOMAIN_APP + "/avatar/chatbg/local_background_one.jpg";
    public final String LOCAL_BG_TWO = DOMAIN_APP + "/avatar/chatbg/local_background_two.jpg";
    public final String LOCAL_BG_THREE = DOMAIN_APP + "/avatar/chatbg/local_background_three.jpg";
    public final String LOCAL_BG_FOUR = DOMAIN_APP + "/avatar/chatbg/local_background_four.jpg";
    public final String LOCAL_BG_FIVE = DOMAIN_APP + "/avatar/chatbg/local_background_five.jpg";

    private String mChatId;
    private String mType;// 1:群组聊天 2:单人聊天 3:全局聊天
    private SharedPreferences sp;
    private String mUserId;
    private GridView mGvBg;
    private List<BackgroundModel> mSourceDataList = new ArrayList<>();
    private String[] bgs = new String[]{LOCAL_BG_DEFAULT, LOCAL_BG_ONE, LOCAL_BG_TWO, LOCAL_BG_THREE, LOCAL_BG_FOUR, LOCAL_BG_FIVE};
    private BgAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_backgroud);
        initView();
        initData();
    }

    private void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_default, false));
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_one, false));
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_two, false));
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_three, false));
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_four, false));
        mSourceDataList.add(new BackgroundModel(R.drawable.local_background_five, false));
        getBgImage();
    }

    private void initView() {
        mChatId = getIntent().getStringExtra("chatId");
        mType = getIntent().getStringExtra("type");
        setTitle("聊天背景");
        LinearLayout llSelect = (LinearLayout) findViewById(R.id.ll_select);
        llSelect.setOnClickListener(this);
        mGvBg = ((GridView) findViewById(R.id.gv_bg));
        mAdapter = new BgAdapter();
        mGvBg.setAdapter(mAdapter);
        mGvBg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChatBackgroundActivity.this, ChatBackgroundPreviewActivity.class);
                intent.putExtra("chatId", mChatId);
                intent.putExtra("type", mType);
                intent.putExtra("userId", mUserId);
                intent.putExtra("imgUrl", bgs[position]);
                startActivityForResult(intent, 100);
            }
        });
    }

    private void getBgImage() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .getChatBackgroundImage(mUserId, mChatId)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData>() {
                    @Override
                    public void Successful(NetData netData) {
                        if (!TextUtils.isEmpty(((String) netData.result))) {
                            for (int i = 0; i < bgs.length; i++) {
                                if (((String) netData.result).equals(bgs[i])) {
                                    mSourceDataList.get(i).isSelect = true;
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_select:
                break;
        }
    }

    class BgAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSourceDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSourceDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_background, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.rlBg.setBackgroundResource(mSourceDataList.get(position).resId);
            holder.ivChoice.setVisibility(mSourceDataList.get(position).isSelect ? View.VISIBLE : View.GONE);
            return convertView;
        }

        class ViewHolder {

            private final RelativeLayout rlBg;
            private final ImageView ivChoice;

            ViewHolder(View view) {
                rlBg = ((RelativeLayout) view.findViewById(R.id.ll_bg));
                ivChoice = ((ImageView) view.findViewById(R.id.iv_choice));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 100) {
                finish();
            }
        }
    }
}
