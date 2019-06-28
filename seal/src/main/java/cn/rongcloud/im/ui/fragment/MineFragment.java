package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrmf360.rylib.JrmfClient;

import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.VersionResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.AboutRongCloudActivity;
import cn.rongcloud.im.ui.activity.AccountSettingActivity;
import cn.rongcloud.im.ui.activity.MyAccountActivity;
import cn.rongcloud.im.ui.activity.ScanActivity;
import cn.rongcloud.im.ui.activity.SetLanguageActivity;
import cn.rongcloud.im.ui.activity.collection.CollectionActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongConfigurationManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.UserInfo;

import static android.app.Activity.RESULT_OK;

/**
 * Created by AMing on 16/6/21.
 * Company RongCloud
 */
public class MineFragment extends Fragment implements View.OnClickListener {
    private static final int COMPARE_VERSION = 54;
    public static final String SHOW_RED = "SHOW_RED";
    private SharedPreferences sp;
    private SelectableRoundedImageView imageView;
    private TextView mName, mCurrentLanguageTv;
    private ImageView mNewVersionView;
    private boolean isHasNewVersion;
    private String url;
    private boolean isDebug;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.seal_mine_fragment, container, false);
        isDebug = getContext().getSharedPreferences("config", getContext().MODE_PRIVATE).getBoolean("isDebug", false);
        initViews(mView);
        initData();
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUserInfo();
            }
        });
        compareVersion();
        return mView;
    }

    private void compareVersion() {
        AsyncTaskManager.getInstance(getActivity()).request(COMPARE_VERSION, new OnDataListener() {
            @Override
            public Object doInBackground(int requestCode, String parameter) throws HttpException {
                return new SealAction(getActivity()).getSealTalkVersion();
            }

            @Override
            public void onSuccess(int requestCode, Object result) {
                if (result != null) {
                    VersionResponse response = (VersionResponse) result;
                    String[] s = response.getAndroid().getVersion().split("\\.");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < s.length; i++) {
                        sb.append(s[i]);
                    }

                    String[] s2 = getVersionInfo()[1].split("\\.");
                    StringBuilder sb2 = new StringBuilder();
                    for (int i = 0; i < s2.length; i++) {
                        sb2.append(s2[i]);
                    }

                    int locVersion = Integer.parseInt(getVersionInfo()[0]);
                    String remoteVersionString = response.getIos().getBuild().substring(0, 10);
                    if (!TextUtils.isEmpty(remoteVersionString)) {
                        int remoteVersion = Integer.parseInt(remoteVersionString);
                        if (remoteVersion > locVersion) {
                            mNewVersionView.setVisibility(View.VISIBLE);
                            url = response.getAndroid().getUrl();
                            isHasNewVersion = true;
                            BroadcastManager.getInstance(getActivity()).sendBroadcast(SHOW_RED);
                        }
                    } else {
                        if (Integer.parseInt(sb.toString()) > Integer.parseInt(sb2.toString())) {
                            mNewVersionView.setVisibility(View.VISIBLE);
                            url = response.getAndroid().getUrl();
                            isHasNewVersion = true;
                            BroadcastManager.getInstance(getActivity()).sendBroadcast(SHOW_RED);
                        }
                    }
                }
            }

            @Override
            public void onFailure(int requestCode, int state, Object result) {

            }
        });
    }

    private void initData() {
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        updateUserInfo();
    }

    private void initViews(View mView) {
        mNewVersionView = (ImageView) mView.findViewById(R.id.new_version_icon);
        imageView = (SelectableRoundedImageView) mView.findViewById(R.id.mine_header);
        mName = (TextView) mView.findViewById(R.id.mine_name);
        LinearLayout mUserProfile = (LinearLayout) mView.findViewById(R.id.start_user_profile);
        LinearLayout mMineSetting = (LinearLayout) mView.findViewById(R.id.mine_setting);
        LinearLayout mMineLanguage = (LinearLayout) mView.findViewById(R.id.mine_language);
        LinearLayout mMineService = (LinearLayout) mView.findViewById(R.id.mine_service);
        LinearLayout mMineXN = (LinearLayout) mView.findViewById(R.id.mine_xiaoneng);
        LinearLayout mMineAbout = (LinearLayout) mView.findViewById(R.id.mine_about);
        LinearLayout mMineSao = (LinearLayout) mView.findViewById(R.id.mine_sao);
        LinearLayout minePc = (LinearLayout) mView.findViewById(R.id.mine_pc);
        LinearLayout mineCollect = (LinearLayout) mView.findViewById(R.id.mine_collect);
        mineCollect.setOnClickListener(this);
        minePc.setOnClickListener(this);
        mCurrentLanguageTv = (TextView) mView.findViewById(R.id.tv_mine_current_language);
        mCurrentLanguageTv.setText(getLanguageStr());

        if (isDebug) {
            mMineXN.setVisibility(View.VISIBLE);
        } else {
            mMineXN.setVisibility(View.GONE);
        }
        mUserProfile.setOnClickListener(this);
        mMineSetting.setOnClickListener(this);
        mMineLanguage.setOnClickListener(this);
        mMineService.setOnClickListener(this);
        mMineAbout.setOnClickListener(this);
        mMineXN.setOnClickListener(this);
        mMineSao.setOnClickListener(this);
        mView.findViewById(R.id.my_wallet).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_user_profile:
                startActivity(new Intent(getActivity(), MyAccountActivity.class));
                break;
            case R.id.mine_setting:
                startActivity(new Intent(getActivity(), AccountSettingActivity.class));
                break;
            case R.id.mine_language:
                startActivity(new Intent(getActivity(), SetLanguageActivity.class));
                break;
            case R.id.mine_service:
                CSCustomServiceInfo.Builder builder = new CSCustomServiceInfo.Builder();
                builder.province(getString(R.string.beijing));
                builder.city(getString(R.string.beijing));
                RongIM.getInstance().startCustomerServiceChat(getActivity(), "KEFU146001495753714", getString(R.string.online_custom_service), builder.build());
                // KEFU146001495753714 正式  KEFU145930951497220 测试  小能: zf_1000_1481459114694   zf_1000_1480591492399
                break;
            case R.id.mine_xiaoneng:
                CSCustomServiceInfo.Builder builder1 = new CSCustomServiceInfo.Builder();
                builder1.province(getString(R.string.beijing));
                builder1.city(getString(R.string.beijing));
                RongIM.getInstance().startCustomerServiceChat(getActivity(), "zf_1000_1481459114694", getString(R.string.online_custom_service), builder1.build());
                break;
            case R.id.mine_about:
                mNewVersionView.setVisibility(View.GONE);
                BroadcastManager.getInstance(getActivity()).sendBroadcast(SHOW_RED, "clear");
                Intent intent = new Intent(getActivity(), AboutRongCloudActivity.class);
                intent.putExtra("isHasNewVersion", isHasNewVersion);
                if (!TextUtils.isEmpty(url)) {
                    intent.putExtra("url", url);
                }
                startActivity(intent);
                break;
            case R.id.my_wallet:
                JrmfClient.intentWallet(getActivity());
                break;
            case R.id.mine_sao:
                ScanActivity.actionStartFragment(this);
                break;
            case R.id.mine_pc:
                NToast.shortToast(getActivity(), "敬请期待");
                break;
            case R.id.mine_collect:
                startActivity(new Intent(getActivity(), CollectionActivity.class));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ScanActivity.REQUEST_CODE) {
                if (data != null) {
                    String result = data.getStringExtra(ScanActivity.SCAN_RESULT);
                    NToast.shortToast(getActivity(), result);
                    Log.e("swo", result);
                }
            }
        }
    }

    private void updateUserInfo() {
        String userId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        String username = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String userPortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        mName.setText(username);
        if (!TextUtils.isEmpty(userId)) {
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri
                    (new UserInfo(userId, username, Uri.parse(userPortrait)));
            ImageLoader.getInstance().displayImage(portraitUri, imageView, App.getOptions());
        }
    }

    private String[] getVersionInfo() {
        String[] version = new String[2];

        PackageManager packageManager = getActivity().getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取当前语言字符串
     */
    private String getLanguageStr() {
        LangUtils.RCLocale currentLocale = RongConfigurationManager.getInstance().getAppLocale(getContext());
        Locale systemLocale = RongConfigurationManager.getInstance().getSystemLocale();
        if (currentLocale == LangUtils.RCLocale.LOCALE_CHINA) {
            return getString(R.string.lang_chs);
        } else if (currentLocale == LangUtils.RCLocale.LOCALE_US) {
            return getString(R.string.lang_english);
        } else {
            if (systemLocale.getLanguage().equals(Locale.CHINESE.getLanguage())) {
                return getString(R.string.lang_chs);
            } else if (systemLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                return getString(R.string.lang_english);
            } else {
                return getString(R.string.lang_chs);
            }
        }
    }
}
