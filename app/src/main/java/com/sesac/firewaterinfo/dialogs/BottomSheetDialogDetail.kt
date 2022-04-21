package com.sesac.firewaterinfo.dialogs

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.Toolbar
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sesac.firewaterinfo.*
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FireWaterSPLiteOpenHelper
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.CntResult
import com.sesac.firewaterinfo.databinding.BottomDetailInfo2Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class BottomSheetDialogDetail(val alfw2: AllFW): BottomSheetDialogFragment() {

    private var _binding: BottomDetailInfo2Binding? = null
    private val binding get() = _binding!!

    private var isOpenableImage = false

    private var isAlreadyFavoriteList = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomDetailInfo2Binding.inflate(inflater, container, false)

        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        dialog.behavior.skipCollapsed = true

        with(binding) {

            val imgName: String? = alfw2.img_name

            if (imgName != null && imgName.isNotEmpty()) {
                val targetImage = "$FIREWATER_IMAGE_ADDRESS${imgName}"
                Log.d(MY_DEBUG_TAG, "Uri.parse(targetImage)= ${Uri.parse(targetImage)}")
                Glide.with(previewIV)
                    .load(Uri.parse(targetImage))
//                    .error(R.drawable.ic_no_img)
                    .transform(CenterCrop(), RoundedCorners(10))
                    .into(previewIV)

                isOpenableImage = true
            }

            with(alfw2) {
                detailNameTV1.text = name
                detailTV1.text = name

                if (available) {
                    detailAvailableTV1.setBackgroundResource(R.drawable.alive_background)
                } else {
                    detailAvailableTV2.setBackgroundResource(R.drawable.death_background)
                }

                detailTV2.text = address
                detailTV3.text = detail
                detailTV4.text = type
                detailTV5.text = latitude.toString()
                detailTV6.text = longitude.toString()
                detailTV7.text = center
//                detailTV8
                if (protect_case) {
                    detailTV8.text = "유"
                } else {
                    detailTV8.text = "무"
                }
                detailTV9.text = installation_year.toString()
                detailTV10.text = pipe_depth.toString()
                detailTV11.text = out_pressure.toString()
                detailTV112.text = pipe_diameter.toString()
                detailTV12.text = update_date

                if (isAlreadyExistMyFavorite(alfw2.serial)) {
                    isAlreadyFavoriteList = true
                    addStarBtn.setImageResource(R.drawable.ic_star_border_orange)
                }
            }

            detailFBtn.setOnClickListener {
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
            editBtn.setOnClickListener {
                if (LOG_ON_STATUS != null) {
                    val botEditMenu = BottomSheetEditMenu().newInstance(this@BottomSheetDialogDetail, alfw2)
                    botEditMenu.show(parentFragmentManager, botEditMenu.tag)
                } else {
                    Toast.makeText(this@BottomSheetDialogDetail.context,
                        "회원만 정보를 수정할 수 있습니다.",
                        Toast.LENGTH_SHORT).show()
                }

            }

            btnBackKey.setOnClickListener {
                dismiss()
            }

            addStarBtn.setOnClickListener { // 즐겨찾기 추가 버튼

                if (LOG_ON_STATUS != null) {
                    if (LOG_ON_STATUS!!.result) {
                        Thread.sleep(800)
                        if (isAlreadyFavoriteList) { // 즐겨찾기에 이미 있으면 삭제
                            Toast.makeText(FireApplication.getFireApplication(),
                                "즐겨찾기 삭제는 즐겨찾기 탭을 이용하세요",
                                Toast.LENGTH_SHORT).show()

                        } else { // 즐겨찾기에 없으면 추가
                            insertMyFavorite()
                            if ((activity as MainActivity).isAlreadyAdded((activity as MainActivity).fragmentMy)) {
                                (activity as MainActivity).fragmentMy!!.asyncRequestMyFavorites()
                            }
                        }

                    } else {
                        Toast.makeText(FireApplication.getFireApplication(),
                            "로그인 후 즐겨찾기 기능을 사용할 수 있어요",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(FireApplication.getFireApplication(),
                        "로그인 후 즐겨찾기 기능을 사용할 수 있어요",
                        Toast.LENGTH_SHORT).show()
                }
            }

            return root
        }
    }

    private fun isAlreadyExistMyFavorite(serial: Int) : Boolean{

        if (MY_FAVORITE != null) {
            MY_FAVORITE!!.forEach {
                if (serial == it.serial) {
                    return true
                }
            }
        }

        return false
    }

    private fun insertMyFavorite() {

        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<CntResult> = firewaterService.addFavorite(LOG_ON_STATUS!!.digital_code, alfw2.serial)

        call.enqueue(object : Callback<CntResult> {
            override fun onResponse(call: Call<CntResult>, response: Response<CntResult>) {
                if (response.isSuccessful) {
                    val cntResult = response.body() as CntResult

                    if (cntResult.cnt >= 1) {
                        Toast.makeText(FireApplication.getFireApplication(),
                            "내 즐겨찾기에 추가했어요",
                            Toast.LENGTH_SHORT).show()
                        isAlreadyFavoriteList = true
                        binding.addStarBtn.setImageResource(R.drawable.ic_star_border_orange)
                    }
                }
            }

            override fun onFailure(call: Call<CntResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "즐겨찾기 추가 도중 오류가 발생했어요",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun insertMyFavorite() {
//
//        with(alfw2) {
//            val mFSerial: Int = serial
////            val mFName: String = name
////            val mFAddress: String = address
////            val mFDetail: String = detail
////            val mFDate: String = update_date
////            val mFAvailable: Boolean = available
//
//            val mFValues = ContentValues()
//            with(mFValues) {
//                put("fw_serial", mFSerial)
////                put("fw_name", mFName)
////                put("fw_address", mFAddress)
////                put("fw_detail", mFDetail)
////                put("fw_date", mFDate)
////                put("fw_available", mFAvailable)
//            }
//
//            val mFPK = FireWaterSPLiteOpenHelper.getDBInstance(requireActivity()).insertMyFavorite(mFValues, TABLE_MY_FAVORITE)
//        }
//    }

}
