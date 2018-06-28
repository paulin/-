package com.mop.friendflare

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_location_request.*
import kotlinx.android.synthetic.main.activity_send_location.*

class SendLocationActivity : AppCompatActivity() {

    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_location)

        try {
            var bundle: Bundle = intent.extras
            id = bundle.getInt("MainActId", 0)
            if (id != 0) {
                edtContent.setText(bundle.getString("MainActContent"))
            }
        } catch (ex: Exception) {
        }

        btGetGeo.setOnClickListener {

            var values = ContentValues()


            Toast.makeText(this, "Got GEO!", Toast.LENGTH_LONG).show()
            //values.put("Content", edtContent.text.toString())
        }


        btShrink.setOnClickListener {
            Toast.makeText(this, "Shrinking URL", Toast.LENGTH_LONG).show()

            var values = ContentValues()

            //values.put("Content", edtContent.text.toString())

        }

        btSend.setOnClickListener {
            Toast.makeText(this, "Sending Geo", Toast.LENGTH_LONG).show()
        }
    }
}