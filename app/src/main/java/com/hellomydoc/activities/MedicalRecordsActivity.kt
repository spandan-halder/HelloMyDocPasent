package com.hellomydoc.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.data.MedicalRecord
import com.hellomydoc.databinding.ActivityMedicalRecordsBinding
import kotlinx.coroutines.launch

class MedicalRecordsActivity : AbstractActivity() {
    private var loading = false
    private lateinit var binding: ActivityMedicalRecordsBinding
    private var records: List<MedicalRecord> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            navi().back()
        }


    }

    override fun onResume() {
        super.onResume()
        fetchRecords()
    }

    private fun fetchRecords() {
        loading = true
        showRecords()
        lifecycleScope.launch {
            processApi {
                repository.medicalRecords().resp
            }
            .apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                this@MedicalRecordsActivity.records = records
                            }
                        }
                    }
                    ApiDispositionStatus.EXCEPTION -> {
                        this@MedicalRecordsActivity.records = listOf()
                        R.string.something_went_wrong.string.toast(this@MedicalRecordsActivity)
                        Log.d("records_exception",exception.toString())
                    }
                    else->{
                        this@MedicalRecordsActivity.records = listOf()
                        //R.string.something_went_wrong.string.toast(this@MedicalRecordsActivity)
                    }
                }
                loading = false
                showRecords()
            }
        }
    }

    private fun showRecords() {
        binding.cvContent.setContent {
            if(loading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    CircularProgressIndicator(color = colorResource(id = R.color.red))
                }
            }
            else{
                if(records.isEmpty()){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Column(modifier = Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                ImageVector.vectorResource(
                                    id = R.drawable.ic_medical_record
                                ),
                                "Localized description",
                                modifier = Modifier.size(180.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(R.string.add_a_medical_record.string, style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(R.string.detailed_health_record_message.string)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { addNewRecord() },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color.Black)
                            ) {
                                Text(
                                    R.string.add_a_record.string.uppercase(),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                else{
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)){
                        LazyColumn(modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)){
                            items(records){record->
                                RecordUI(record)
                            }
                            item{
                                Spacer(Modifier.height(60.dp))
                            }
                        }
                        Button(onClick = {
                            addNewRecord()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(id = R.color.red),
                            contentColor = Color.White
                        ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .align(
                                    Alignment.BottomCenter
                                )
                            ) {
                            Text(R.string.add_a_record.string.uppercase(),
                                modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                }
            }
        }
    }

    @Composable
    private fun RecordUI(record: MedicalRecord) {
        Card(
            modifier =
            Modifier
                .padding(vertical = 10.dp)
                .clickable {
                    openRecordDetails(record)
                }
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            elevation = 8.dp
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)){
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()){
                    RecordDateUI(record)
                    Spacer(modifier = Modifier.width(24.dp))
                    RecordCoreUI(record)
                }
                RecordMenuUI(record)
            }
        }
    }

    private fun openRecordDetails(record: MedicalRecord) {
        startActivity(
            Intent(this,MedicalRecordDetailsActivity::class.java)
                .apply {
                    putExtra(Constants.MEDICAL_RECORD,record)
                }
        )
    }

    @Composable
    private fun BoxScope.RecordMenuUI(record: MedicalRecord) {
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = {

            }) {
            Icon(

                imageVector = Icons.Default.MoreVert,
                tint = Color.Gray,
                contentDescription = "Menu")
        }
    }

    @Composable
    private fun RowScope.RecordCoreUI(record: MedicalRecord) {
        Column(modifier = Modifier
            .fillMaxSize()
            .align(alignment = Alignment.CenterVertically)) {
            val self = record.user_id == repository.userUid
            Text(
                if(self) getString(R.string.records_added_by_you) else record.user_id?:"",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                record.patientName?:"Patient",
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${record.attachments?.size?:0} ${record.type?.uppercase()?:"Record"}(s)",
                style = TextStyle(
                    color = colorResource(id = R.color.red),
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun RecordDateUI(record: MedicalRecord) {
        Column {
            Card(modifier = Modifier
                .width(60.dp)
                .height(65.dp)
                ,
                backgroundColor = colorResource(
                    id = R.color.red
                ),
                shape = RoundedCornerShape(8.dp)){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val ts = Dt.format(Dt.DATE_DB_DATE_TIME,"dd\nMMM\nyyyy",record.timestamp).split("\n")
                        Text(
                            ts[0],
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            ts[1],
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "NEW",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.red),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .width(60.dp)
                    .clip(shape = RoundedCornerShape(2.dp))
                    .background(
                        color = colorResource(id = R.color.very_light_red)
                    ),
                textAlign = TextAlign.Center,
            )
        }
    }

    private fun addNewRecord() {
        navi().target(AddMedicalRecordActivity::class.java).finish(false)?.go()
    }
}