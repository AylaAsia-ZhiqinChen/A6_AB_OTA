package com.system.ota;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件工具类
 */
public class FileUtils {
    final String TAG = "A6_OTA " + this.getClass().getSimpleName();

    public static String OTA_PATH;
    public static String OTA_LOCK_FILE_PATH;

    public Context mContext;
    private static FileUtils mInstance;

    public FileUtils(Context context) {
        mContext = context;
    }

    /**
     * 创建文件工具类示例
     *
     * @param context 上下文
     * @return
     */
    public static synchronized FileUtils createInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FileUtils(context);
            mInstance.initPath();
        }
        return mInstance;
    }

    /**
     * 获取文件工具类实例
     *
     * @return
     */
    public static synchronized FileUtils getInstance() {
        if (mInstance == null)
            throw new IllegalStateException("FileUtil must be create by call createInstance(Context context)");
        return mInstance;
    }

    /**
     * 初始化本地缓存路径
     */
    public void initPath() {
        OTA_PATH = "/sdcard/Documents/";
        OTA_LOCK_FILE_PATH = OTA_PATH + "ota_install.lock";

        Log.d(TAG, "OTA_PATH = " + OTA_PATH);
        Log.d(TAG, "OTA_LOCK_FILE_PATH = " + OTA_LOCK_FILE_PATH);
    }

    public String getAppPath() {
        Log.i(TAG, "AppPath = " + mContext.getFilesDir().getParent());
        return mContext.getFilesDir().getParent() + "/";
    }

    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }


    /**
     * [将文件保存到SDcard方法]<BR>
     * [功能详细描述]
     *
     * @param fileName
     * @param inStream
     * @throws IOException
     */
    public boolean saveFile2SDCard(String fileName, byte[] dataBytes) throws IOException {
        boolean flag = false;
        FileOutputStream fs = null;
        try {
            if (!TextUtils.isEmpty(fileName)) {
                File file = newFileWithPath(fileName.toString());
                if (file.exists()) {
                    file.delete();
                    Log.w(TAG, "httpFrame  threadName:" + Thread.currentThread().getName() + " 文件已存在 则先删除: "
                            + fileName.toString());
                }
                fs = new FileOutputStream(file);
                fs.write(dataBytes, 0, dataBytes.length);
                fs.flush();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fs != null)
                fs.close();
        }

        return flag;
    }

    /**
     * 创建一个文件，如果其所在目录不存在时，他的目录也会被跟着创建
     *
     * @return
     * @author
     * @date
     */
    public File newFileWithPath(String filePath) {
        Log.d(TAG, "filePath =" + filePath);
        if (TextUtils.isEmpty(filePath)) {
            Log.d(TAG, "retunr null!");
            return null;
        }

        int index = filePath.lastIndexOf(File.separator);
        Log.d(TAG, "index = " + index);

        String path = "";
        if (index != -1) {
            path = filePath.substring(0, index);
            Log.d(TAG, "path = " + filePath);
            if (!TextUtils.isEmpty(path)) {
                Log.d(TAG, "111111");
                File file = new File(path.toString());
                Log.d(TAG, "file = " + file);

                // 如果文件夹不存在
                if (!file.exists() && !file.isDirectory()) {
                    Log.d(TAG, "222222");
                    boolean flag = file.mkdirs();
                    if (flag) {
                        Log.i(TAG, "httpFrame  threadName:" + Thread.currentThread().getName() + " 创建文件夹成功："
                                + file.getPath());
                    } else {
                        Log.e(TAG, "httpFrame  threadName:" + Thread.currentThread().getName() + " 创建文件夹失败："
                                + file.getPath());
                    }
                }
            }
        }
        return new File(filePath);
    }

    /**
     * 判断文件是否存在
     *
     * @param strPath
     * @return
     */
    public boolean isExists(String strPath) {
        if (strPath == null) {
            return false;
        }

        final File strFile = new File(strPath);

        if (strFile.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 读写普通文件，例如txt
     *
     * @param context
     * @param fileName just file name, not include path
     * @param image
     * @param subDir   sub direction name, not absolute path
     */
    public void saveTxt2Public(Context context, String fileName, String content, String subDir) {
        String subDirection;
        if (!TextUtils.isEmpty(subDir)) {
            if (subDir.endsWith("/")) {
                subDirection = subDir.substring(0, subDir.length() - 1);
            } else {
                subDirection = subDir;
            }
        } else {
            subDirection = "Documents";
        }

//        Log.d(TAG, "subDirection = " + subDirection);

        Cursor cursor = searchTxtFromPublic(context, subDir, fileName);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                Uri uri = Uri.withAppendedPath(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), "" + id);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), id);
//                Log.d(TAG, "id = " + id);
//                Log.d(TAG, "uri = " + uri.toString());
//                Log.d(TAG, "contentUri = " + contentUri.toString());
                if (uri != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(content.getBytes());
                        outputStream.flush();
                        outputStream.close();
                    }
                }

                if (cursor != null) {
                    cursor.close();
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, subDirection);
            } else {

            }
            //设置文件类型
            contentValues.put(MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);
            Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues);
            if (uri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(content.getBytes());
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param filePath relative path in Q, such as: "DCIM/" or "DCIM/dir_name/"
     *                 absolute path before Q
     * @return
     */
    private Cursor searchTxtFromPublic(Context context, String filePath, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "searchTxtFromPublic: fileName is null");
            return null;
        }
        if (!filePath.endsWith("/")) {
            filePath = filePath + "/";
        }

        String queryPathKey = MediaStore.Files.FileColumns.RELATIVE_PATH;
        String selection = queryPathKey + "=? and " + MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                new String[]{MediaStore.Files.FileColumns._ID, queryPathKey, MediaStore.Files.FileColumns.DISPLAY_NAME},
                selection,
                new String[]{filePath, fileName},
                null);

        return cursor;
    }
}
