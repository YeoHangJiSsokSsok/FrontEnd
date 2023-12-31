package com.example.yeohangjissokssok.fragment

import PlaceAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.yeohangjissokssok.activity.APIResponseData
import com.example.yeohangjissokssok.activity.PlaceResponse
import com.example.yeohangjissokssok.activity.ResultActivity
import com.example.yeohangjissokssok.api.RetrofitBuilder
import com.example.yeohangjissokssok.databinding.FragmentHomeBinding
import com.example.yeohangjissokssok.models.PlaceData
import com.example.yeohangjissokssok.models.SACategoryResponse
import com.example.yeohangjissokssok.models.SAPlaceResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class HomeFragment : Fragment() {
    // 바인딩 객체 선언
    private lateinit var binding: FragmentHomeBinding

    val curDate: LocalDate = LocalDate.now()
    val month = curDate.monthValue

    var caPlaceIds = mutableListOf<Long>()

    val datas = ArrayList<PlaceData>()
    var placeAdapter = PlaceAdapter(datas)

    var categorynum = 0

    val name = "name"

    var time1 : Long = 0
    var time2 : Long = 0

    private fun getMonthSACategoryPlace(input: String) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getMonthSACategoryPlace(input, month).enqueue(object :
                Callback<APIResponseData> {
                override fun onResponse(
                    call: Call<APIResponseData>,
                    response: Response<APIResponseData>
                ) {
                    if (response.isSuccessful) {
                        val temp = response.body() as APIResponseData
                        val jsonResult = Gson().toJson(temp.data)
                        val result: List<SACategoryResponse> = GsonBuilder()
                            .create()
                            .fromJson(jsonResult, Array<SACategoryResponse>::class.java)
                            .toList()

                        // 받아온 placeId를 caPlaceIds 리스트에 추가
                        caPlaceIds.clear()
                        for (item in result) {
                            caPlaceIds.add(item.placeId)
                        }

                        // initRecycler 함수를 호출하여 데이터 추가
                        initRecycler(input)
                    }
                }

                override fun onFailure(call: Call<APIResponseData>, t: Throwable) {
                    Log.d("test", "연결실패")
                }
            })
        }
    }

    private fun getPlaceById(input: Long, callback: (PlaceResponse) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getPlace(input).enqueue(object : Callback<APIResponseData> {
                override fun onResponse(
                    call: Call<APIResponseData>, response: Response<APIResponseData>
                ) {
                    if (response.isSuccessful) {
                        val temp = response.body() as APIResponseData
                        val jsonResult = Gson().toJson(temp.data)
                        val result: PlaceResponse = GsonBuilder()
                            .create()
                            .fromJson(jsonResult, PlaceResponse::class.java)
                        callback(result) // 데이터를 가져온 후 콜백으로 전달
                    }
                }

                override fun onFailure(call: Call<APIResponseData>, t: Throwable) {
                    Log.d("test", "연결실패")
                }
            })
        }
    }

    // 월 별 감성분석 결과 불러오는 메소드
    private fun getPlaceMonthlySAResult(id: Long, month: Int, callback: (List<SAPlaceResponse>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getSAMonthPlace(id, month).enqueue(object : Callback<APIResponseData> {
                override fun onResponse(
                    call: Call<APIResponseData>, response: Response<APIResponseData>
                ) {
                    if (response.isSuccessful) {
                        val temp = response.body() as APIResponseData
                        val jsonResult = Gson().toJson(temp.data)
                        val result: List<SAPlaceResponse> = GsonBuilder()
                            .create()
                            .fromJson(jsonResult, Array<SAPlaceResponse>::class.java)
                            .toList()
                        callback(result) // 데이터를 가져온 후 콜백으로 전달
                    }
                }

                override fun onFailure(call: Call<APIResponseData>, t: Throwable) {
                    Log.d("test", "연결실패")
                }
            })
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d("month", month.toString())

        binding.apply {
            // 현재 날짜 기준 월 설정
            monthText.text = month.toString() + "월 BEST"

            // 클릭 이벤트 핸들러 내에서 getSACategoryPlace를 호출하지 않고
            // 해당 카테고리를 저장하고, 아래에서 한 번에 호출하도록 변경
            lateinit var category:String

            radioButton1.setOnClickListener {
                // 분위기 선택 시
                category = "C001"
                categorynum = 0
                getMonthSACategoryPlace(category)
            }

            radioButton2.setOnClickListener {
                // 교통 선택 시
                category = "C002"
                categorynum = 1
                getMonthSACategoryPlace(category)
            }

            radioButton3.setOnClickListener {
                // 혼잡도 선택 시
                category = "C003"
                categorynum = 2
                getMonthSACategoryPlace(category)
            }

            radioButton4.setOnClickListener {
                // 인프라 선택 시
                category = "C004"
                categorynum = 3
                getMonthSACategoryPlace(category)
            }
        }

        // 초기 카테고리 설정 (예: "C001")
        val initialCategory = "C001"
        categorynum = 0
        getMonthSACategoryPlace(initialCategory)

        // 리사이클러뷰 구분선 지정
        val dividerItemDecoration =
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvPlace.addItemDecoration(dividerItemDecoration)

        // 이곳에서 initClickEvent() 호출
        initClickEvent()

        return view
    }

    private fun initRecycler(input: String) {
        datas.clear() // 데이터 초기화

        // placeAdapter 초기화
        binding.rvPlace.adapter = placeAdapter

        val totalPlaceIds = caPlaceIds.toList()

        var currentIndex = 0
        var itc = -1

        fun addNextPlace() {
            if (currentIndex < totalPlaceIds.size) {
                val placeId = totalPlaceIds[currentIndex]

                when (input) {
                    "C001" -> itc = 0
                    "C002" -> itc = 1
                    "C003" -> itc = 2
                    "C004" -> itc = 3
                }

                if(categorynum != itc){
                    return
                }

                // 데이터 가져오기
                getPlaceMonthlySAResult(placeId, month) { result ->

                    var totalnum = result[categorynum].positive + result[categorynum].negative + result[categorynum].neutral

                    // 장소 정보 가져오기
                    getPlaceById(placeId) { placeResult ->
                        val newPlaceResponse = PlaceData(
                            id = placeResult.id,
                            name = placeResult.name,
                            region = placeResult.region,
                            address = placeResult.address,
                            photoUrl = placeResult.photoUrl,
                            pos = result[categorynum].positive.toDouble() / totalnum * 100,
                            totalNum = result[categorynum].positive + result[categorynum].negative + result[categorynum].neutral
                        )

                        if(categorynum == itc){
                            datas.add(newPlaceResponse)
                            placeAdapter.notifyItemInserted(datas.size - 1)
                        }

                        currentIndex++ // 다음 장소를 가져오기 위해 인덱스 증가
                        addNextPlace() // 다음 장소를 가져오도록 재귀 호출
                    }
                }
            }
        }

        // 첫 번째 장소를 가져오기 위해 호출
        addNextPlace()
    }

    private fun initClickEvent() {
        binding.apply {
            //adapter에 클릭리스너 부착
            //여행지 클릭 시 이벤트
            placeAdapter.itemClicklistener =
                object : PlaceAdapter.OnItemClickListener {
                    override fun OnItemClick(position: Int) {
                        time1 = System.currentTimeMillis()
                        //Log.e("time1", time1.toString())
                        // HomeFragment를 호스팅하는 Activity에서 다른 Activity로 전환
                        val intent = Intent(requireActivity(), ResultActivity::class.java)
                        intent.putExtra("placeId", placeAdapter.datas[position].id)
                        intent.putExtra("region", placeAdapter.datas[position].region)
                        intent.putExtra("name", placeAdapter.datas[position].name)
                        intent.putExtra("page", "home")
                        Log.d("IntentData", "placeId: ${intent.getLongExtra("placeId", -1)}, region: ${intent.getStringExtra("region")}, name: ${intent.getStringExtra("name")}, page: ${intent.getStringExtra("page")}")
                        time2 = System.currentTimeMillis()
                        //Log.e("time2", time2.toString())

                        startActivity(intent)
                    }
                }
        }
    }
}