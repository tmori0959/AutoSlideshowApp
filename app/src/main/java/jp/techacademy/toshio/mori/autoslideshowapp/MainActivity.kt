package jp.techacademy.toshio.mori.autoslideshowapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.activity.ComponentActivity
import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Build
import android.widget.*
import androidx.core.content.ContextCompat
import java.lang.String.copyValueOf
import java.lang.String.format
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import java.lang.reflect.Array.getLong
import java.net.URI


class MainActivity : AppCompatActivity(),View.OnClickListener {     // View.OnClickListener追加

    // パーミッション追加
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合:パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                Log.d("ANDROID", "許可されている")
                getContentsInfo()
            } else {
                Log.d("ANDROID", "許可されていない")
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された")
                } else {
                    Log.d("ANDROID", "許可されなかった")
                }
        }

        // ボタンクリック待ち
        onoff_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        }
//        cursor!!.close()
    }

    private var cursor: Cursor? = null
    private var mHandler = Handler()
    private var mTimer: Timer? = null

    override fun onClick(v: View?) {
        // 進むボタン
        if (v?.id == R.id.next_button) {
            if (cursor!!.moveToNext()) {
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)    // 最初の画像を表示:moveToNext
            } else if (cursor!!.moveToFirst()) {
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)     // 次の画像を表示:moveToFirst
            }
            // 戻るボタン
        } else if (v?.id == R.id.back_button) {
            if (cursor!!.moveToPrevious()) {
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)    // 最後の画像を表示:moveToLast
            } else if(cursor!!.moveToLast()) {
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)      // 次の画像を表示:moveToNext
            }
            // 再生停止ボタン
        } else if (v?.id == R.id.onoff_button) {
            if (mTimer == null) {
                mTimer = Timer()        // タイマー作成
                // タイマー始動
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        cursor!!.moveToFirst()
                        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                        val id = cursor!!.getLong(fieldIndex)
                        val imageUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        imageView.setImageURI(imageUri)
/*                        mHandler.post {

                        }
*/                    }
                }, 2000, 2000) // 最初に始動させるまで2秒、ループの間隔を2秒 に設定
                    onoff_button.text = "停止"
                    next_button.isEnabled = false
                    back_button.isEnabled = false

            } else if(mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
                onoff_button.text = "再生"
                next_button.isEnabled = true
                back_button.isEnabled = true
            }
        }
    }
}