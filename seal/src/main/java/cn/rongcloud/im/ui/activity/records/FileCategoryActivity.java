package cn.rongcloud.im.ui.activity.records;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.rong.imlib.model.Conversation;

/**
 * 图片/视频：   PicFragment
 * 文件   FileFragment
 */
public class FileCategoryActivity extends BaseActivity implements View.OnClickListener {

    private View mIndexPic;
    private View mIndexFile;
    private FrameLayout mFlContainer;
    private FragmentManager mFm;
    private FileFragment mFileFragment;
    private PicFragment mPicFragment;
    private int mType;// 0file，1pic
    private TextView mTvIndexPic;
    private TextView mTvIndexFile;
    private SealSearchConversationResult mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_category);
        initView();
        setTitle("聊天文件");
        initData();
    }

    private void initData() {
        mFileFragment = FileFragment.newInstance(mResult);
        mPicFragment = PicFragment.newInstance(mResult);
        mFm = getSupportFragmentManager();
        mFm.beginTransaction()
                .add(R.id.fl_container, mFileFragment)
                .add(R.id.fl_container, mPicFragment)
                .hide(mType == 1 ? mFileFragment : mPicFragment)
                .commit();
        onClick(mType == 1 ? mTvIndexPic : mTvIndexFile);
    }

    private void initView() {
        mIndexPic = (View) findViewById(R.id.index_pic);
        mTvIndexPic = ((TextView) findViewById(R.id.tv_index_pic));
        mTvIndexPic.setOnClickListener(this);
        mIndexFile = (View) findViewById(R.id.index_file);
        mTvIndexFile = ((TextView) findViewById(R.id.tv_index_file));
        mTvIndexFile.setOnClickListener(this);
        mFlContainer = (FrameLayout) findViewById(R.id.fl_container);
        mType = getIntent().getIntExtra("type", 0);
        mResult = getIntent().getParcelableExtra("searchConversationResult");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_index_pic:
                mIndexFile.setBackgroundColor(getResources().getColor(R.color.white));
                mIndexPic.setBackgroundColor(getResources().getColor(R.color.blue));
                mFm.beginTransaction()
                        .show(mPicFragment)
                        .hide(mFileFragment)
                        .commit();
                break;
            case R.id.tv_index_file:
                mIndexPic.setBackgroundColor(getResources().getColor(R.color.white));
                mIndexFile.setBackgroundColor(getResources().getColor(R.color.blue));
                mFm.beginTransaction()
                        .show(mFileFragment)
                        .hide(mPicFragment)
                        .commit();
                break;
        }
    }
}
