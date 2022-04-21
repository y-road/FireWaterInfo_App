package com.sesac.firewaterinfo.dialogs

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sesac.firewaterinfo.LOG_ON_STATUS
import com.sesac.firewaterinfo.MARKERSS
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.MainActivity
import com.sesac.firewaterinfo.common.FireApplication
import com.sesac.firewaterinfo.common.FireWaterSPLiteOpenHelper
import com.sesac.firewaterinfo.common.RetrofitOkHttpManager
import com.sesac.firewaterinfo.common.data.AllFW
import com.sesac.firewaterinfo.common.data.SimpleFW
import com.sesac.firewaterinfo.common.data.CntResult
import com.sesac.firewaterinfo.common.data.TABLE_EDIT_HISTORY
import com.sesac.firewaterinfo.databinding.BottomDetailInfo2EditBinding
import com.sesac.firewaterinfo.fragments.MapFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetDialogEdit(val alfw3: AllFW) : BottomSheetDialogFragment() {

    private var _binding: BottomDetailInfo2EditBinding? = null
    private val binding get() = _binding!!

    private var fSerial: Int = 0
    private var fName: String = ""
    private var fType: String = ""
    private var fAddress: String = ""
    private var fDetail: String = ""
    private var fLatitude: Double = 0.0
    private var fLongitude: Double = 0.0
    private var fCenter: String = ""
    private var fProtectCase: Boolean = false
    private var fAvailable: Boolean = false
    private var fInstallationYear: Int = 0
    private var fPipeDepth: Double = 0.0
    private var fPipeDiameter: Double = 0.0
    private var fOutPressure: Double = 0.0

    //    private var fUpdateDate: String = ""
    private var fImgName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = BottomDetailInfo2EditBinding.inflate(inflater, container, false)

        val dialog = dialog as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        dialog.behavior.skipCollapsed = true

        with(binding) {

            with(alfw3) {
                detailTV1.setText(name)

                availableSwitch.isChecked = available

                detailTV2.setText(address)
                detailTV3.setText(detail)
                when (type) {
                    "지상식 소화전" -> spinnerType.setSelection(0)
                    "지하식 소화전" -> spinnerType.setSelection(1)
                    "급수탑" -> spinnerType.setSelection(2)
                    "저수조" -> spinnerType.setSelection(3)
                    "승하강식" -> spinnerType.setSelection(4)
                    else -> spinnerType.setSelection(5)
                }

                detailTV5.setText(latitude.toString())
                detailTV6.setText(longitude.toString())
                detailTV7.setText(center)
//                detailTV8
                if (protect_case) {
                    protectCaseSwitch.isChecked = true
                    protectCaseSwitch.text = "유"
                } else {
                    protectCaseSwitch.isChecked = false
                    protectCaseSwitch.text = "무"
                }
                detailTV9.setText(installation_year.toString())
                detailTV10.setText(pipe_depth.toString())
                detailTV11.setText(out_pressure.toString())
                detailTV112.setText(pipe_diameter.toString())
            }

            emptySpacePro.setOnClickListener {
                (activity as MainActivity).hideKeyboard(true)
            }

            detailSaveBtn.setOnClickListener {
                if (isExceptionCheck()) {

                    setDefault()
                    insertEditData()

                    addNewEdit(
                        LOG_ON_STATUS!!.digital_code,
                        alfw3.name,
                        "text",
                        getCurrentTimeFormat())

                    insertEditHistoryText(alfw3.name)
                    refreshHomeFragmentList()

                    (activity as MainActivity).fragmentMa.clearMarker()
                    dismiss()
                }
            }
            return root
        }
    }

    private fun refreshHomeFragmentList() {
        with(CoroutineScope(Dispatchers.Main)) {
            launch {
                with((requireActivity() as MainActivity)) {
                    if (this.isAlreadyAdded(this.fragmentHo)) {
                        this.fragmentHo.refreshEditedRecycler()
                    }
                }
            }
        }
    }

    private fun insertEditHistoryText(fwName: String) {
        val eName: String = fwName
        val eType: String = "text"
        val eDate: String = getCurrentTimeFormat()

        val eValues = ContentValues()
        with(eValues) {
            put("fw_name", eName)
            put("edited_type", eType)
            put("edited_Date", eDate)
        }
        val ePK = FireWaterSPLiteOpenHelper.getDBInstance(requireActivity()).insertEditedHistory(eValues, TABLE_EDIT_HISTORY)
    }

    fun insertEditData() {
        with(binding) {
            fSerial = alfw3.serial
            fName = detailTV1.text.toString().replace("\'","").replace("\"","")
            fType = spinnerType.selectedItem.toString().replace("\'","").replace("\"","")
            fAddress = detailTV2.text.toString().replace("\'","").replace("\"","")
            fDetail = detailTV3.text.toString().replace("\'","").replace("\"","")
            fLatitude = detailTV5.text.toString().toDouble()
            fLongitude = detailTV6.text.toString().toDouble()
            fCenter = detailTV7.text.toString().replace("\'","").replace("\"","")
            fProtectCase = protectCaseSwitch.isChecked
            fAvailable = availableSwitch.isChecked
            fInstallationYear = detailTV9.text.toString().toInt()
            fPipeDepth = detailTV10.text.toString().toDouble()
            fPipeDiameter = detailTV112.text.toString().toDouble()
            fOutPressure = detailTV11.text.toString().toDouble()
//            fUpdateDate = getCurrentTimeFormat()
            fImgName = alfw3.img_name
        }
    }

    fun setDefault() {
        fSerial = 0
        fName = ""
        fType = ""
        fAddress = ""
        fDetail = ""
        fLatitude = 0.0
        fLongitude = 0.0
        fCenter = ""
        fProtectCase = false
        fAvailable = false
        fInstallationYear = 0
        fPipeDepth = 0.0
        fPipeDiameter = 0.0
        fOutPressure = 0.0
//        fUpdateDate = ""
        fImgName = ""
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
//                        Toast.makeText(FireApplication.getFireApplication(),
//                            "수정 로그 추가- 반환: ${cntResult.cnt}",
//                            Toast.LENGTH_SHORT).show()
                        addNewFw(
                            fName,
                            fSerial,
                            fType,
                            fAddress,
                            fDetail,
                            fLatitude,
                            fLongitude,
                            fCenter,
                            fProtectCase,
                            fAvailable,
                            fInstallationYear,
                            fPipeDepth,
                            fPipeDiameter,
                            fOutPressure,
                            getCurrentTimeFormat(),
                            fImgName
                        )

                    }
                }
            }

            override fun onFailure(call: Call<CntResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "정보 수정 중 오류가 발생했어요 (1)",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    fun addNewFw(
        name: String,
        serial: Int,
        type: String,
        address: String,
        detail: String,
        latitude: Double,
        longitude: Double,
        center: String,
        protectCase: Boolean,
        available: Boolean,
        installationYear: Int,
        pipeDepth: Double,
        pipeDiameter: Double,
        outPressure: Double,
        updateDate: String,
        imgName: String,
    ) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<CntResult> = firewaterService.sendEditFW(
            name,
            serial,
            type,
            address,
            detail,
            latitude,
            longitude,
            center,
            protectCase,
            available,
            installationYear,
            pipeDepth,
            pipeDiameter,
            outPressure,
            updateDate,
            imgName)

        call.enqueue(object : Callback<CntResult> {
            override fun onResponse(
                call: Call<CntResult>,
                response: Response<CntResult>,
            ) {
                if (response.isSuccessful) {
                    val cntResult = response.body() as CntResult

                    if (cntResult.cnt >= 1) {
//                        Toast.makeText(FireApplication.getFireApplication(),
//                            "소화전 추가 - 반환: ${cntResult.cnt}",
//                            Toast.LENGTH_SHORT).show()
                        removeOldFW(alfw3.number)
                    }
                }
            }

            override fun onFailure(call: Call<CntResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "정보 수정 중 오류가 발생했어요 (2)",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    fun removeOldFW(number: Int) {
        val firewaterService = RetrofitOkHttpManager.firewaterRESTService
        val call: Call<CntResult> = firewaterService.removeFW(number)

        call.enqueue(object : Callback<CntResult> {
            override fun onResponse(
                call: Call<CntResult>,
                response: Response<CntResult>,
            ) {
                if (response.isSuccessful) {
                    val cntResult = response.body() as CntResult

                    if (cntResult.cnt >= 1) {
//                        Toast.makeText(FireApplication.getFireApplication(),
//                            "기존 소화전 삭제 - 반환: ${cntResult.cnt}",
//                            Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<CntResult>, t: Throwable) {
                Toast.makeText(FireApplication.getFireApplication(),
                    "정보 수정 중 오류가 발생했어요 (3)",
                    Toast.LENGTH_SHORT).show()
                Log.e(MY_DEBUG_TAG, t.toString())
            }
        })
    }

    fun isExceptionCheck(): Boolean {

        with(binding) {
            if (detailTV1.text.isEmpty()) {
                detailTV1.requestFocus()
                Toast.makeText(requireActivity(), "명칭은 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }
            if (detailTV2.text.isEmpty()) {
                detailTV2.requestFocus()
                Toast.makeText(requireActivity(), "주소는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }
            if (detailTV3.text.isEmpty()) {
                detailTV3.requestFocus()
                Toast.makeText(requireActivity(), "설명은 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }
            if (detailTV5.text.isEmpty()) {
                detailTV5.requestFocus()
                Toast.makeText(requireActivity(), "위도는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            val lat: Double = detailTV5.text.toString().toDouble()
            if (lat < 33.0 || lat > 43.0) {
                detailTV5.requestFocus()
                Toast.makeText(requireActivity(), "위도는 국내만 지정 가능해요 (33 ~ 43)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

            if (detailTV6.text.isEmpty()) {
                detailTV6.requestFocus()
                Toast.makeText(requireActivity(), "경도는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            val lon: Double = detailTV6.text.toString().toDouble()
            if (lon < 124.0 || lon > 132.0) {
                detailTV5.requestFocus()
                Toast.makeText(requireActivity(), "경도는 국내만 지정 가능해요 (124 ~ 132)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

            if (detailTV7.text.isEmpty()) {
                detailTV7.requestFocus()
                Toast.makeText(requireActivity(), "담당 센터는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            if (detailTV9.text.isEmpty()) {
                detailTV9.requestFocus()
                Toast.makeText(requireActivity(), "설치 연도는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            if (detailTV10.text.isEmpty()) {
                detailTV10.requestFocus()
                Toast.makeText(requireActivity(), "배관 깊이는 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            if (detailTV11.text.isEmpty()) {
                detailTV11.requestFocus()
                Toast.makeText(requireActivity(), "출수 압력은 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }

            if (detailTV112.text.isEmpty()) {
                detailTV112.requestFocus()
                Toast.makeText(requireActivity(), "배관 지름은 빈칸일 수 없어요", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

}