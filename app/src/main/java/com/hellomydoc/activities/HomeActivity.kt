package com.hellomydoc.activities


import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.chat.presentation.activity.ChatActivity
import com.hellomydoc.databinding.ActivityHomeBinding
import com.hellomydoc.fragments.ConsultationFragment
import com.hellomydoc.fragments.HomeFragment
import com.hellomydoc.fragments.MyBookingsFragment
import com.hellomydoc.fragments.ProfileFragment
import kotlinx.coroutines.launch
import java.util.*

class HomeActivity : AbstractActivity() {
    private val ageGenderDialog = mutableStateOf(true)
    enum class PAGE{
        PROFILE,
        MEDICAL_HISTORY,
        VIDEO,
        CHAT,
        ADD_PATIENT_DETAILS,
        CONSULTATIONS,
        BOOKINGS,
        HOME,
        SPLASH,
        NOTIFICATIONS,
        MEMBERS,
        PRESCRIPTIONS,
        PRESCRIPTION,
    }

    data class ChildCallback(
        val goToPage: (PAGE,Bundle?)->Unit
    )

    private val childCallback = ChildCallback(
        goToPage = {it,bundle->
            onGoToPageRequested(it,bundle)
        }
    )

    private fun onGoToPageRequested(page: PAGE,bundle: Bundle?) {
        when(page){
            PAGE.ADD_PATIENT_DETAILS -> {
                navi()
                    .target(AddPatientDetailsActivity::class.java)
                    .finish(false)
                    ?.go()
            }
            PAGE.CHAT -> {
                navi()
                    .target(ChatActivity::class.java)
                    .bundle(bundle)
                    ?.finish(false)
                    ?.go()
            }
            PAGE.VIDEO -> {
                navi()
                    .target(VideoCallingActivity::class.java)
                    .bundle(bundle)
                    ?.finish(false)
                    ?.go()
            }
            PAGE.CONSULTATIONS-> {
                binding.bottomNavigation.selectedItemId = R.id.bottom_menu_consultation
            }
            PAGE.BOOKINGS-> {
                binding.bottomNavigation.selectedItemId = R.id.bottom_menu_my_bookings
            }
            PAGE.HOME-> {
                binding.bottomNavigation.selectedItemId = R.id.bottom_menu_home
            }
            PAGE.SPLASH-> {
                navi()
                    .target(SplashActivity::class.java)
                    .finish(false)
                    ?.back()
            }
            PAGE.PROFILE-> {
                binding.bottomNavigation.selectedItemId = R.id.bottom_menu_profile
            }
            PAGE.NOTIFICATIONS-> {
                navi()
                    .target(NotificationActivity::class.java)
                    .finish(false)
                    ?.go()
            }
            PAGE.MEMBERS-> {
                navi()
                    .target(MembersActivity::class.java)
                    .finish(false)
                    ?.go()
            }
            PAGE.MEDICAL_HISTORY -> {
                navi()
                    .target(MedicalHistoryActivity::class.java)
                    .finish(false)
                    ?.go()
            }
            PAGE.PRESCRIPTIONS -> {
                navi()
                    .target(PrescriptionsActivity::class.java)
                    .finish(false)
                    ?.go()
            }
            PAGE.PRESCRIPTION -> {
                navi()
                    .target(AppointmentDetailsActivity::class.java)
                    .bundle(bundle)
                    ?.finish(false)
                    ?.go()
            }
        }
    }

    private var drawerOnSelectedListener = NavigationView.OnNavigationItemSelectedListener {
        if(binding.dlSideDrawer.isOpen){
            binding.dlSideDrawer.closeDrawer(GravityCompat.START)
        }
        return@OnNavigationItemSelectedListener onDrawerSelected(it.itemId,true)
    }

    private var bottomNavigationOnSelectedListener = NavigationBarView.OnItemSelectedListener {
        return@OnItemSelectedListener onDrawerSelected(it.itemId, false)
    }

    private fun onDrawerSelected(id: Int, fromDrawer: Boolean): Boolean {
        try {
            releaseNavigationMenuListener()
            binding.navMenu.setCheckedItem(id)
            binding.bottomNavigation.selectedItemId = id
            return true
        } finally {
            applyMenuSelected(id,fromDrawer)
            setupNavigationMenuListener()
        }
    }

    private fun setupNavigationMenuListener() {
        binding.navMenu.setNavigationItemSelectedListener(drawerOnSelectedListener)
        binding.bottomNavigation.setOnItemSelectedListener(bottomNavigationOnSelectedListener)
    }

    var onDrawerClosedEvent: (()->Unit)? = null
    private fun applyMenuSelected(id: Int, fromDrawer: Boolean) {
        if(fromDrawer){
            onDrawerClosedEvent = {
                onDrawerClosedEvent = null
                when(id){
                    R.id.bottom_menu_consultation->replaceToConsultationsFragment()
                    R.id.bottom_menu_home->replaceToHomeFragment()
                    R.id.bottom_menu_my_bookings->replaceToMyBookingsFragment()
                    R.id.bottom_menu_profile->replaceToProfileFragment()
                    R.id.menu_notifications-> goToNotificationsPage()
                    R.id.menu_medical_record-> goToMedicalRecordsPage()
                }
            }
        }
        else{
            when(id){
                R.id.bottom_menu_consultation->replaceToConsultationsFragment()
                R.id.bottom_menu_home->replaceToHomeFragment()
                R.id.bottom_menu_my_bookings->replaceToMyBookingsFragment()
                R.id.bottom_menu_profile->replaceToProfileFragment()
                R.id.menu_notifications-> goToNotificationsPage()
                R.id.menu_medical_record-> goToMedicalRecordsPage()
            }
        }
    }

    private fun goToMedicalRecordsPage() {
        navi().target(MedicalRecordsActivity::class.java).finish(false)?.go()
    }

    private fun goToNotificationsPage() {
        navi().target(NotificationActivity::class.java).finish(false)?.go()
    }

    private fun releaseNavigationMenuListener() {
        binding.navMenu.setNavigationItemSelectedListener(null)
        binding.bottomNavigation.setOnItemSelectedListener(null)
    }

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        setupAgeAndGenderDialog()

        binding.dlSideDrawer.addDrawerListener(object: DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.d("drawer_state","slide=$slideOffset")
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.d("drawer_state","opened")
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.d("drawer_state","closed")
            }

            override fun onDrawerStateChanged(newState: Int) {
                Log.d("drawer_state","$newState")
                onDrawerClosedEvent?.invoke()
            }

        })

        binding.ivNotification.setOnClickListener {
            goToNotificationsPage()
        }

        setupNavigationMenuListener()

        binding.ivMenu.setOnClickListener {
            if(!binding.dlSideDrawer.isOpen){
                binding.dlSideDrawer.open()
            }
        }

        replaceToHomeFragment()

        /*binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bottom_menu_home-> {
                    replaceToHomeFragment()
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_menu_consultation->{
                    replaceToConsultationsFragment()
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_menu_my_bookings->{
                    replaceToMyBookingsFragment()
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_menu_profile->{
                    replaceToProfileFragment()
                    return@setOnItemSelectedListener true
                }
            }
            return@setOnItemSelectedListener false
        }*/
    }

    private fun setupAgeAndGenderDialog() {
        if(!repository.getAgeAndGenderOk()){
            binding.cvContent.setContent {
                val canUpdate = remember { mutableStateOf(false) }
                val genderIconSize = 48
                val genderState = remember { mutableStateOf(-1) }
                val red = colorResource(id = R.color.red)
                val dateDelimeter = "-"
                val calendarOpenState = remember { mutableStateOf(false) }
                val ageStringState = remember { mutableStateOf("") }
                val dobString = remember { mutableStateOf("") }
                fun validate(){
                    canUpdate.value = (dobString.value.isNotEmpty()||ageStringState.value.isNotEmpty())&&genderState.value > -1
                }
                if(ageGenderDialog.value){
                    Dialog(onDismissRequest = { ageGenderDialog.value = false }) {
                        if(calendarOpenState.value){
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight()
                                .background(color = Color.White)) {
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    factory = {
                                        DatePicker(it).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                            )
                                        }
                                    },
                                    update = {views->
                                        val dob = dobString.value
                                        val parts = dob.split(dateDelimeter)
                                        val c = Calendar.getInstance()
                                        var y = c.get(Calendar.YEAR)
                                        var m = c.get(Calendar.MONTH)
                                        var d = c.get(Calendar.DATE)
                                        if(parts.size==3){
                                            y = parts[0].toInt()
                                            m = parts[1].toInt() - 1
                                            d = parts[2].toInt()
                                        }

                                        views.init(y,m,d,object: DatePicker.OnDateChangedListener{
                                            override fun onDateChanged(
                                                view: DatePicker?,
                                                year: Int,
                                                monthOfYear: Int,
                                                dayOfMonth: Int
                                            ) {
                                                dobString.value = "$year$dateDelimeter${leftPad(monthOfYear+1)}$dateDelimeter${leftPad(dayOfMonth)}"
                                                calendarOpenState.value = false
                                                ageStringState.value = ""
                                                validate()
                                            }

                                        })
                                    }
                                )
                                IconButton(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    onClick = {
                                        calendarOpenState.value = false
                                    }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        tint = Color.White,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        }
                        else{
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentHeight(),
                                backgroundColor = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)) {
                                    Text(
                                        "Age and Gender",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()) {
                                        TextField(
                                            placeholder = {
                                                Text("Age")
                                            },
                                            value =ageStringState.value,
                                            onValueChange ={
                                                if(it.length<4){
                                                    val value = try {
                                                        it.toInt()
                                                    } catch (e: Exception) {
                                                        0
                                                    }
                                                    if(value<181){
                                                        ageStringState.value = it
                                                        dobString.value = ""
                                                        validate()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            colors = TextFieldDefaults.textFieldColors(
                                                backgroundColor = Color.White,
                                                unfocusedIndicatorColor = Color.Gray,
                                                focusedIndicatorColor = colorResource(id = R.color.red)
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            )
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text("Or",
                                            modifier = Modifier
                                                .height(IntrinsicSize.Min)
                                                .align(Alignment.CenterVertically))
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally){
                                            IconButton(onClick = {
                                                calendarOpenState.value = true
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.EditCalendar,
                                                    tint = colorResource(id = R.color.red),
                                                    contentDescription = "DOB"
                                                )
                                            }
                                            Text("DOB\n"+dobString.value,
                                                textAlign = TextAlign.Center,
                                                style = TextStyle(
                                                    color = colorResource(id = R.color.red),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.CenterHorizontally),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ){
                                        IconButton(onClick = {
                                            genderState.value = 0
                                            validate()
                                        }) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally){
                                                Icon(
                                                    modifier = Modifier.size(genderIconSize.dp),
                                                    imageVector = Icons.Filled.Male,
                                                    tint = if(genderState.value==0) red else Color.Gray,
                                                    contentDescription = "Male"
                                                )
                                                Text("Male",
                                                    style = TextStyle(
                                                        fontWeight = if(genderState.value==0) FontWeight.Bold else FontWeight.Normal,
                                                        color = if(genderState.value==0) red else Color.Gray
                                                    ))
                                            }


                                        }
                                        IconButton(onClick = {
                                            genderState.value = 1
                                            validate()
                                        }) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally){
                                                Icon(
                                                    modifier = Modifier.size(genderIconSize.dp),
                                                    imageVector = Icons.Filled.Female,
                                                    tint = if(genderState.value==1) red else Color.Gray,
                                                    contentDescription = "Female"
                                                )
                                                Text("Female",
                                                    style = TextStyle(
                                                        fontWeight = if(genderState.value==1) FontWeight.Bold else FontWeight.Normal,
                                                        color = if(genderState.value==1) red else Color.Gray
                                                    ))
                                            }
                                        }
                                        IconButton(onClick = {
                                            genderState.value = 2
                                            validate()
                                        }) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally){
                                                Icon(
                                                    modifier = Modifier.size(genderIconSize.dp),
                                                    imageVector = Icons.Filled.Person,
                                                    tint = if(genderState.value==2) red else Color.Gray,
                                                    contentDescription = "Other"
                                                )
                                                Text(
                                                    "Other",
                                                    style = TextStyle(
                                                        fontWeight = if(genderState.value==2) FontWeight.Bold else FontWeight.Normal,
                                                        color = if(genderState.value==2) red else Color.Gray
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            updateAgeAndGender(ageStringState.value,dobString.value,genderState.value)
                                            ageGenderDialog.value = false
                                        },
                                        enabled = canUpdate.value,
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = red,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Update")
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun updateAgeAndGender(age: String, dob: String, gender: Int) {
        var g = when(gender){
            0->"male"
            1->"female"
            else->"other"
        }
        lifecycleScope.launch {
            wait = true
            processApi {
                repository.updateAgeAndGender(age,dob,g).resp
            }.apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                R.string.age_and_gender_updated_successfully.string.toast(this@HomeActivity)
                                repository.setAgeAndGenderOk(success)
                            }
                            else{
                                message.toast(this@HomeActivity)
                            }
                        }
                    }
                    else->{
                        //R.string.something_went_wrong.string.toast(this@HomeActivity)
                    }
                }
            }
            wait = false
        }
    }

    private fun leftPad(i: Int): String {
        if(i<10){
            return "0$i"
        }
        else{
            return "$i"
        }
    }

    private fun replaceToHomeFragment(){
        val fm = supportFragmentManager
        fm.commit {
            replace(R.id.fragmentContainerView,HomeFragment.newInstance(childCallback))
        }
    }

    private fun replaceToConsultationsFragment(){
        val fm = supportFragmentManager
        fm.commit {
            replace(R.id.fragmentContainerView,ConsultationFragment.newInstance(childCallback))
        }
    }

    private fun replaceToMyBookingsFragment(){
        val fm = supportFragmentManager
        fm.commit {
            replace(R.id.fragmentContainerView,MyBookingsFragment.newInstance(childCallback))
        }
    }

    private fun replaceToProfileFragment(){
        val fm = supportFragmentManager
        fm.commit {
            replace(R.id.fragmentContainerView,ProfileFragment.newInstance(childCallback))
        }
    }

    var lastBackPressed = -1L
    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if(lastBackPressed==-1L){
            lastBackPressed = now
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        else{
            val dif = now - lastBackPressed
            lastBackPressed = -1
            if(dif<2000){
                super.onBackPressed()
            }
            else{
                lastBackPressed = now
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }
}