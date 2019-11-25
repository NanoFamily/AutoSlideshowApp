package jp.techacademy.ito.hiroki.autoslideshowapp

import android.Manifest
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import java.util.Timer
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*

private val PERMISSIONS_REQUEST_CODE = 100 //←これ必要なの？　8.3

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var cursor: Cursor? = null //メンバ変数(ﾟдﾟ)！
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )

            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
        go_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
        playpause_button.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    public fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        this.cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        cursor!!.moveToFirst()
        baseimage()
        /*if (cursor!!.moveToFirst()) {
            cursor = null
        }*/
    }

    //cursor!!.moveToFirst()
    /*do {*/
    // indexからIDを取得し、そのIDから画像のURIを取得する
    fun baseimage() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
    } //while (cursor.moveToNext())
    //cursor!!.close()

    override fun onClick(v: View) {
        if (v.id == R.id.go_button) {
            if (cursor!!.moveToNext() == true) {
                baseimage()
            } else {
                cursor!!.moveToFirst()
                baseimage()
            }
        }

        if (v.id == R.id.back_button) {
            if (cursor!!.moveToPrevious() == true) {
                baseimage()
            } else {
                cursor!!.moveToLast()
                baseimage()
            }
        }

        if (v.id == R.id.playpause_button) {
            if (mTimer == null) {
                mTimer = Timer()
                go_button.isEnabled = false
                back_button.isEnabled = false
                playpause_button.text = "停止"
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (cursor!!.moveToNext() == true) {
                                baseimage()
                            } else {
                                cursor!!.moveToFirst()
                                baseimage()
                            }
                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
            } else {
                mTimer!!.cancel()
                mTimer = null
                playpause_button.text = "再生"
                go_button.isEnabled = true
                back_button.isEnabled = true
            }
        }
    }
    //

    override fun onDestroy() {
        cursor!!.close()
    }
}

