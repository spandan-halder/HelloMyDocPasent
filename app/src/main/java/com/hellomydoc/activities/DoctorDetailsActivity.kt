package com.hellomydoc.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.activities.ui.theme.HelloMyDoc2Theme
import com.hellomydoc.data.doctorDetails.Speciality

class DoctorDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: DoctorDetailsViewModel by viewModels()
        if(savedInstanceState==null){
            intent?.apply {
                val doctorId = getStringExtra(Constants.DOCTOR_ID_KEY)?:return
                val doctorName = getStringExtra(Constants.DOCTOR_NAME_KEY)?:return
                viewModel.setDoctor(doctorId,doctorName)
            }
        }
        setContent {
            HelloMyDoc2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DoctorDetailsUI(viewModel)
                }
            }
        }
    }

    @Composable
    private fun DoctorDetailsUI(viewModel: DoctorDetailsViewModel) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            HeaderUi(viewModel)
            when(viewModel.pageState.value){
                PageState.INITIAL -> {}

                PageState.LOADING -> LoadingUI(viewModel)

                PageState.SUCCESS -> {}
                PageState.EXCEPTION,
                PageState.FAILED,
                PageState.ERROR -> ErrorUI(viewModel)
                PageState.HAS_DATA -> BodyUi(viewModel)
            }

        }

    }

    @Composable
    private fun LoadingUI(viewModel: DoctorDetailsViewModel) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xff2b95ff),
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    @Composable
    private fun ErrorUI(viewModel: DoctorDetailsViewModel) {
        Box(
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    viewModel.fetchDetails()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xff2b95ff),
                    contentColor = Color.White
                )
            ) {
                Text("Try Again")
            }
        }
    }

    @Composable
    private fun BodyUi(viewModel: DoctorDetailsViewModel) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ){
            item{
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Card(
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .fillMaxWidth(0.50f)
                            .aspectRatio(1f),
                        shape = CircleShape
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(viewModel.doctorDetails.value?.profile_pic?:"")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Doctor Image",
                            contentScale = ContentScale.Crop
                        )
                    }
                }

            }
            item{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        viewModel.doctorName.value,
                        color = Color(0xff0384fc),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
            items(viewModel.doctorDetails.value?.specialities?: listOf()){
                SpecialityUI(it)
            }
        }
    }

    @Composable
    private fun SpecialityUI(it: Speciality) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            elevation = 8.dp
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ){
                AsyncImage(
                    model = it.logo?:R.drawable.ic_medicine,
                    contentDescription = "Speciality Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    it.name.uppercase(),
                    color = Color(0xff005eff),
                    fontSize = 18.sp
                )
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f))
                Text(
                    it.exp+" Years",
                    color = Color(0xff18b300),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }

    @Composable
    private fun HeaderUi(viewModel: DoctorDetailsViewModel) {
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
    private fun HeaderCoreUI(viewModel: DoctorDetailsViewModel) {
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
    private fun RowScope.HeaderTextUI(viewModel: DoctorDetailsViewModel) {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        Text(
            "Doctor Details",
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
    private fun BackButtonUI(viewModel: DoctorDetailsViewModel) {
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