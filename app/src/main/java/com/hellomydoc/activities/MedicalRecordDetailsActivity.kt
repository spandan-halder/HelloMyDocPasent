package com.hellomydoc.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hellomydoc.Constants
import com.hellomydoc.MedicalRecordDetailsViewModel
import com.hellomydoc.activities.ui.theme.HelloMyDoc2Theme
import com.hellomydoc.data.MedicalRecord
import com.hellomydoc.placeValue

class MedicalRecordDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MedicalRecordDetailsViewModel by viewModels()
        if(savedInstanceState==null){
            intent?.let {
                viewModel.record.value = it.getSerializableExtra(Constants.MEDICAL_RECORD) as? MedicalRecord
            }
        }
        setContent {
            HelloMyDoc2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ContentUI(viewModel)
                }
            }
        }
    }

    @Composable
    private fun ContentUI(viewModel: MedicalRecordDetailsViewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            HeaderUI(viewModel)
            BodyUI(viewModel)
        }
    }

    @Composable
    private fun BodyUI(viewModel: MedicalRecordDetailsViewModel) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ){
            item{
                Spacer(Modifier.height(24.dp))
            }
            item{
                if(viewModel.record.value?.patientName==null&&viewModel.record.value?.patientName==""){
                    Text(
                        "Patient not provided",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }
                else{
                    Text(
                        viewModel.record.value?.patientName?:"Patient not provided",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }
            }
            item{
                Text(
                    viewModel.record.value?.name.placeValue("No name",""),
                    color = Color(0xff4287f5),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item{
                Text(
                    viewModel.record.value?.date.placeValue("Date??",""),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item{
                Spacer(Modifier.height(12.dp))
            }
            item{
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Divider(
                        color = Color.LightGray,
                        thickness = 0.5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        viewModel.record.value?.type.placeValue("record","").replaceFirstChar { it.uppercase() },
                        color = Color(0xff009c08),
                        fontWeight = FontWeight.W300,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Divider(
                        color = Color.LightGray,
                        thickness = 0.5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            item{
                Spacer(Modifier.height(12.dp))
            }
            items(viewModel.record.value?.attachments?: listOf()){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 8.dp
                ){
                    AsyncImage(
                        model = it,
                        contentDescription = "Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(),
                        filterQuality = FilterQuality.High
                    )
                }
            }
        }
    }



    @Composable
    private fun HeaderUI(viewModel: MedicalRecordDetailsViewModel) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = {
                    finish()
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Text(
                    "Record Details",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(
                    modifier = Modifier.width(32.dp)
                )
            }
            Divider(
                color = Color.LightGray
            )
        }
    }
}