//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import cn.rongcloud.im.R;
import cn.rongcloud.im.message.GifMessage;
import cn.rongcloud.im.ui.activity.GifPreviewActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.ImageMessage;
import io.rong.message.utils.BitmapUtil;

@ProviderTag(
    messageContent = GifMessage.class,
    showProgress = false,
    showReadState = true
)
public class GifMessageItemProvider extends MessageProvider<GifMessage> {
    private static final String TAG = "GifMessageItemProvider";

    public GifMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_image_message, (ViewGroup)null);
        GifMessageItemProvider.ViewHolder holder = new GifMessageItemProvider.ViewHolder();
        holder.message = (TextView)view.findViewById(id.rc_msg);
        holder.img = (AsyncImageView)view.findViewById(id.rc_img);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, GifMessage content, UIMessage message) {
        if (content != null) {
            Intent intent = new Intent(view.getContext(), GifPreviewActivity.class);
            intent.setPackage(view.getContext().getPackageName());
            intent.putExtra("message", message.getMessage());
            view.getContext().startActivity(intent);
        }

    }

    public void bindView(View v, int position, GifMessage content, UIMessage message) {
        GifMessageItemProvider.ViewHolder holder = (GifMessageItemProvider.ViewHolder)v.getTag();
        if (message.getMessageDirection() == MessageDirection.SEND) {
            v.setBackgroundResource(drawable.rc_ic_bubble_no_right);
        } else {
            v.setBackgroundResource(drawable.rc_ic_bubble_no_left);
        }

        if (content.isDestruct()) {
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(content.getThumUri().toString());
            if (bitmap != null) {
                Bitmap blurryBitmap = BitmapUtil.getBlurryBitmap(v.getContext(), bitmap, 5.0F, 0.25F);
//                holder.img.setBitmap(blurryBitmap);
                Glide.with(v).applyDefaultRequestOptions(new RequestOptions().override(300, 300)
                        .placeholder(R.drawable.loadfailure))
                        .asGif().load(blurryBitmap).into(holder.img);
            }
        } else {
//            holder.img.setResource(content.getThumUri());
            Glide.with(v).applyDefaultRequestOptions(new RequestOptions().override(300, 300)
                    .placeholder(R.drawable.loadfailure))
                    .asGif().load(content.getThumUri() == null ? content.getMediaUrl() : content.getThumUri()).into(holder.img);
        }

        int progress = message.getProgress();
        SentStatus status = message.getSentStatus();
        if (status.equals(SentStatus.SENDING) && progress < 100) {
            holder.message.setText(progress + "%");
            holder.message.setVisibility(View.VISIBLE);
        } else {
            holder.message.setVisibility(View.GONE);
        }

    }

    @Override
    public Spannable getContentSummary(GifMessage gifMessage) {
        return new SpannableString("[gif动态图]");
    }

    public Spannable getContentSummary(Context context, ImageMessage GifMessage) {
        return new SpannableString("[gif动态图]");
    }

    private static class ViewHolder {
        AsyncImageView img;
        TextView message;

        private ViewHolder() {
        }
    }
}
