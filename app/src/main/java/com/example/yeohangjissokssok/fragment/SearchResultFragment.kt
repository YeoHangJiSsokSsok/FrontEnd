package com.example.yeohangjissokssok.fragment

import PlaceAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.yeohangjissokssok.R
import com.example.yeohangjissokssok.activity.APIResponseData
import com.example.yeohangjissokssok.activity.PlaceResponse
import com.example.yeohangjissokssok.activity.ResultActivity
import com.example.yeohangjissokssok.api.RetrofitBuilder
import com.example.yeohangjissokssok.databinding.ActivityPlaceListBinding
import com.example.yeohangjissokssok.databinding.FragmentSearchresultBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface FragmentVisibilityListener {
    fun onFragmentVisibilityChanged(isVisible: Boolean)
}

class SearchResultFragment : Fragment() {
    lateinit var placeAdapter: PlaceAdapter

    // 바인딩 객체 선언
    private lateinit var binding: FragmentSearchresultBinding

    val datas = ArrayList<PlaceResponse>()


    //var id = 3
    var name = "name"
    var region = "region"
    var address = "address"
    var body = ""

    private lateinit var searchEditText: EditText
    private lateinit var searchBtnInplacelist: ImageView

    private var visibilityListener: FragmentVisibilityListener? = null

    fun setVisibilityListener(listener: FragmentVisibilityListener) {
        visibilityListener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchresultBinding.inflate(inflater, container, false)
        val view = binding.root

        searchEditText = binding.searchEditText
        searchBtnInplacelist = binding.searchBtnInplacelist

        placeAdapter = PlaceAdapter(this.datas)
        binding.rvRecommendlist.adapter = placeAdapter

        // RecyclerView를 처음에는 숨김(GONE) 상태로 설정
        binding.rvRecommendlist.visibility = View.GONE

        // 리사이클러뷰 구분선 지정
        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvRecommendlist.addItemDecoration(dividerItemDecoration)

        initRecycler()
        initClickEvent()

        searchBtnInplacelist.setOnClickListener {
            val searchText = searchEditText.text.toString()
            if (searchText.isNotEmpty()) {
                fetchPlacesByName(searchText)

                // 검색 버튼 클릭 시 리사이클러뷰를 보이도록 변경
                binding.rvRecommendlist.visibility = View.VISIBLE
            }
        }

        return view
    }


    private fun initRecycler() {
        // 데이터 추가
        datas.add(PlaceResponse(1, "낙성대공원", "서울 관악", null))
        datas.add(PlaceResponse(1, "낙성대공원", "서울 관악", null))
        datas.add(PlaceResponse(1, "낙성대공원", "서울 관악", null))

        // 어댑터에 데이터 설정
        placeAdapter.datas = datas

        // RecyclerView를 갱신
        placeAdapter.notifyDataSetChanged()
    }

    private fun initClickEvent() {
        searchBtnInplacelist.setOnClickListener {
            val searchText = searchEditText.text.toString()
            if (searchText.isNotEmpty()) {
                fetchPlacesByName(searchText)

                // 검색 버튼 클릭 시 Fragment의 가시성 변경을 알림
                visibilityListener?.onFragmentVisibilityChanged(true)
            }
        }
    }


    private fun fetchPlacesByName(searchText: String) {
        getPlaceByName(searchText) { result ->
            // 데이터를 가져온 후 어댑터에 데이터 설정
            datas.clear() // 기존 데이터를 지우고 새로운 데이터로 대체
            datas.addAll(result)
            placeAdapter.notifyDataSetChanged()

            // 리사이클러뷰를 보이도록 변경
            binding.rvRecommendlist.visibility = View.VISIBLE

            // 로그를 추가하여 데이터가 잘 가져와지는지 확인
            Log.d("SearchResultFragment", "Fetched ${result.size} places by name")
        }
    }


    private fun getPlaceByName(input: String, callback: (List<PlaceResponse>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitBuilder.api.getPlaceByName(input).enqueue(object : Callback<APIResponseData> {
                override fun onResponse(
                    call: Call<APIResponseData>, response: Response<APIResponseData>
                ) {
                    if (response.isSuccessful) {
                        val temp = response.body() as APIResponseData
                        val jsonResult = Gson().toJson(temp.data)
                        val result: List<PlaceResponse> = GsonBuilder()
                            .create()
                            .fromJson(jsonResult, Array<PlaceResponse>::class.java)
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
}
