package com.hellomydoc.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hellomydoc.R
import com.hellomydoc.data.HistoryItem
import com.hellomydoc.dateTimeFormat

class MedicalHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MedicalHistoryViewModel by viewModels()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                HandleFinish(model)
                MedicalHistoryUserInterFace(model)
                HandleBack(model)
            }
        }
    }

    @Composable
    private fun HandleBack(model: MedicalHistoryViewModel) {
        BackHandler(enabled = true) {
            model.onBack()
        }
    }

    @Composable
    private fun HandleFinish(model: MedicalHistoryViewModel) {
        val context = LocalContext.current
        if(model.leave.value){
            LaunchedEffect(key1 = Unit){
                (context as Activity).finish()
            }
        }
    }

    @Composable
    private fun MedicalHistoryUserInterFace(model: MedicalHistoryViewModel) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            Column(){
                Header(model)
                Content(model)
            }
        }
    }

    @Composable
    private fun Content(model: MedicalHistoryViewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ){
            when(model.historyState.value){
                DataState.LOADING -> LoadingContent(model)
                DataState.IO_FAILED -> IoFailedContent{
                    model.fetchMedicalHistory()
                }
                DataState.CHECKING -> CheckingContent(model)
                DataState.IO_SUCCESS -> {}
                DataState.TRANSACTION_SUCCESS -> {}
                DataState.TRANSACTION_FAILED -> TransactionFailedContent(block = {
                    model.fetchMedicalHistory()
                },
                    model.transactionMessage.value
                )
                DataState.DATA -> HistoryContent(model)
                DataState.NO_DATA -> NoDataContent{
                    model.fetchMedicalHistory()
                }
            }
        }
    }

    @Composable
    private fun HistoryContent(model: MedicalHistoryViewModel) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ){
            items(model.history){
                MedicalHistoryItem(it,model)
            }
        }
    }

    @Composable
    private fun MedicalHistoryItem(it: HistoryItem, model: MedicalHistoryViewModel) {
        when(it.type){
            "prescription"->PrescriptionItem(it,model)
            "record"->RecordItem(it,model)
        }
    }

    @Composable
    private fun RecordItem(it: HistoryItem, model: MedicalHistoryViewModel) {
        Card(
            backgroundColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "RECORD:${it.record?.type?.uppercase()}",
                        color = Color(0xff006eff),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription ="Date Time",
                            tint = Color.DarkGray
                        )
                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )
                        Text(
                            it.record?.timestamp?.dateTimeFormat("dd-MM-yyyy")?:"Not found",
                            color = Color.Gray
                        )
                    }
                }
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LazyRow{
                    items(it.record?.attachments?.split(",")?: emptyList()){
                        if(it.uppercase().endsWith(".PDF")){
                            IconButton(
                                onClick = {
                                    val url = it
                                    val i = Intent(Intent.ACTION_VIEW)
                                    i.data = Uri.parse(url)
                                    startActivity(i)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "PDF",
                                    modifier = Modifier.size(100.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                        else{
                            AsyncImage(
                                model = it,
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                    val url = it
                                    val i = Intent(Intent.ACTION_VIEW)
                                    i.data = Uri.parse(url)
                                    startActivity(i)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PrescriptionItem(it: HistoryItem, model: MedicalHistoryViewModel) {
        Card(
            backgroundColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "PRESCRIPTION",
                        color = Color(0xff006eff),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription ="Date Time",
                            tint = Color.DarkGray
                        )
                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )
                        Text(
                            it.prescription?.timestamp?.dateTimeFormat("dd-MM-yyyy")?:"Not found",
                            color = Color.Gray
                        )
                    }
                }

                Divider(
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    it.prescription?.patientSummary?:"Not found",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                Text(
                    it.prescription?.patientSymptoms?:"Not found",
                    color = Color.Gray
                )
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }
    }

    @Composable
    private fun NoHistoryYet() {
        Text(
            "No history yet",
            color = Color.Gray
        )
    }

    @Composable
    private fun NoDataContent(block: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                NoHistoryYet()
                TryAgainButton(block)
            }
        }
    }

    @Composable
    private fun TryAgainButton(block: () -> Unit) {
        Button(
            onClick = block,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xff4287f5),
                contentColor = Color.White
            )
        ) {
            Text(
                "Try again"
            )
        }
    }

    @Composable
    private fun TransactionFailedContent(block: () -> Unit, message: String) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                TransactionMessageContent(message)
                Spacer(modifier = Modifier.height(12.dp))
                TryAgainButton(block)
            }
        }
    }

    @Composable
    private fun TransactionMessageContent(message: String) {
        Text(
            message,
            color = Color.Black
        )
    }

    @Composable
    private fun CheckingContent(model: MedicalHistoryViewModel) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(
                "Checking...",
                color = Color.Gray
            )
        }
    }

    @Composable
    private fun IoFailedContent(block: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            TryAgainButton(block)
        }
    }

    @Composable
    private fun LoadingContent(model: MedicalHistoryViewModel) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            CircularProgressIndicator(
                color = colorResource(id = R.color.red),
                strokeWidth = 1.dp,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
    }

    @Composable
    private fun Header(model: MedicalHistoryViewModel) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White)
        ){
            IconButton(
                onClick = {
                    model.onBack()
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                "Medical History",
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(
            thickness = 0.5.dp,
            color = Color.LightGray
        )
    }
}

