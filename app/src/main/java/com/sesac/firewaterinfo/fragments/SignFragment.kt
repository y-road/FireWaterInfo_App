package com.sesac.firewaterinfo.fragments

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FuncModule
import com.sesac.firewaterinfo.databinding.FragmentSigninBinding
import com.sesac.firewaterinfo.dialogs.CustomDialog2
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.concurrent.TimeUnit

class SignFragment : Fragment() {

    companion object {
        fun newInstance(): SignFragment {
            return SignFragment()
        }
    }

    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    var signImgPath: String = ""

    private lateinit var anim: Animation

    val mainHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            val returnCnt = msg.data.getInt("cntt")

            if (returnCnt > 0) {
                CustomDialog2(requireActivity(),
                    this@SignFragment,
                    "회원 가입 신청이 완료되었습니다.\n관리자가 5분 이내로 확인합니다.").apply {
                    setCanceledOnTouchOutside(false)
                }.show()
            } else {
                Toast.makeText(this@SignFragment.context,
                    "가입 신청에 실패하였습니다\n잠시후 다시 시도해주십시오",
                    Toast.LENGTH_SHORT)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        Log.d(MY_DEBUG_TAG, "SignFragment - onCreateView")

        (activity as MainActivity).viewBottomLayout(false)

        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(MY_DEBUG_TAG, "SignFragment - onViewCreated")

        anim = AlphaAnimation(0.0f, 1.0f)
        anim.apply {
            duration = 30
            startOffset = 20
            repeatCount = 1
        }

        with(binding) {
            btnTakePic6.setOnClickListener {

                val tpi = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                tpi.resolveActivity(requireActivity().packageManager)?.also {
                    startActivityForResult(tpi, 200)
                }
            }
            btnBackKey.setOnClickListener {

                with((activity as MainActivity)) {
                    onBackPressed()
                    viewBottomLayout(true)
                }

            }
            passwordET2.addTextChangedListener {
                Log.d(MY_DEBUG_TAG,
                    "passwordET1: ${passwordET1.text} , passwordET2: ${passwordET2.text}")
                if (passwordET1.text.toString() == passwordET2.text.toString()) {
                    Log.d(MY_DEBUG_TAG, "passwordET1.text == passwordET2.text")

                    passwordTV4.visibility = View.GONE
                } else {
                    Log.d(MY_DEBUG_TAG, "passwordET1.text != passwordET2.text")

                    passwordTV4.visibility = View.VISIBLE
                    passwordTV4.startAnimation(anim)
                }
            }

            btnSend7.setOnClickListener {

                if (allIsNullCheck()) {
                    val f = File(signImgPath)
//                    /data/user/0/com.sesac.firewaterinfo/cache/upload_1647432146244.jpg

                    with(binding) {
                        val fc = FuncModule()
                        val pw256: String = fc.getSign(passwordET1.text.toString())

                        val name: String = nameET1.text.toString()
                        val rank: String = spinnerRank.selectedItem.toString()
                        val digitalCode: String = digitalCodeET2.text.toString()
                        val password: String = pw256
                        val phone: String = phoneET3.text.toString()
                        val belongStation: String = belongET35.text.toString()
                        val belongCenter: String = belongET4.text.toString()

                        binding.progressbarSign.visibility = View.VISIBLE

                        fileUploadAsync(f,
                            name.trim(),
                            rank.trim(),
                            digitalCode.trim(),
                            password.trim(),
                            phone.trim(),
                            belongStation.trim(),
                            belongCenter.trim())
                        binding.progressbarSign.visibility = View.GONE

                    }
                }
            }

            mainNestedSV.setOnClickListener {
                (activity as MainActivity).hideKeyboard(true)
            }
        }
    }

    fun fileUploadAsync(
        uploadFile: File,
        name: String,
        rank: String,
        digitalCode: String,
        password: String,
        phone: String,
        belongStation: String,
        belongCenter: String,
    ) {

        var response: okhttp3.Response? = null

        val toServer: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val fileUploadBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM) // 파일 업로드시 반드시 설정
            .addFormDataPart("digitalCode", digitalCode)
            .addFormDataPart("name", name)
            .addFormDataPart("rank", rank)
            .addFormDataPart("belongStation", belongStation)
            .addFormDataPart("belongCenter", belongCenter)
            .addFormDataPart("phone", phone)
            .addFormDataPart("password", password)
            .addFormDataPart("uploadFile", uploadFile.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), uploadFile))
            .build()

        // 요청 세팅
        val request: Request = Request.Builder()
            .url("http://59.9.249.206:8081/api/firewater/sign")
            .post(fileUploadBody)
            .build()

        Thread {
            try {
                response = toServer.newCall(request).execute()
                if (response!!.isSuccessful) {
                    var responseJSON = ""
                    responseJSON = response!!.body!!.string()

                    val jsonObject = JSONObject(responseJSON)
                    val returnCnt = jsonObject.optInt("cnt")

                    with(mainHandler.obtainMessage()) { //메세지 객체를 얻음.
                        val bundle = Bundle()
                        bundle.putInt("cntt", returnCnt)
                        data = bundle
                        mainHandler.sendMessage(this)
                    }
                }
            } catch (e: Exception) {
                Log.e(MY_DEBUG_TAG, "Error 발생!: $e")
            } finally {

                response?.close()
            }
        }.start()
    }

    private fun allIsNullCheck(): Boolean {

        var flag = false

        with(binding) {
            when {
                nameET1.text.isEmpty() -> {
                    nameET1.requestFocus()
                    Toast.makeText(this@SignFragment.context, "이름을 적어주세요", Toast.LENGTH_SHORT)
                        .show()
                }
                digitalCodeET2.text.isEmpty() -> {
                    digitalCodeET2.requestFocus()
                    Toast.makeText(this@SignFragment.context, "디지털 코드를 적어주세요", Toast.LENGTH_SHORT)
                        .show()
                }
                phoneET3.text.isEmpty() -> {
                    phoneET3.requestFocus()
                    Toast.makeText(this@SignFragment.context, "전화번호를 적어주세요", Toast.LENGTH_SHORT)
                        .show()
                }
                belongET35.text.isEmpty() -> {
                    belongET35.requestFocus()
                    Toast.makeText(this@SignFragment.context, "소속 소방서를 적어주세요", Toast.LENGTH_SHORT)
                        .show()
                }
                belongET4.text.isEmpty() -> {
                    belongET4.requestFocus()
                    Toast.makeText(this@SignFragment.context, "소속 센터를 적어주세요", Toast.LENGTH_SHORT)
                        .show()
                }
                IV5.drawable == null -> {
                    Toast.makeText(this@SignFragment.context,
                        "공무원증 뒷면 사진을 찍어주세요\n이미지 정보는 인증 이후 곧바로 폐기됩니다",
                        Toast.LENGTH_SHORT)
                        .show()
                }
                passwordET1.text.toString() != passwordET2.text.toString() -> {
                    passwordET1.requestFocus()
                    Toast.makeText(this@SignFragment.context, "패스워드가 다릅니다", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    flag = true
                }
            }
        }
        return flag
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                200 -> {
                    val imgBitmap = data?.extras?.get("data") as Bitmap
//                    saveBitmapAsJPGFile(imgBitmap)
                    val path: String =
                        requireContext().cacheDir.toString() // "upload_${System.currentTimeMillis()}.jpg"
                    var dir = File(path)

                    if (!dir.exists()) {
                        dir.mkdirs()
                    }

                    val file = File(dir, "upload_${System.currentTimeMillis()}.jpg")
                    signImgPath = file.path
                    var fOut = FileOutputStream(file)
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.flush()
                    fOut.close()

                    with(binding) {
                        IV5.setImageBitmap(imgBitmap)
                        btnTakePic6.setBackgroundResource(R.drawable.take_pic_bg_after)
                        btnTakePic6.setTextColor(Color.parseColor("#FFEB5E28"))
                        btnSend7.isEnabled = true
                        btnSend7.setBackgroundResource(R.drawable.login_bg_enabled)
                    }
                }
            }
        }
    }



}