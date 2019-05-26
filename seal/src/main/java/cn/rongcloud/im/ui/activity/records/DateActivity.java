package cn.rongcloud.im.ui.activity.records;

import android.os.Bundle;
import android.widget.CalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

public class DateActivity extends BaseActivity {

    private CalendarView mCalendarView;
    private SealSearchConversationResult mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        initView();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        setTitle("按日期查找");
        mResult = getIntent().getParcelableExtra("searchConversationResult");
        mCalendarView = ((CalendarView) findViewById(R.id.calendar));
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                long date = 0;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                            .parse(year + "-" + (month + 1) + "-" + dayOfMonth).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Conversation conversation = mResult.getConversation();
                RongIM.getInstance().startConversation(mContext, conversation.getConversationType(), conversation.getTargetId(), mResult.getTitle(), date);
            }
        });
    }
}
