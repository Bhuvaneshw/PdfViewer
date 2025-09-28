package com.bhuvaneshw.pdfviewerdemo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bhuvaneshw.pdfviewerdemo.ui.theme.PdfViewerComposeDemoTheme
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.collections.immutable.toImmutableList

class UsedLibrariesActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PdfViewerComposeDemoTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = "Libraries used") })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val context = LocalContext.current

                    val libs = Libs
                        .Builder()
                        .withContext(context)
                        .build()

                    LibrariesContainer(
                        libraries = libs.copy(
                            libraries = libs.libraries
                                .distinctBy { it.name }
                                .toImmutableList()
                        ),
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        showDescription = true,
                    )
                }
            }
        }
    }

}
