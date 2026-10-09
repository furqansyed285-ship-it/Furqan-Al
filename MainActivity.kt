package com.furqanai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import android.graphics.drawable.GradientDrawable
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import org.json.JSONObject

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var chat: LinearLayout
    private lateinit var input: EditText
    private lateinit var tts: TextToSpeech

    // Change this to your deployed HTTPS backend URL.
    private val BACKEND_URL = "https://YOUR-BACKEND-DOMAIN.example.com/chat"

    override fun onCreate(b: Bundle?) { super.onCreate(b); tts=TextToSpeech(this,this); buildUI() }

    private fun buildUI() {
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.WHITE)}
        val header=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(18,12,10,8)}
        val title=TextView(this).apply{text="Furqan AI";textSize=23f;setTextColor(0xFF151515.toInt())}
        header.addView(title,LinearLayout.LayoutParams(0,-2,1f))
        val fresh=Button(this).apply{text="New";setOnClickListener{chat.removeAllViews();welcome()}}
        header.addView(fresh);root.addView(header)

        val scroll=ScrollView(this)
        chat=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,8,14,10)}
        scroll.addView(chat);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))

        val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(7,5,7,10)}
        val mic=Button(this).apply{text="🎤";setOnClickListener{voice()}}
        row.addView(mic)
        input=EditText(this).apply{hint="Message Furqan AI...";setSingleLine(false);maxLines=4;setPadding(14,7,14,7);background=box(0xFFF1F3F6.toInt(),24f)}
        row.addView(input,LinearLayout.LayoutParams(0,-2,1f))
        val send=Button(this).apply{text="Send";setOnClickListener{sendMessage()}}
        row.addView(send);root.addView(row);setContentView(root);welcome()
    }

    private fun welcome(){addBubble("Assalam-o-Alaikum 👋\nI'm Furqan AI. Ask me anything.",false)}

    private fun sendMessage() {
        val q=input.text.toString().trim(); if(q.isEmpty()) return
        addBubble(q,true); input.setText("")
        addBubble("Thinking…",false)
        Thread {
            try {
                val url=URL(BACKEND_URL)
                val conn=url.openConnection() as HttpURLConnection
                conn.requestMethod="POST"; conn.doOutput=true
                conn.setRequestProperty("Content-Type","application/json")
                conn.connectTimeout=15000;conn.readTimeout=60000
                val body=JSONObject().put("message",q).toString()
                OutputStreamWriter(conn.outputStream).use{it.write(body)}
                val stream=if(conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val result=BufferedReader(InputStreamReader(stream)).use{it.readText()}
                val answer=JSONObject(result).optString("reply","No reply received.")
                runOnUiThread {
                    if(chat.childCount>0) chat.removeViewAt(chat.childCount-1)
                    addBubble(answer,false)
                    tts.speak(answer,TextToSpeech.QUEUE_FLUSH,null,"furqan_reply")
                }
            } catch(e:Exception) {
                runOnUiThread {
                    if(chat.childCount>0) chat.removeViewAt(chat.childCount-1)
                    addBubble("Connection error. Check your backend URL and internet connection.",false)
                }
            }
        }.start()
    }

    private fun voice(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){addBubble("Voice recognition is not available.",false);return}
        val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ur-PK")
            putExtra(RecognizerIntent.EXTRA_PROMPT,"Furqan AI ko bol kar message dein")
        }
        startActivityForResult(i,99)
    }

    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==99&&c==RESULT_OK){
        val s=d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if(!s.isNullOrBlank()){input.setText(s);sendMessage()}
    }}

    private fun addBubble(s:String,user:Boolean){
        val t=TextView(this).apply{text=s;textSize=16f;setTextColor(if(user)Color.WHITE else 0xFF202020.toInt());setPadding(16,12,16,12);background=box(if(user)0xFF2563EB.toInt() else 0xFFF1F3F6.toInt(),21f)}
        val p=LinearLayout.LayoutParams(-2,-2).apply{gravity=if(user)Gravity.END else Gravity.START;setMargins(0,5,0,5)}
        chat.addView(t,p)
    }
    private fun box(c:Int,r:Float)=GradientDrawable().apply{setColor(c);cornerRadius=r}
    override fun onInit(status:Int){if(status==TextToSpeech.SUCCESS)tts.language=Locale.US}
    override fun onDestroy(){tts.stop();tts.shutdown();super.onDestroy()}
}
