package com.example.erjinzzhi2;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    static {
        // 加载 native-lib 库
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 确定二进制文件的名称和存放路径
        String binaryFileName = "android";
        String destinationPath = getFilesDir().getAbsolutePath() + "/" + binaryFileName;

        // 检查文件是否已经存在且可执行
        File binaryFile = new File(destinationPath);
        if (!binaryFile.exists() || !binaryFile.canExecute()) {
            Log.d("BinaryExecution", "Binary does not exist or is not executable. Copying and setting permissions...");

            // 将二进制文件从 assets 复制到 files 目录
            if (copyBinaryToFilesDir(binaryFileName, destinationPath)) {
                Log.d("BinaryExecution", "Binary copied and permissions granted.");
            } else {
                Log.e("BinaryExecution", "Failed to copy binary or set execute permission.");
                return;  // 如果复制或设置权限失败，停止继续运行
            }
        } else {
            Log.d("BinaryExecution", "Binary already exists and is executable.");
        }

        // 运行二进制文件
        int result = runNativeBinary(destinationPath);
        Log.d("NativeExecution", "Native binary execution result: " + result);
    }

    // 声明 native 方法
    public native int runNativeBinary(String binaryPath);

    // 将二进制文件从 assets 复制到 files 目录
    private boolean copyBinaryToFilesDir(String binaryFileName, String destinationPath) {
        try (InputStream in = getAssets().open(binaryFileName);
             FileOutputStream out = new FileOutputStream(new File(destinationPath))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            // 通过 Runtime.exec() 授予执行权限
            Process chmod = Runtime.getRuntime().exec("chmod +x " + destinationPath);
            int chmodResult = chmod.waitFor();  // 等待命令执行完毕

            if (chmodResult == 0) {
                Log.d("BinaryExecution", "Binary copied and granted execute permission.");
                return true;
            } else {
                Log.e("BinaryExecution", "Failed to grant execute permission. chmod result: " + chmodResult);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            Log.e("BinaryExecution", "Error copying or granting permission to binary", e);
            return false;
        }
    }
}