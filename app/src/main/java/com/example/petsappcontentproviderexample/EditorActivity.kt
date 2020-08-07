package com.example.petsappcontentproviderexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu

class EditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }
}
