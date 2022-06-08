package com.hellomydoc.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hellomydoc.Constants
import com.hellomydoc.AppointmentDetailsViewModel
import com.hellomydoc.PageState
import com.hellomydoc.R
import com.hellomydoc.activities.ui.theme.HelloMyDoc2Theme
import com.hellomydoc.data.AppointmentData
import com.hellomydoc.data.PrescribedMedicine
import com.hellomydoc.views.AppointmentTypeView

class AppointmentDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: AppointmentDetailsViewModel by viewModels()
        if(savedInstanceState==null){
            val v = intent?.getSerializableExtra(Constants.APPOINTMENT)
            viewModel.setAppointmentData((v as? AppointmentData)?:return)
        }
        setContent {
            HelloMyDoc2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    PrescriptionDetailsUI(viewModel)
                }
            }
        }
    }

    @Composable
    private fun PrescriptionDetailsUI(viewModel: AppointmentDetailsViewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            HeaderUi(viewModel)
            BodyUi(viewModel)
        }
    }

    @Composable
    private fun BodyUi(viewModel: AppointmentDetailsViewModel) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ){
            item{
                PatientNameUI(viewModel)
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item{
                AppointmentDataUI(viewModel)
            }
            item{
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
            if(viewModel.hasPrescription){
                item{
                    Box(){
                        Text(
                            "Prescription"
                        )
                    }
                }
                item{
                    Divider(
                        color = Color.Gray,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                when(viewModel.prescriptionLoadState.value){
                    PageState.INITIAL -> {}
                    PageState.LOADING -> item{
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            CircularProgressIndicator()
                        }
                    }
                    PageState.SUCCESS -> {}

                    PageState.EXCEPTION,
                    PageState.FAILED,
                    PageState.ERROR -> item{
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            TextButton(onClick = {
                                viewModel.fetchPrescription()
                            }) {
                                Text(
                                    "Try Again",
                                    color = Color(0xff036bfc)
                                )
                            }
                        }
                    }
                    PageState.HAS_DATA -> {
                        if(viewModel.prescription.value?.medicines?.isNotEmpty()==true){
                            item{
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        "Medicines",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                            items(viewModel.prescription.value?.medicines?:return@LazyColumn){
                                MedicineContent(it)
                            }
                        }
                        else{
                            item{
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        "No medicine prescribed",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        if(viewModel.prescription.value?.labTests?.isNotEmpty()==true){
                            item{
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        "Lab tests",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                            items(viewModel.prescription.value?.labTests?:return@LazyColumn){
                                Text(
                                    it.uppercase(),
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else{
                            item{
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        "No medicine prescribed",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else{
                item{
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            "Not prescribed yet",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
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
    private fun AppointmentDataUI(viewModel: AppointmentDetailsViewModel) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Appointment",
                color = Color.DarkGray
            )
            Divider(
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth()
            ){
                IconButton(onClick = {

                }) {
                    Icon(
                        painter = getDrawableIconAsAppointmentType(viewModel.appointmentType),
                        contentDescription = "Appointment Type",
                        tint = Color(0xFF4184FF),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Text(
                    viewModel.appointmentDateTime,
                    textAlign = TextAlign.Right
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable {
                            openDoctorView(viewModel)
                        }
                ) {
                    Card(
                        shape = CircleShape,
                        elevation = 4.dp,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(80.dp)
                    ) {
                        AsyncImage(
                            model = viewModel.doctorImage,
                            contentDescription = "Doctor Image",
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.End
                    ){
                        Text(
                            viewModel.doctorName,
                            color = Color(0xff036bfc),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(viewModel.doctorSpeciality)
                    }
                }
            }
        }
    }

    private fun openDoctorView(viewModel: AppointmentDetailsViewModel) {
        startActivity(
            Intent(this,DoctorDetailsActivity::class.java).apply {
                val doctorId = viewModel.doctorId
                val doctorName = viewModel.doctorName
                putExtra(Constants.DOCTOR_ID_KEY,doctorId)
                putExtra(Constants.DOCTOR_NAME_KEY,doctorName)
            }
        )
    }

    private fun getIconAsAppointmentType(appointmentType: String): ImageVector {
        return when(appointmentType){
            AppointmentTypeView.APPOINTMENT_TYPE.VIDEO.name->Icons.Filled.Videocam
            AppointmentTypeView.APPOINTMENT_TYPE.VOICE.name->Icons.Filled.Phone
            else->Icons.Filled.Chat
        }
    }

    @Composable
    private fun getDrawableIconAsAppointmentType(appointmentType: String): Painter {
        return when(appointmentType){
            AppointmentTypeView.APPOINTMENT_TYPE.VIDEO.name-> painterResource(id = R.drawable.ic_video_svgrepo_com)
            AppointmentTypeView.APPOINTMENT_TYPE.VOICE.name-> painterResource(id = R.drawable.ic_phone_call_svgrepo_com)
            else-> painterResource(id = R.drawable.ic_chat_svgrepo_com)
        }
    }

    @Composable
    private fun PatientNameUI(viewModel: AppointmentDetailsViewModel) {
        Text(
            viewModel.patientName,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }

    @Composable
    private fun HeaderUi(viewModel: AppointmentDetailsViewModel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ){
            HeaderCoreUI(viewModel)
            Divider(
                color = Color.LightGray,
                thickness = 0.5.dp
            )
        }
    }

    @Composable
    private fun HeaderCoreUI(viewModel: AppointmentDetailsViewModel) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(65.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            BackButtonUI(viewModel)
            HeaderTextUI(viewModel)
        }
    }

    @Composable
    private fun RowScope.HeaderTextUI(viewModel: AppointmentDetailsViewModel) {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        Text(
            stringResource(R.string.prescription_details),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        Spacer(modifier = Modifier.width(32.dp))
    }

    @Composable
    private fun BackButtonUI(viewModel: AppointmentDetailsViewModel) {
        val context = LocalContext.current
        IconButton(onClick = {
            (context as Activity).finish()
        }) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Back",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}