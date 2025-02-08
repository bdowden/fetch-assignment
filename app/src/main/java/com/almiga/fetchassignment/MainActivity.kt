package com.almiga.fetchassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.almiga.fetchassignment.ui.composables.FetchItemListView
import com.almiga.fetchassignment.ui.theme.FetchAssignmentTheme
import com.almiga.fetchassignment.viewModels.FetchItemListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: FetchItemListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FetchAssignmentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewState by viewModel.viewState.collectAsState()

                    FetchItemListView(
                        modifier = Modifier.padding(innerPadding),
                        refreshData = viewModel::refreshItems,
                        viewState = viewState,
                    )
                }
            }
        }
    }
}