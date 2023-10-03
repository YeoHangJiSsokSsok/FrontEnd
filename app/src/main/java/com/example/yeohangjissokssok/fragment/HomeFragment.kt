package com.example.yeohangjissokssok.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeohangjissokssok.R
import com.example.yeohangjissokssok.activity.APIResponseData
import com.example.yeohangjissokssok.activity.ButtonAdapter
import com.example.yeohangjissokssok.activity.PlaceAdapterRecommend
import com.example.yeohangjissokssok.activity.ResultActivity
import com.example.yeohangjissokssok.api.RetrofitBuilder
import com.example.yeohangjissokssok.databinding.FragmentHomeBinding
import com.example.yeohangjissokssok.databinding.FragmentRecommendBinding
import com.example.yeohangjissokssok.models.SACategoryResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    lateinit var placeAdapterRecommend: PlaceAdapterRecommend

    // 바인딩 객체 선언
    private lateinit var binding: FragmentHomeBinding

    val datas = ArrayList<SACategoryResponse>()

    var placeId = 3
    var region = "region"
    val name = "name"
    val positiveNumber = 1
    val totalNumber = 1
    val proportion = 0.001

    private var selectedImageResource: Int = 0
    private var selectedButtonIndex: Int = -1

    var isMoodClicked = false
    var isTransportClicked = false
    var isCongestionClicked = false
    var isInfraClicked = false
    var isPurposeClicked = false


    private fun getSACategoryPlace(input: String) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getSACategoryPlace(input).enqueue(object :
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
                        Log.d("result", result.toString())

                        // initRecycler 함수를 호출하여 데이터 추가
                        initRecycler(result)
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


        binding.apply {
            // 클릭 이벤트 핸들러 내에서 getSACategoryPlace를 호출하지 않고
            // 해당 카테고리를 저장하고, 아래에서 한 번에 호출하도록 변경
            lateinit var category:String

            radioButton1.setOnClickListener {
                // 분위기 선택 시
                category = "C001"
                updateRecyclerView(category)
            }

            radioButton2.setOnClickListener {
                // 교통 선택 시
                category = "C002"
                updateRecyclerView(category)
            }

            radioButton3.setOnClickListener {
                // 혼잡도 선택 시
                category = "C003"
                updateRecyclerView(category)
            }

            radioButton4.setOnClickListener {
                // 인프라 선택 시
                category = "C004"
                updateRecyclerView(category)
            }
        }

        // 초기 카테고리 설정 (예: "C001")
        val initialCategory = "C001"
        updateRecyclerView(initialCategory)

        placeAdapterRecommend = PlaceAdapterRecommend(this.datas)
        binding.rvPlace.adapter = placeAdapterRecommend

        // 리사이클러뷰 구분선 지정
        val dividerItemDecoration =
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvPlace.addItemDecoration(dividerItemDecoration)

        initClickEvent()

        return view
    }

    private fun updateRecyclerView(category: String) {
        // 서버와 통신하여 리사이클러뷰 데이터 업데이트
        getSACategoryPlace(category)
    }


    private fun initRecycler(result: List<SACategoryResponse>) {
        // 데이터 추가
        datas.clear() // 데이터 초기화
        datas.addAll(result)

        // RecyclerView 어댑터에 데이터 설정
        placeAdapterRecommend.datas = datas

        // RecyclerView 갱신
        placeAdapterRecommend.notifyDataSetChanged()
    }


    private fun initClickEvent() {
        binding.apply {
            //            goBackBtn.setOnClickListener {
            //                // 뒤로가기 버튼 클릭 시 이벤트
            //                requireActivity().onBackPressed() // 현재 Fragment를 스택에서 제거
            //            }


            // adapter에 클릭리스너 부착
            // 여행지 클릭 시 이벤트
            placeAdapterRecommend.itemClicklistener =
                object : PlaceAdapterRecommend.OnItemClickListener {
                    override fun OnItemClick(position: Int) {
                        // RecommendListFragment를 호스팅하는 Activity에서 다른 Activity로 전환
                        val intent = Intent(requireActivity(), ResultActivity::class.java)
                        intent.putExtra("placeId", placeAdapterRecommend.datas[position].placeId)
                        intent.putExtra("region", placeAdapterRecommend.datas[position].region)
                        intent.putExtra("name", placeAdapterRecommend.datas[position].name)
                        intent.putExtra(
                            "positiveNumber",
                            placeAdapterRecommend.datas[position].positiveNumber
                        )
                        intent.putExtra(
                            "totalNumber",
                            placeAdapterRecommend.datas[position].totalNumber
                        )
                        intent.putExtra(
                            "proportion",
                            placeAdapterRecommend.datas[position].proportion
                        )
                        intent.putExtra("page", "placeList")
                        startActivity(intent)
                    }
                }
        }
    }
}