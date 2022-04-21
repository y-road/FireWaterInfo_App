package com.sesac.firewaterinfo.dialogs

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FireWaterSPLiteOpenHelper
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.CntResult
import com.sesac.firewaterinfo.common.data.SharedPrefManager
import com.sesac.firewaterinfo.common.data.TABLE_EDIT_HISTORY
import com.sesac.firewaterinfo.databinding.BottomSimpleInfoBinding
import com.sesac.firewaterinfo.fragments.MapFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt

class BottomSheetDialogMy(val alfw: AllFW) : BottomSheetDialogFragment() {

    private var _binding: BottomSimpleInfoBinding? = null
    private val binding get() = _binding!!

    private val PICK_FROM_GALLERY = 100
    private var selectedNumber: Int = 0

    private var isOpenableImage = false

    fun newInstance(detail: AllFW): BottomSheetDialogMy {

        val fragment = BottomSheetDialogMy(detail)
        val bundle = Bundle()
        bundle.apply {
            putInt("fwNum", detail.number)
            putString("fwName", detail.name)
            putString("fwType", detail.type)
//            putString("fwProvince", detail.province)
//            putString("fwCity", detail.city)
            putString("fwAddress", detail.address)
            putDouble("fwLati", detail.latitude)
            putDouble("fwLong", detail.longitude)
            putString("fwDetail", detail.detail)
            putString("fwCenter", detail.center)
            putBoolean("fwProtectCase", detail.protect_case)
            putBoolean("fwAvailable", detail.available)
            putInt("fwInstallationYear", detail.installation_year)
            putDouble("fwPipeDepth", detail.pipe_depth)
            putDouble("fwOutPressure", detail.out_pressure)
            putDouble("fwPipeDiameter", detail.pipe_diameter)
//            putString("fwFireStation", detail.fire_station)
//            putString("fwPhone", detail.phone)
            putString("fwUpdateDate", detail.update_date)
            putBoolean("is_exist", detail.is_exist)
            putString("fwImgName", detail.img_name)
        }
        fragment.arguments = bundle
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = BottomSimpleInfoBinding.inflate(inflater, container, false)

        with(binding) {

            val imgName: String? = arguments?.getString("fwImgName")

            if (imgName != null && imgName.isNotEmpty()) {
                val targetImage = "${FIREWATER_IMAGE_ADDRESS}${imgName}"
                Log.d(MY_DEBUG_TAG, "Uri.parse(targetImage)= ${Uri.parse(targetImage)}")
                Glide.with(previewIV)
                    .load(Uri.parse(targetImage))
//                    .error(R.drawable.ic_no_img)
                    .transform(CenterCrop(), RoundedCorners(10))
                    .into(previewIV)

                isOpenableImage = true
            }


            val isAvailable: Boolean = arguments?.getBoolean("fwAvailable") ?: false

            if (!isAvailable) {
                previewTV1.text = "사용불가"
                previewTV1.setBackgroundResource(R.drawable.death_background)
            }

            previewTV2.text = arguments?.getString("fwName") ?: "-"

            val lat: Double = arguments?.getDouble("fwLati") ?: 0.0
            val lon: Double = arguments?.getDouble("fwLong") ?: 0.0
            var distance = getDistance(MY_LATITUDE, MY_LONGITUDE, lat, lon)

            var distanceKM: Double = distance / 1000.0
            distanceKM = round(distanceKM * 10) / 10.0

            previewTV3.text = if (distance > 1000) {
                "${distanceKM}km"
            } else {
                "${distance}m"
            }

            previewTV4.text = arguments?.getString("fwAddress") ?: "-"
            previewTV5.text = arguments?.getString("fwDetail") ?: "-"

            previewTV35.setOnClickListener {

                if (LOG_ON_STATUS != null) {
//                    Toast.makeText(this@BottomSheetDialogMy.context,
//                        "변경할 소화전 사진을 선택해주세요",
//                        Toast.LENGTH_SHORT).show()

                    loadImage()
                } else {
                    Toast.makeText(this@BottomSheetDialogMy.context,
                        "회원만 사진을 변경할 수 있습니다.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            previewBtn7.setOnClickListener {
                val botd = BottomSheetDialogDetail(alfw)
                botd.show(parentFragmentManager, botd.tag)
                dismiss()
            }

            previewIV.setOnClickListener {

                if (isOpenableImage) {
                    val intent = Intent(requireActivity(), ImageActivity::class.java)

                    val bitmap: Bitmap = (previewIV.drawable as BitmapDrawable).bitmap
                    var stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray = stream.toByteArray()
                    intent.putExtra("image", byteArray)
                    startActivity(intent)
                }
            }

            return root
        }
    }

    fun loadImage() {
        val selectedNum: Int = arguments?.getInt("fwNum") ?: 0

        if (selectedNum != 0) {
            selectedNumber = selectedNum

            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_GALLERY)
        } else {
            Toast.makeText(this@BottomSheetDialogMy.context,
                "서비스 오류로 인해 이미지를 업로드 할 수 없습니다",
                Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {

                val dataUri: Uri? = data?.data

                if (dataUri != null) {


                    val realPath: String = getRealPathFromURI(dataUri)

                    var bitmapImage: Bitmap = BitmapFactory.decodeFile(realPath)

                    var bitmapImage2: Bitmap = getResizedBitmap(bitmapImage, 500)

                    binding.previewIV.setImageBitmap(bitmapImage2)

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

                    val fwName2: String = arguments?.getString("fwName") ?: "-"

                    addNewEdit(LOG_ON_STATUS!!.digital_code,
                        fwName2,
                        "photo",
                        getCurrentTimeFormat())

                    changeFWPicAsync(file2, selectedNumber, fwName2)

//                    dismiss()
//
//                    MapFragment.selectedMarker.performClick()
                }
            }
        }
    }

    private fun insertEditHistoryPhoto(fwName: String) {
        val eName: String = fwName
        val eType: String = "photo"
        val eDate: String = getCurrentTimeFormat()

        val eValues = ContentValues()
        with(eValues) {
            put("fw_name", eName)
            put("edited_type", eType)
            put("edited_Date", eDate)
        }
        val ePK = FireWaterSPLiteOpenHelper.getDBInstance(requireActivity())
            .insertEditedHistory(eValues, TABLE_EDIT_HISTORY)
    }

    private fun refreshHomeFragmentList() {
        with(CoroutineScope(Dispatchers.Main)) {
            launch {
                with((requireActivity() as MainActivity)) {
                    if (this.isAlreadyAdded(this.fragmentHo)) {
                        Log.e(MY_DEBUG_TAG,
                            "this.isAlreadyAdded(this.fragmentHo)= ${this.isAlreadyAdded(this.fragmentHo)}")
                        this.fragmentHo.refreshEditedRecycler()
                    }
                }
            }
        }
    }

    fun getCurrentTimeFormat(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    fun addNewEdit(
        digitalCode: Long,
        editedName: String,
        editedType: String,
        editedTime: String,
    ) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<CntResult> = firewaterService.sendEditLog(
            digitalCode,
            editedName,
            editedType,
            editedTime)

        call.enqueue(object : Callback<CntResult> {
            override fun onResponse(
                call: Call<CntResult>,
                response: Response<CntResult>,
            ) {
                if (response.isSuccessful) {
                    val cntResult = response.body() as CntResult

                    if (cntResult.cnt >= 1) {

                    }
                }
            }

            override fun onFailure(call: Call<CntResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "사진 수정 중 오류가 발생했어요 (1)",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    fun changeFWPicAsync(
        //DB에 사진을 업로드하고 file name 을 업데이트 합니다.
        uploadFile: File,
        number: Int,
        fwName: String,
    ) {

        var response: okhttp3.Response? = null

        val toServer: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val fileUploadBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM) // 파일 업로드시 반드시 설정
            .addFormDataPart("number", number.toString())
            .addFormDataPart("uploadFile", uploadFile.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), uploadFile))
            .build()

        // 요청 세팅
        val request: Request = Request.Builder()
            .url("http://59.9.249.206:8081/api/firewater/change/fwpic")
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
                        Log.d(MY_DEBUG_TAG, "소화전 사진 변경 완료")
                        insertEditHistoryPhoto(fwName)
                        refreshHomeFragmentList()

                        (requireActivity() as MainActivity).showToast("소화전 사진을 변경했어요")

                    } else {
                        Log.e(MY_DEBUG_TAG, "소화전 사진 변경 실패 - 통신 상태가 좋지 않을 경우 변경되지 않을 수 있어요")
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(MY_DEBUG_TAG, "Error 발생!: $e")
            } finally {
                response?.close()
                uploadFile.delete()
                dismiss()
            }
        }.start()

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
            var cursor: Cursor? =
                requireActivity().contentResolver.query(uri, null, null, null, null)

            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {

        val R = 6372.8 * 1000

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(
                lat2))
        val c = 2 * asin(sqrt(a))

        return (R * c).toInt() // 두 좌표 사이의 거리 (m)
    }

}



