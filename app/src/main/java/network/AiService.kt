package com.example.wga.network

import com.example.wga.data.UserInput  // UserInput 클래스 import
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AiService {
    // 질문 생성 API
    @POST("/generate_questions")
    fun sendUserInput(@Body userInput: UserInput): Call<List<String>>  // List<String>으로 질문들을 반환받음

    // 채팅 메시지 처리 API
    @POST("/chat")
    fun chat(@Body userInput: UserInput): Call<String>  // GPT 응답을 String으로 받음
}
