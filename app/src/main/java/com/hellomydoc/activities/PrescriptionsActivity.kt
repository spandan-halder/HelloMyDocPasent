package com.hellomydoc.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hellomydoc.R
import com.hellomydoc.data.PrescribedMedicine
import com.hellomydoc.data.Prescription
import com.hellomydoc.dateTimeFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class PrescriptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: PrescriptionsViewModel by viewModels()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                HandleFinish(model)
                UserInterface(model)
                HandleBack(model)
            }
        }
    }
    @Composable
    fun UserInterface(model: PrescriptionsViewModel){
        when(model.currentPage.value){
            PrescriptionPages.LIST -> ListContent(model)
            PrescriptionPages.DETAILS -> DetailsContent(model)
        }
    }

    @Composable
    private fun DetailsContent(model: PrescriptionsViewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            Header("Prescription Details") {
                model.leaveDetails()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ){
                var prescription by remember { mutableStateOf(model.currentPrescription.value?:return@Box) }
                LazyColumn(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                ){
                    item {
                        Text(
                            prescription.patientSummary?:"Not found",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    item{
                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                    item {
                        Text(
                            prescription.patientSymptoms?:"Not found",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    item{
                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                    item{
                        Text(
                            "Given at "+prescription.timestamp.dateTimeFormat("dd-MM-yyyy"),
                            color = Color(0xffB8C3C5),
                            fontSize = 16.sp
                        )
                    }
                    item{
                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )
                    }

                    when(model.detailsState.value){
                        DataState.LOADING -> item {
                            LoadingContent(model)
                        }
                        DataState.IO_FAILED -> item {
                            IoFailedContent{
                                model.fetchPrescriptionDetails(prescription.prescriptionId?:return@IoFailedContent)
                            }
                        }
                        DataState.CHECKING -> item{
                            CheckingContent(model)
                        }
                        DataState.IO_SUCCESS -> {}
                        DataState.TRANSACTION_SUCCESS -> {}
                        DataState.TRANSACTION_FAILED -> item{
                            TransactionFailedContent(block = {
                                    model.fetchPrescriptionDetails(prescription.prescriptionId?:return@TransactionFailedContent)
                                },
                                model.transactionMessage.value
                            )
                        }
                        DataState.DATA -> {
                            item{
                                Text(
                                    "Medicines",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            item{
                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                            if(model.prescriptionDetails.value?.medicines==null||model.prescriptionDetails.value?.medicines?.isEmpty()==true){
                                item{
                                    Text(
                                        "No medicine prescribed",
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            else{
                                items(model.prescriptionDetails.value?.medicines?:return@LazyColumn){
                                    MedicineContent(it)
                                }
                            }
                            //////////////////////////////////////////
                            item{
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            item{
                                Text(
                                    "Lab tests",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            item{
                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                            if(model.prescriptionDetails.value?.labTests==null||model.prescriptionDetails.value?.labTests?.isEmpty()==true){
                                item{
                                    Text(
                                        "No lab test prescribed",
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            else{
                                items(model.prescriptionDetails.value?.labTests?:return@LazyColumn){
                                    Text(
                                        it,
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        DataState.NO_DATA -> item{
                            NoDataContent{
                                model.fetchPrescriptionDetails(prescription.prescriptionId?:return@NoDataContent)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LabTestContent(it: String) {

    }

    @Composable
    private fun MedicineContent(medicine: PrescribedMedicine) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color.White,
            elevation = 4.dp
        ){
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ){
                Text(
                    medicine.name.uppercase(),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Divider(
                    color = Color.LightGray,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        medicine.doseValue.toString(),
                        color = Color(0xff0390fc),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        medicine.doseUnit.uppercase(),
                        color = Color(0xff0390fc),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                Text(
                    when(medicine.frequency){
                        "BDAC"->"Twice a day before meals"
                        "BDPC"->"Twice a day after meals"
                        "ODPC"->"Once in a day after meals"
                        "TDPC"->"Thrice a day after meals"
                        else->medicine.frequency
                    },
                    color = Color(0xff8400ff),
                )
                Divider(
                    color = Color.LightGray,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "For: ${medicine.timePeriodValue} ${medicine.timePeriodUnit}",
                    color = Color.Black,
                )
            }
        }
    }

    @Composable
    private fun ListContent(model: PrescriptionsViewModel) {
        Column{
            Header("Prescriptions"){
                model.onBack()
            }
            when(model.listState.value){
                DataState.LOADING -> LoadingContent(model)
                DataState.IO_FAILED -> IoFailedContent{
                    model.fetchList()
                }
                DataState.CHECKING -> CheckingContent(model)
                DataState.IO_SUCCESS -> {}
                DataState.TRANSACTION_SUCCESS -> {}
                DataState.TRANSACTION_FAILED -> TransactionFailedContent(block = {
                    model.fetchList()
                },
                model.transactionMessage.value
                    )
                DataState.DATA -> PrescriptionContent(model)
                DataState.NO_DATA -> NoDataContent{
                    model.fetchList()
                }
            }
        }
    }

    @Composable
    private fun PrescriptionContent(model: PrescriptionsViewModel) {
        if(model.prescriptions.isEmpty()){
            NoDataContent{
                model.fetchList()
            }
        }
        else{
            Content(model)
        }
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
                NoPrescriptionYet()
                TryAgainButton(block)
            }
        }
    }

    @Composable
    private fun NoPrescriptionYet() {
        Text(
            "No prescription yet",
            color = Color.Gray
        )
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
    private fun CheckingContent(model: PrescriptionsViewModel) {
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
    private fun LoadingContent(model: PrescriptionsViewModel) {
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
    private fun Content(model: PrescriptionsViewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ){
                items(model.prescriptions){
                    PrescriptionItem(model,it)
                }
            }
        }
    }

    @Composable
    private fun PrescriptionItem(model: PrescriptionsViewModel, it: Prescription) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
                .background(Color.White)
        ){
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_prescription_document),
                    contentDescription = "Prescription Icon",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.very_light_red))
                        .padding(20.dp)
                )
                Spacer(
                    modifier = Modifier.width(12.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        it.patientSymptoms?:"Not available",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        it.patientSummary?:"Not available",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                    Text(
                        "Given at "+it.timestamp.dateTimeFormat("dd-MM-yyyy"),
                        color = Color(0xffB8C3C5),
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                    Row(
                        modifier = Modifier
                            .clickable {
                                model.onPrescriptionDetailsClick(it)
                            }
                    ) {
                        Text(
                            "See Prescription",
                            color = colorResource(id = R.color.red)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "See Prescription",
                            tint = colorResource(id = R.color.red)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Header(heading: String, block: ()->Unit){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White)
        ){
            IconButton(
                onClick = block,
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
                heading,
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

    @Composable
    private fun HandleFinish(model: PrescriptionsViewModel) {
        val context = LocalContext.current
        if(model.leave.value){
            LaunchedEffect(key1 = Unit){
                (context as Activity).finish()
            }
        }
    }

    @Composable
    private fun HandleBack(model: PrescriptionsViewModel) {
        BackHandler(enabled = true) {
            model.onBack()
        }
    }
}