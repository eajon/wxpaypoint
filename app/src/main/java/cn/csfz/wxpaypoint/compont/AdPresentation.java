package cn.csfz.wxpaypoint.compont;

import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.PathUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.csfz.wxpaypoint.App;
import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.model.Video;
import cn.csfz.wxpaypoint.widget.FullScreenVideoView;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import cn.eajon.tool.SPUtils;
import cn.eajon.tool.StringUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class AdPresentation extends Presentation {

    @BindView(R.id.video_view)
    @Nullable
    public FullScreenVideoView videoView;

    @BindView(R.id.video_image)
    @Nullable
    public ImageView videoImage;

    public List<Video> videoPaths;
    private int index;

    public AdPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        setContentView(R.layout.dialog_ad);
        ButterKnife.bind(this);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoImage.setVisibility(View.VISIBLE);
                index++;
                if (index >= videoPaths.size()) {
                    index = 0;
                }
                startVideos();
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        videoImage.setVisibility(View.GONE);
                        videoView.setBackgroundColor(Color.TRANSPARENT);
                        return false;
                    }
                });
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                videoView.stopPlayback(); //播放异常，则停止播放，防止弹窗使界面阻塞
                return true;
            }
        });
    }

    public void updateVideos(VersionModel versionModel) {
        VersionModel version = SPUtils.getData("version", VersionModel.class);
        if (null != version) {
            if (version.getAdVersion() < versionModel.getAdVersion()) {
                SPUtils.putData("version", versionModel);
                this.videoPaths = versionModel.getVideos();
            } else {
                this.videoPaths = version.getVideos();
            }
        } else {
            SPUtils.putData("version", versionModel);
            this.videoPaths = versionModel.getVideos();
        }
        index = 0;
        startVideos();
    }


    /**
     * 播放视频
     */
    private void startVideos() {
        if (videoPaths.get(index).getType() == 0) {
            videoImage.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
            if (!StringUtils.isEmpty(videoPaths.get(index).getUrl())) {
                HttpProxyCacheServer proxy = App.getProxy(App.getContext());
                String proxyUrl = proxy.getProxyUrl(videoPaths.get(index).getUrl());
                try {
                    if (proxyUrl.toLowerCase().startsWith("file://")) {
                        videoImage.setImageBitmap(getVideoOne(App.getContext(), proxyUrl));
                    }
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                }
                videoView.setVideoPath(proxyUrl);
                videoView.start();
            } else {
                index++;
                if (index >= videoPaths.size()) {
                    index = 0;
                }
                startVideos();
            }

        } else {
            videoImage.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            Observable.timer(videoPaths.get(index).getDelay(), TimeUnit.SECONDS).compose(ObservableUtils.ioMain()).subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    Glide.with(AdPresentation.this.getContext()).load(videoPaths.get(index).getImage()).into(videoImage);
                }

                @Override
                public void onNext(Long aLong) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    index++;
                    if (index >= videoPaths.size()) {
                        index = 0;
                    }
                    startVideos();
                }
            });
        }
    }

    /**
     * 获取视频的第一帧
     *
     * @param sdPath
     * @return
     */
    private Bitmap getVideoOne(Context context, String sdPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        sdPath = sdPath.replace("file://", "");
        media.setDataSource(sdPath);// videoPath 本地视频的路径
        return media.getFrameAtTime();
    }

}
