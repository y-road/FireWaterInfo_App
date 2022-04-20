package com.sesac.firewaterinfo.fragments

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FireWaterSPLiteOpenHelper
import com.sesac.firewaterinfo.common.HomeRecyclerAdapterEditHistory
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.PersonInfo
import com.sesac.firewaterinfo.common.data.SharedPrefManager
import com.sesac.firewaterinfo.databinding.FragmentHomeBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val PICK_FROM_GALLERY = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (LOG_ON_STATUS != null) {
            if (LOG_ON_STATUS!!.result) {
                if (MY_INFO == null) {
                    try {
                        getPerson(LOG_ON_STATUS!!.digital_code)
//                        getMyFW(LOG_ON_STATUS!!.digital_code)
                    } catch (e: Exception) {
                        Log.d(MY_DEBUG_TAG, "e= $e")
                        Toast.makeText(this@HomeFragment.context,
                            "정보 불러오기에 실패했습니다\n앱을 재실행 해보시고 안되시면 관리자에게 문의바랍니다",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        with(binding) {

            pictureIV.setOnClickListener {
                Toast.makeText(this@HomeFragment.context,
                    "변경할 프로필 사진을 선택해주세요\n프로필 사진은 남에게 표시되지 않아요",
                    Toast.LENGTH_SHORT).show()
                loadImage()
            }


            swipeContainer.setOnRefreshListener {

                if (LOG_ON_STATUS != null) {
                    if (LOG_ON_STATUS!!.result) {
                        try {
//                            getPerson(MY_INFO!!.digital_code)
//                            getMyFW(LOG_ON_STATUS!!.digital_code)
                            refreshEditedRecycler()

                        } catch (e: Exception) {
                            Toast.makeText(this@HomeFragment.context,
                                "정보 불러오기에 실패했습니다\n앱을 재실행 해보시고 안되시면 관리자에게 문의바랍니다",
                                Toast.LENGTH_SHORT).show()
                            Log.e(MY_DEBUG_TAG,"$e")
                        }
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    swipeContainer.isRefreshing = false
                }, 1100L)
            }
        }

        refreshEditedRecycler()
    }

    fun refreshEditedRecycler() {
        with(binding.homeRecycler) {

            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            val history = FireWaterSPLiteOpenHelper.getDBInstance(requireActivity()).selectAllEditedHistory()
            adapter = HomeRecyclerAdapterEditHistory(history, requireActivity())
        }
    }

    fun loadImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                val dataUri: Uri? = data?.data

                if (dataUri != null) {
                    Log.d(MY_DEBUG_TAG, "dataUri= $dataUri")
                    binding.pictureIV.setImageURI(dataUri)
//                    content://media/external/images/media/35355

//                    val file = File(dataUri.path)

                    val realPath: String = getRealPathFromURI(dataUri)
                    SharedPrefManager.saveMyProfilePath(realPath)

                    var bitmapImage: Bitmap = BitmapFactory.decodeFile(realPath)

                    var bitmapImage2: Bitmap = getResizedBitmap(bitmapImage, 500)
                    val path: String =
                        requireContext().cacheDir.toString() // "upload_${System.currentTimeMillis()}.jpg"
                    var dir = File(path)

                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file2 = File(dir, "upload_${System.currentTimeMillis()}.jpg")
                    var fOut = FileOutputStream(file2)
                    bitmapImage2.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.flush()
                    fOut.close()

                    if (LOG_ON_STATUS != null) {
                        changeMyProfilePicAsync(file2, LOG_ON_STATUS!!.digital_code.toString())
                    }

                }
            }
        }
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        var bitmapRatio: Float = width.toFloat() / height.toFloat()

        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getRealPathFromURI(uri: Uri): String {
        var path = ""

        if (requireActivity().contentResolver != null) {
            var cursor: Cursor? = requireActivity().contentResolver.query(uri, null, null, null, null)

            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    fun changeMyProfilePicAsync(
        uploadFile: File,
        digitalCode: String,
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
            .addFormDataPart("uploadFile", uploadFile.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), uploadFile))
            .build()

        // 요청 세팅
        val request: Request = Request.Builder()
            .url("http://59.9.249.206:8081/api/firewater/change/mypic")
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

                    if (returnCnt > 0) {
//                        Toast.makeText(FireApplication.getFireApplication(),
//                            "변경 완료",
//                            Toast.LENGTH_SHORT).show()
                        Log.d(MY_DEBUG_TAG, "변경 완료")
                    } else {
//                        Toast.makeText(FireApplication.getFireApplication(),
//                            "변경 실패 - 통신 상태가 좋지 않을 경우 변경되지 않을 수 있어요",
//                            Toast.LENGTH_SHORT).show()
                        Log.e(MY_DEBUG_TAG, "변경 실패 - 통신 상태가 좋지 않을 경우 변경되지 않을 수 있어요")
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(MY_DEBUG_TAG, "Error 발생!: $e")
            } finally {

                response?.close()
                uploadFile.delete()
            }
        }.start()

    }


//    private fun getMyFW(digCode: Long) {
//
//        with(CoroutineScope(Dispatchers.IO)) {
//            launch {
//                MYFWS.clear()
//                val rf = RestFunction()
//                rf.selectMyFW(digCode)
//            }
//        }
//        with(CoroutineScope(Dispatchers.Main)) {
//            launch {
//                var ctFlag = true
//                var tryCnt = 0
//
//                while (ctFlag) {
//                    if (++tryCnt > 6) ctFlag = false
//                    Log.d(MY_DEBUG_TAG, "getMyFW tryCnt= $tryCnt")
//
//                    if (MYFWS.size > 0) {
//                        with(binding.homeRecycler) {
//                            layoutManager =
//                                LinearLayoutManager(FireApplication.getFireApplication())
//                            val dividerItemDecoration = DividerItemDecoration(this.context,
//                                LinearLayoutManager(FireApplication.getFireApplication()).orientation)
////                            addItemDecoration(dividerItemDecoration)
//                            dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.divider_layer,
//                                null))
//                            addItemDecoration(dividerItemDecoration)
//                            adapter =
//                                HomeRecyclerAdapter(MYFWS, FireApplication.getFireApplication())
//                        }
//                        ctFlag = false
//                    }
//                    delay(200)
//                }
//            }
//        }
//    }

    private fun getPerson(digCode: Long) {

        MY_INFO = null

        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<PersonInfo> = firewaterService.selectMyInfo(digCode)

        call.enqueue(object : Callback<PersonInfo> {
            override fun onResponse(
                call: Call<PersonInfo>,
                response: Response<PersonInfo>,
            ) {
                if (response.isSuccessful) {
                    val myInfo = response.body() as PersonInfo

                    MY_INFO = myInfo

                    with(binding) {
//                        myBelong1TV.text = myInfo.belong_station
//                        myBelong2TV.text = myInfo.belong_center
                        digitalCodeTV.text = myInfo.digital_code.toString()
                        myNameTV.text = myInfo.name
//                        rankTV.text = myInfo.rank

//                        if (myInfo.img_name.isNotEmpty()) {
//                            val targetImage = "$PERSON_IMAGE_ADDRESS${myInfo.img_name}"
//                            Glide.with(pictureIV)
//                                .load(Uri.parse(targetImage))
//                                //.error(R.drawable.picture_back)
////                                .circleCrop()
//                                .into(pictureIV)
//                        }

                        if (SharedPrefManager.loadMyProfilePath().isNotEmpty()) {
                            val imgFile = File(SharedPrefManager.loadMyProfilePath())

                            if (imgFile.exists()) {
                                val bitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                                pictureIV.setImageBitmap(bitmap)
                            }
                        } else {
                            Toast.makeText(FireApplication.getFireApplication(),
                                "프리퍼런스 비었음.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PersonInfo>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "내 정보를 불러올 수 없음",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }
//    override fun onPause() {
//        super.onPause()
//        Log.d(MY_DEBUG_TAG,"onPause= $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Log.d(MY_DEBUG_TAG,"onStop= $this")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        Log.d(MY_DEBUG_TAG,"onDestroyView= $this")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Log.d(MY_DEBUG_TAG,"onDestroy= $this")
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        Log.d(MY_DEBUG_TAG,"onDetach= $this")
//    }


}