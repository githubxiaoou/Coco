package cn.rongcloud.im.ui.activity.collection;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

/**
 * 收藏详情
 */
public class CollectionDetailActivity extends BaseActivity {
    SelectableRoundedImageView portraitImageView;
    TextView nameTextView;
    TextView chatRecordsDetailTextView;
    TextView chatRecordsDateTextView;
    private String mSenderUserId;
    private MessageContent mContent;
    private long mSentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);
        initView();
    }

    private void initView() {
        mSenderUserId = getIntent().getStringExtra("senderUserId");
        mContent = ((MessageContent) getIntent().getParcelableExtra("content"));
        mSentTime = getIntent().getLongExtra("sentTime", 0);
        portraitImageView = (SelectableRoundedImageView) findViewById(R.id.item_iv_record_image);
        nameTextView = (TextView) findViewById(R.id.item_tv_chat_name);
        chatRecordsDetailTextView = (TextView) findViewById(R.id.item_tv_chatting_records_detail);
        chatRecordsDateTextView = (TextView) findViewById(R.id.item_tv_chatting_records_date);

        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(mSenderUserId);
        if (userInfo != null) {
            ImageLoader.getInstance().displayImage(userInfo.getPortraitUri().toString(), portraitImageView, App.getOptions());
            nameTextView.setText(userInfo.getName());
        }
        chatRecordsDetailTextView.setText(((TextMessage) mContent).getContent());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String date = simpleDateFormat.format(new Date(mSentTime));
        String formatDate = date.replace("-", "/");
        setTitle(formatDate);
    }


}
