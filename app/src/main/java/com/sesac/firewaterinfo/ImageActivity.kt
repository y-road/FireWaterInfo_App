package com.sesac.firewaterinfo

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sesac.firewaterinfo.databinding.ActivityImageBinding

class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding

//    private var targetImagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        if (intent.hasExtra("path")) {
//        targetImagePath = intent.getStringExtra("path") ?: ""
//        }

        with(binding) {
            btnBackKey.setOnClickListener {
//                this@ImageActivity.onBackPressed()
                finish()
            }

            val arr = intent.getByteArrayExtra("image")

            if (arr != null) {
                val image = BitmapFactory.decodeByteArray(arr, 0, arr.count())
                singleImage.setImageBitmap(image)
            }


//            if (targetImagePath.isNotEmpty()) {
//                Glide.with(singleImage)
//                    .load(Uri.parse(targetImagePath))
//                    .error(R.drawable.ic_no_img)
//                    .transform(CenterCrop(), RoundedCorners(10))
//                    .into(singleImage)
//            } else {
//                Toast.makeText(this@ImageActivity, "이미지 로딩중 에러가 발생했어요", Toast.LENGTH_SHORT).show()
//                finish()
//            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(MY_DEBUG_TAG, "ImageActivity - onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(MY_DEBUG_TAG, "ImageActivity - onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MY_DEBUG_TAG, "ImageActivity - onDestroy")
    }

}