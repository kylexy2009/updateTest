package com.sureserve.update;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.format.Formatter;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.FileCallback;
import com.lzy.okhttputils.callback.StringCallback;
import com.lzy.okhttputils.request.BaseRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


public class UpdateUtil {

    private static UpdateUtil mInstance;                 //单例
    private static Application application;

    private Context mContext;

    boolean mSilence; // 是否静默检查

    UpdateInfo _updateInfo;

    ProgressUtil mProgressDialog;
    ProgressUtil _dialogUpdate;
    View positiveAction;


    String _savePath = "";

    String version = "1.0.0.0";
    String appName = "";
    //String appDir = "sunhua";

    String mUpdateUrl = "http://erp.cxzyj.com/";


    /**
     * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
     */
    public static void init(Application app) {
        application = app;

        OkHttpUtils.init(app);
        //以下都不是必须的，根据需要自行选择
        OkHttpUtils.getInstance()//
                .debug("OkHttpUtils")                                              //是否打开调试
                .setConnectTimeout(OkHttpUtils.DEFAULT_MILLISECONDS)               //全局的连接超时时间
                .setReadTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS)                  //全局的读取超时时间
                .setWriteTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS);               //全局的写入超时时间


    }


    public static UpdateUtil getInstance() {
        if (mInstance == null) {
            synchronized (UpdateUtil.class) {
                if (mInstance == null) {
                    mInstance = new UpdateUtil();
                }
            }
        }
        return mInstance;
    }

    public UpdateUtil setUpdateUrl(String updateUrl) {
        this.mUpdateUrl = updateUrl;
        return this;
    }

    public UpdateUtil setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public UpdateUtil setAppDir(String appDir) {
        //this.appDir = appDir;
        return this;
    }

    public UpdateUtil setVersion(String version) {
        this.version = version;
        return this;
    }

    public UpdateUtil setContext(Context context) {
        this.mContext = context;
        return this;
    }


    public void destory() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }


    public void checkUpdate(boolean isSilence) {
        // 获取packagemanager的实例
        mSilence = isSilence;

        if (!mSilence) {
            if (mProgressDialog == null) createProgressDialog();
            mProgressDialog.show();
        }


        getUpdateInfo();
    }

    private void createProgressDialog() {
        //Context context = mContext;

        mProgressDialog = new ProgressUtil(mContext);

//        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
//                .title("版本升级")
//                .content("正在获取版本更新信息...")
//                .progress(true, 0)
//                .progressIndeterminateStyle(false);


        mProgressDialog.showSimpleDialog("版本升级", "正在获取版本更新信息...", "", "", null, null, false);
        mProgressDialog.show();
        //mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }


    private void getUpdateInfo() {


        int time = (int) (System.currentTimeMillis());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("appVision", version);
        params.put("appName", appName);
        params.put("timestamp", time + "");

        //String urls = new AppConfig().getVersionUrl();

        String urls = MakeURL(mUpdateUrl + "/opsmapp/download.do", params);


        OkHttpUtils.get(urls)//
                .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .connTimeOut(15000)
                .cacheKey("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode(CacheMode.NO_CACHE)    // 缓存模式，详细请看缓存介绍
                .execute(new StringCallback() {
                    @Override
                    public void onResponse(boolean isFromCache, String s, Request request, @Nullable Response response) {
                        returnStr(s);
                    }

                    @Override
                    public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
                        //super.onError(isFromCache, call, response, e);
                        UpdateFail("网络错误", e != null ? e.getMessage() : "");
                    }
                });

    }

    private void returnStr(String result) {
        try {

            Gson gson = new GsonBuilder().disableHtmlEscaping() // html不转义
                    .create();

            UpdateInfo data = gson.fromJson(result, new TypeToken<UpdateInfo>() {
            }.getType());

            if (data != null) {
                beginUpdateDialog(data);
            } else {
                UpdateFail("解析错误", result);
            }
        } catch (Exception e) {
            // 类型转换错误异常处理
            UpdateFail("解析错误", e.getMessage());
            return;
        }
    }


    private void UpdateFail(String message, String err) {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        }
        System.out.print("获取更新错误:" + err);

        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private void ToastMessage(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private void beginUpdateDialog(UpdateInfo data) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        _updateInfo = data;

        if (data.getState().equals("1")) {

            //if (_dialogUpdate == null) {
            initDialog(data);
            //}
            //_dialogUpdate.show();
        } else {
            if (!mSilence) {
                ToastMessage("当前版本已经是最新版本" + version + "了!");
            }
        }

    }

    private void initDialog(UpdateInfo data) {
        _dialogUpdate = new ProgressUtil(mContext);


        String title = "发现新版本  " + data.getAppVision();
        String message = data.getAppInfo();
        String negativeText = "取消";
        String positiveText = "升级";

        DialogInterface.OnClickListener positiveButton = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (positiveAction != null) positiveAction.setEnabled(false);

                _dialogUpdate.getAlterDialog().getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);

                update();
            }
        };


        DialogInterface.OnClickListener nagetiveButton = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelUpdate();
            }
        };


        _dialogUpdate.showProgressDialog(title, message, positiveText, negativeText, positiveButton, nagetiveButton, false);
        _dialogUpdate.show();


//        //if (_dialogUpdate !=null) {
//        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
//                .title()
//                .content(data.getAppInfo())
//                .positiveText("升级")
//                .negativeText("取消")
//                .autoDismiss(false)
//                .contentGravity(GravityEnum.START)
//                .progress(false, 100, true)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        if (positiveAction != null) positiveAction.setEnabled(false);
//                        update();
//                    }
//                })
//                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        cancelUpdate();
//                    }
//                });
//
//        _dialogUpdate = builder.build();
//        //_dialogUpdate.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        //}
//        positiveAction = _dialogUpdate.getActionButton(DialogAction.POSITIVE);
//        if (positiveAction != null) positiveAction.setEnabled(true);
//
//        _dialogUpdate.show();
    }


    private void update() {
        //修改链接地址为版本服务器地址和相对地址拼凑
        //String url =  new AppUrls().getVersionUrl() + _updateInfo.getAppPath();  //_updateInfo.getUrl();
        String url = _updateInfo.getUrl();
        String fileName = appName + "_" + _updateInfo.getAppVision() + ".apk";
        String dir = getDir();
        if (dir == null || dir.equals("")) {
            updateFail("没有SD卡,无法保存文件!");
            return;
        }

        if (!new File(dir).exists()) {
            new File(dir).mkdir();
        }

        _savePath = dir + fileName;
        File saveFile = new File(_savePath);
        if (saveFile.exists()) {
            saveFile.delete();
        }

        OkHttpUtils.get(url)//
                .tag(this)//
                .headers("header1", "headerValue1")//
                .params("param1", "paramValue1")//
                .execute(new DownloadFileCallBack(dir, fileName));

    }


    private class DownloadFileCallBack extends FileCallback {

        public DownloadFileCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName);
        }

        @Override
        public void onBefore(BaseRequest request) {
            //btnFileDownload.setText("正在下载中");
        }

        @Override
        public void onResponse(boolean isFromCache, File file, Request request, Response response) {
            //handleResponse(isFromCache, file, request, response);
            //btnFileDownload.setText("下载完成");
            System.out.println("file:" + file.getAbsolutePath());


            updateSucess();
        }

        @Override
        public void downloadProgress(long currentSize, long totalSize, float progress, long networkSpeed) {

            if (_dialogUpdate != null) {
                int pro = (int) (currentSize * 100 / totalSize);
                _dialogUpdate.show();
                _dialogUpdate.setProgress(pro);


                System.out.println(currentSize + "/" + totalSize + "  " + currentSize + " | " + pro);
            }
        }

        @Override
        public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
            super.onError(isFromCache, call, response, e);
            updateFail("取消下载!");
        }
    }

    private void cancelUpdate() {
        if (_dialogUpdate != null) _dialogUpdate.dismiss();
        //根据 Tag 取消请求
        OkHttpUtils.getInstance().cancelTag(this);
    }

    private void updateBegin() {
//        _progressLayout.setVisibility(View.VISIBLE);
//        _btUpdate.setText("取消下载");
//        _btUpdate.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                // TODO Auto-generated method stub
//                updateCancle();
//            }
//        });

    }

    private void updateProgress(long current, long total) {
//        _progressLayout.setVisibility(View.VISIBLE);
//        float progressF = (((float) current) / ((float) total)) * 100;
//        int progress = (int) progressF;
//
//        _tvProgress.setText(progress + "%");
//        _pbProgress.setMax(100);
//        _pbProgress.setProgress(progress);
    }

    private void updateSucess() {
        if (_dialogUpdate != null) _dialogUpdate.dismiss();
        File file = new File(_savePath);

        if (file.exists()) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//
//            mContext.startActivity(intent);

            installAPK(file);
            //installAPK(Uri.fromFile(file));
        }

    }

    private void updateFail(String failString) {
//        if (!StringUtils.isBlank(failString)) {
        ToastMessage(failString);
//            // UIHelper.ToastMessage(application, "打开失败,网络故障或者文件不存在!");
//        }
//        _dialogUpdate.dismiss();

        if (_dialogUpdate != null) _dialogUpdate.dismiss();
    }

    private void updateCancle() {
//        handler.cancel(true);
//        _dialogUpdate.dismiss();
    }

    private String getDir() {
        String dir = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File sd = Environment.getExternalStorageDirectory();
                dir = sd.getPath() + "/Download/";
                File file = new File(dir);
                if (!file.exists()) {
                    file.mkdir();
                }
            } catch (Exception e) {
                // UIHelper.ToastMessage(application, "创建文件失败!");
                dir = "";
            }
        } else {

            // UIHelper.ToastMessage(application,);
            dir = "";

        }

        return dir;

    }

    /**
     * 拼凑get方式的URL字符串
     */
    public static String MakeURL(String p_url, Map<String, Object> params) {
        StringBuilder url = new StringBuilder(p_url);
        if (url.indexOf("?") < 0)
            url.append('?');

        for (String name : params.keySet()) {
            url.append('&');
            url.append(name);
            url.append('=');
            url.append(String.valueOf(params.get(name)));
            // 不做URLEncoder处理
            // url.append(URLEncoder.encode(String.valueOf(params.get(name)),
            // UTF_8));
        }

        return url.toString().replace("?&", "?");
    }


    /**
     * 安装apk文件
     */
    private void installAPK(Uri apk) {

        // 通过Intent安装APK文件
        Intent intents = new Intent();

        intents.setAction("android.intent.action.VIEW");
        intents.addCategory("android.intent.category.DEFAULT");
        //intents.setType("application/vnd.android.package-archive");
        //intents.setData(apk);
        intents.setDataAndType(apk, "application/vnd.android.package-archive");
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //android.os.Process.killProcess(android.os.Process.myPid());
        // 如果不加上这句的话在apk安装完成之后点击单开会崩溃

        mContext.startActivity(intents);

    }


    private void installAPK(File apkFile) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DEFAULT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (apkFile.exists()) {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }


}
