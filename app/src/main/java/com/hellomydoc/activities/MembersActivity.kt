package com.hellomydoc.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import coil.transform.Transformation
import com.anggrayudi.storage.SimpleStorageHelper
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.activities.ui.theme.HelloMyDoc2Theme
import com.hellomydoc.data.FileAttachment
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Years
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MembersActivity : ComponentActivity() {
    enum class MyMode{
        LIST,
        ADD,
        EDIT
    }
    enum class ProfileSource{
        BITMAP,
        URL
    }

    private var currentEditingUser: User? = null
    private var currentImage: Any? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_FILE = 3
    private val storageHelper = SimpleStorageHelper(this)

    val askDeleteState = mutableStateOf(false)
    val deleting = mutableStateOf(false)
    val nameState = mutableStateOf("")
    val emailState = mutableStateOf("")
    val mobileState = mutableStateOf("")
    val uploading = mutableStateOf(false)
    val profileImageUrl = mutableStateOf("")
    val profileSource = mutableStateOf(ProfileSource.BITMAP)
    val profileBitmap = mutableStateOf<Bitmap?>(null)
    val askSourceOpen = mutableStateOf(false)
    val mode = mutableStateOf(MyMode.LIST)
    val members = mutableStateListOf<User>()
    val loading = mutableStateOf(true)
    val calendarOpenState = mutableStateOf(false)
    val dobStringState = mutableStateOf("")
    val ageGenderDialog = mutableStateOf(false)
    val genderState = mutableStateOf(-1)
    val ageStringState = mutableStateOf("")
    val ageGenderDialogDoneAllowed = mutableStateOf(false)
    val canUpdate = mutableStateOf(false)

    private fun onFileSelected(files: List<DocumentFile>) {
        val t = files.first()
        currentImage = t
        if (contentResolver != null) {
            var image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var s = ImageDecoder.createSource(contentResolver, t.uri)
                ImageDecoder.decodeBitmap(s)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, t.uri)
            }
            onImageObtained(image)
        }
    }

    private fun onImageObtained(image: Bitmap) {
        profileBitmap.value = image
        validate()
    }

    fun validate(){
        val valuesOk = (
                (profileBitmap.value!=null||mode.value==MyMode.EDIT)
                        &&nameState.value.isNotEmpty
                        &&emailState.value.isEmail
                        &&mobileState.value.isMobile
                        &&(dobStringState.value.isNotEmpty||ageStringState.value.isNotEmpty)
                        &&genderState.value > -1
                )
        canUpdate.value =  valuesOk
    }

    private fun setupStorageHelperCallback() {
        storageHelper.onFileSelected = { requestCode, files ->
            when (requestCode) {
                REQUEST_FILE -> {
                    onFileSelected(files)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloMyDoc2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Content()
                }
            }
        }
        storageHelper.onFileSelected = { requestCode, files ->
            when (requestCode) {
                REQUEST_FILE -> {
                    onFileSelected(files)
                }
            }
        }
        fetchMembers()
    }

    private fun fetchMembers() {
        lifecycleScope.launch {
            processApi {
                repository.getMembers().resp
            }.apply {
                when (status) {
                    ApiDispositionStatus.RESPONSE -> {
                        response?.apply {
                            if (success) {
                                this@MembersActivity.members.clear()
                                this@MembersActivity.members.addAll(members)
                            }
                        }
                    }
                }
                loading.value = false
            }
        }
    }

    @Composable
    fun Content() {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)){
            deleteButton()
            backButton()
            heading()
            when(mode.value){
                MyMode.LIST -> {
                    mainContent()
                    addMemberButton()
                }
                else -> addOrEditMemberContent()
            }
        }
    }

    @Composable
    private fun BoxScope.deleteButton() {
        if(mode.value==MyMode.EDIT){
            if(deleting.value){
                CircularProgressIndicator(
                    color = colorResource(id = R.color.red),
                    strokeWidth = 1.dp,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                )
            }
            else{
                IconButton(
                    onClick = {
                        askDeleteState.value = true
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        tint = colorResource(id = R.color.red),
                        contentDescription = "Delete"
                    )
                }
            }
            if(askDeleteState.value){
                Dialog(onDismissRequest = {

                }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ){
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(12.dp)){
                            Text(
                                "Delete",
                                style = TextStyle(
                                    color = colorResource(id = R.color.red),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Are you sure to delete the member?",
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth()){
                                Button(
                                    onClick = {
                                        askDeleteState.value = false
                                        deleteMember()
                                              },
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = colorResource(id = R.color.red),
                                        backgroundColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    Text(
                                        "Yes"
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { askDeleteState.value = false },
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color.White,
                                        backgroundColor = colorResource(id = R.color.red)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    Text(
                                        "No"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deleteMember() {
        canUpdate.value = false
        deleting.value = true
        lifecycleScope.launch {
            uploading.value = true
            processApi {
                repository.deleteMember(currentEditingUser?.user_id?:"").resp
            }.apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                R.string.member_deleted_successfully.string.toast(this@MembersActivity)
                                resetModeToList()
                            }
                            else{
                                message.toast(this@MembersActivity)
                            }
                        }
                    }
                    else->{
                        R.string.something_went_wrong.string.toast(this@MembersActivity)
                    }
                }
                deleting.value = false
            }
        }
    }

    @Composable
    private fun askDialog() {
        if (askSourceOpen.value) {
            Dialog(
                onDismissRequest = {
                    askSourceOpen.value = false
                },

                ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(12.dp)
                    ) {
                        Spacer(modifier = Modifier.size(24.dp))
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            fromCameraButton()
                            fromGalleryButton()
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        askCancelButton()
                    }
                }
            }
        }
    }

    @Composable
    private fun askCancelButton() {
        Button(
            onClick = {
                askSourceOpen.value = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                "Cancel"
            )
        }
    }

    private fun fromGallery() {
        storageHelper.openFilePicker(REQUEST_FILE, false, "image/*")
    }

    @Composable
    private fun fromGalleryButton() {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = {
                askSourceOpen.value = false
                fromGallery()
            }) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    tint = colorResource(id = R.color.red),
                    imageVector = Icons.Filled.Folder,
                    contentDescription = "From Gallery"
                )
            }
            Text(
                "From Gallery",
                style = TextStyle(
                    color = colorResource(id = R.color.red)
                )
            )
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                fromCamera()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun fromCamera() {
        /*val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }*/

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA)
            }
        }
    }

    private fun onImageCaptured(imageBitmap: Bitmap) {
        currentImage = imageBitmap
        onImageObtained(imageBitmap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var d = data?.data
        var c = data?.clipData
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            onImageCaptured(imageBitmap)
        }
        /*if ((requestCode == REQUEST_FILE) && resultCode == AppCompatActivity.RESULT_OK) {
            if(d!=null){
                onFileSelected(d)
            }
            if(c!=null){
                val uris = mutableListOf<Uri>()
                for (i in 0 until c.itemCount) {
                    val uri = c.getItemAt(i).uri
                    uris.add(uri)
                }
                attachments.addAll(uris)
                validateCanUpload()
            }
        }*/
    }

    @Composable
    private fun fromCameraButton() {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = {
                askSourceOpen.value = false
                fromCamera()
            }) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    tint = colorResource(id = R.color.red),
                    imageVector = Icons.Filled.Camera,
                    contentDescription = "From Camera"
                )
            }
            Text(
                "From Camera",
                style = TextStyle(
                    color = colorResource(id = R.color.red)
                )
            )
        }
    }

    @Composable
    private fun profileImageFromSelected() {
        Image(
            bitmap = profileBitmap.value!!.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(135.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }

    @Composable
    private fun profileImageFromNetwork(src: String) {
        Image(
            painter = rememberImagePainter(
                src,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.ic_user_svgrepo_com)
                    transformations(CircleCropTransformation())
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(135.dp)
                .clip(CircleShape)
        )
    }

    @Composable
    private fun BoxScope.profileEditBadge() {
        Card(
            modifier =
            Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd),
            shape = CircleShape,
            border = BorderStroke(3.dp, Color.White),
            backgroundColor = Color(0xFF4F6266)

        ) {
            Icon(
                modifier = Modifier.padding(10.dp),
                imageVector = Icons.Filled.Edit,
                tint = Color.White,
                contentDescription = "Edit"
            )
        }
    }

    @Composable
    fun ageAndGenderSection(){
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier
            .clickable {
                if (mode.value == MyMode.ADD) {
                    ageGenderDialog.value = true
                }
            }
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 12.dp),
            elevation = 4.dp
        ){
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                val ageText = if(dobStringState.value.isEmpty&&ageStringState.value.isEmpty){
                    "Age"
                } else if(dobStringState.value.isNotEmpty&&dobStringState.value!="0000-00-00"){
                    "DOB: ${dobStringState.value}"
                } else if(ageStringState.value.isNotEmpty){
                    "${ageStringState.value} Year(s)"
                } else{
                    "Age"
                }
                val genderText = when(genderState.value){
                    0->"MALE"
                    1->"FEMALE"
                    2->"OTHER"
                    else->"Gender"
                }
                Text(
                    ageText,
                    style = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(genderText)
            }
        }
    }

    @Composable
    private fun addOrEditMemberContent() {
        val red = colorResource(id = R.color.red)
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
        ){
            LazyColumn{
                item{
                    Spacer(modifier = Modifier.height(60.dp))
                }
                item{
                    Box(modifier = Modifier.fillMaxWidth()){
                        Box(modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() } // This is mandatory
                            ) {
                                askSourceOpen.value = true
                            }
                            .align(Alignment.Center)
                        ) {
                            if (profileBitmap.value == null) {
                                if(profileImageUrl.value.isEmpty){
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_user_svgrepo_com),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(135.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                else{
                                    Box(
                                        modifier = Modifier
                                            .size(135.dp)
                                            .clip(CircleShape)
                                    ){
                                        AsyncImage(
                                            model = profileImageUrl.value,
                                            contentDescription = "Member Profile image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                }
                            } else {
                                profileImageFromSelected()
                            }
                            profileEditBadge()
                        }
                    }
                }
                item{
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    inputField("Name",nameState.value){
                        nameState.value = it
                        validate()
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    inputField("Email",emailState.value){
                        emailState.value = it
                        validate()
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    inputField("Mobile",mobileState.value){
                        mobileState.value = it
                        validate()
                    }
                }
                item {
                    ageAndGenderSection()
                }
            }

            Button(
                enabled = getCanSubmit(),
                onClick = {
                    submitUserDetails()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = red,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row {
                    if(uploading.value){
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.red),
                            strokeWidth = 1.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(if(uploading.value) "Processing..." else "Submit")
                }
            }

            askDialog()
        }
        if(ageGenderDialog.value){
            ageGenderDialog()
        }
    }

    private fun getCanSubmit(): Boolean {
        val b = canUpdate.value&&!uploading.value
        return b
    }

    private fun newFileName(ext: String): String {
        val base = System.currentTimeMillis().toString()
        return if (ext.isEmpty()) {
            base
        } else {
            "$base.$ext"
        }
    }

    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(getExternalFilesDir(null).toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    private fun fileAttachmentFrom(item: Any?): FileAttachment? {
        if (item == null) {
            return null
        }
        if (item is String) {
            return null
        }
        var contentResolver = contentResolver ?: return null
        try {
            if (item is Uri) {
                val file = item.toFile()
                val name = item.fileName(this)
                val mimeType = contentResolver.getType(item) ?: ""
                return FileAttachment(item, file, name, mimeType)
            } else if (item is Bitmap) {
                val name = newFileName("png")
                val file = bitmapToFile(item, name)
                val mimeType = "image/png"
                if (file != null) {
                    return FileAttachment(null, file, name, mimeType)
                }
            } else if (item is DocumentFile) {
                val name = item.name ?: ""
                val file = item.file(folderToSave() + File.separator + name, this)
                val mimeType = item.type ?: ""
                if (file != null) {
                    return FileAttachment(null, file, name, mimeType)
                }
            }
        } catch (e: Exception) {
            Log.d("uri_to_attachment", "${e.message}")
        }
        return null
    }

    private fun folderToSave(): String {
        return cacheDir.toString()
    }

    private fun submitUserDetails() {
        val f = fileAttachmentFrom(currentImage)
        val name = nameState.value
        val email = emailState.value
        val mobile = mobileState.value
        val age = ageStringState.value
        val dob = dobStringState.value
        val gender = when(genderState.value){
            0->"male"
            1->"female"
            else->"other"
        }
        uploadUserData(f,name,email,mobile,age,dob,gender)
    }

    private fun uploadUserData(
        f: FileAttachment?,
        name: String,
        email: String,
        mobile: String,
        age: String,
        dob: String,
        gender: String
    ) {
        if(mode.value==MyMode.ADD){
            if(f!=null){
                lifecycleScope.launch {
                    uploading.value = true
                    processApi {
                        repository.addNewMember(f,name,email,mobile,age,dob,gender).resp
                    }.apply {
                        when(status){
                            ApiDispositionStatus.RESPONSE ->{
                                response?.apply {
                                    if(success){
                                        R.string.member_added_successfully.string.toast(this@MembersActivity)
                                        resetModeToList()
                                    }
                                    else{
                                        message.toast(this@MembersActivity)
                                    }
                                }
                            }
                            else->{
                                R.string.something_went_wrong.string.toast(this@MembersActivity)
                            }
                        }
                        uploading.value = false
                    }
                }
            }
        }
        else{
            lifecycleScope.launch {
                uploading.value = true
                processApi {
                    repository.updateMember(f,currentEditingUser?.user_id?:"",name,email,mobile).resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    R.string.member_updated_successfully.string.toast(this@MembersActivity)
                                    resetModeToList()
                                }
                                else{
                                    message.toast(this@MembersActivity)
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(this@MembersActivity)
                        }
                    }
                    uploading.value = false
                }
            }
        }
    }

    private fun resetModeToList() {
        currentImage = null
        mode.value = MyMode.LIST
        uploading.value = false
        profileBitmap.value = null
        askSourceOpen.value = false
        calendarOpenState.value = false
        dobStringState.value = ""
        ageGenderDialog.value = false
        genderState.value = -1
        ageStringState.value = ""
        ageGenderDialogDoneAllowed.value = false
        canUpdate.value = false
        nameState.value = ""
        emailState.value = ""
        mobileState.value = ""
        fetchMembers()
    }

    private fun leftPad(i: Int): String {
        if(i<10){
            return "0$i"
        }
        else{
            return "$i"
        }
    }

    @Composable
    private fun ageGenderDialog() {
        val dateDelimeter = "-"
        val genderIconSize = 48
        val red = colorResource(id = R.color.red)
        fun validate(){
            ageGenderDialogDoneAllowed.value = (dobStringState.value.isNotEmpty()||ageStringState.value.isNotEmpty())&&genderState.value > -1
        }
        Dialog(
            onDismissRequest = {
                ageGenderDialog.value = false
            }) {
            if(calendarOpenState.value){
                Box(modifier = Modifier
                    .fillMaxWidth()
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
                            val dob = dobStringState.value
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
                                    dobStringState.value = "$year$dateDelimeter${leftPad(monthOfYear+1)}$dateDelimeter${leftPad(dayOfMonth)}"
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
                                            dobStringState.value = ""
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
                                Text("DOB\n"+dobStringState.value,
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
                                ageGenderDialog.value = false
                                validate()
                                this@MembersActivity.validate()
                            },
                            enabled = ageGenderDialogDoneAllowed.value,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Done")
                        }
                    }
                }
            }

        }
    }

    @Composable
    private fun inputField(field: String,value: String,onChange: (String)->Unit) {
        val focusManager = LocalFocusManager.current
        val red = colorResource(id = R.color.red)
        val name = remember { mutableStateOf(value) }
        OutlinedTextField(
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            value = name.value,
            onValueChange = {
                name.value = it
                onChange(name.value)
            },
            placeholder = {
                Text(field)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = red,
                cursorColor = red,
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = when(field.lowercase()){
                    "name"->KeyboardCapitalization.Words
                    else->KeyboardCapitalization.None
                },
                keyboardType = when(field.lowercase()){
                    "email"->KeyboardType.Email
                    "mobile"->KeyboardType.Phone
                    else->KeyboardType.Text
                },
                imeAction = when(field.lowercase()){
                    "mobile"->ImeAction.Done
                    else->ImeAction.Next
                },

            )
        )
    }

    @Composable
    fun BoxScope.heading() {
        Text(
            when(mode.value){
                MyMode.LIST -> "Members"
                MyMode.ADD -> "Add new Member"
                MyMode.EDIT -> "Update Member details"
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    @Composable
    fun mainContent() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ){
            if(members.size==0){
                if(loading.value){
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.red),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp
                    )
                }else{
                    Text(
                        "No members found\nAdd one by clicking the + button",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            else{
                LazyColumn(modifier = Modifier.fillMaxSize()){
                    items(members){
                        memberView(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun memberView(user: User) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = 10.dp
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)){
                    Card(
                        shape = CircleShape
                    ){
                        AsyncImage(
                            model = user.image?:R.drawable.ic_user_svgrepo_com,
                            placeholder = painterResource(id = R.drawable.ic_user_svgrepo_com),
                            contentDescription = user.name,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()){
                        Text(
                            user.name?:"User",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            user.ageAndGender,
                            style = TextStyle(
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                IconButton(onClick = {
                    goToMemberDetails(user)
                },
                modifier = Modifier.wrapContentWidth()
                    ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Details"
                    )
                }
            }

        }
    }

    private fun goToMemberDetails(user: User) {
        currentEditingUser = user
        /******************************************/
        nameState.value = user.name?:""
        emailState.value = user.email?:""
        mobileState.value = user.mobile?:""
        profileBitmap.value = null
        profileSource.value = ProfileSource.URL
        profileImageUrl.value = user.image?:""
        dobStringState.value = user.dob?:""
        genderState.value = when(user.gender){
            "male"->0
            "female"->1
            else->-1
        }
        ageStringState.value = determineAgeAsAgeFieldAndDateOfAge(user)
        /******************************************/
        mode.value = MyMode.EDIT
    }

    private fun determineAgeAsAgeFieldAndDateOfAge(user: User): String {
        try {
            val age = user.age_on_date?.toInt()?:0
            val date = user.date_of_age
            var dateOfAge = DateTime(date)
            var ageToday = age + Years.yearsBetween(dateOfAge,DateTime()).years
            return ageToday.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    @Composable
    fun NetworkImage(
        url: String,
        @DrawableRes placeHolder:  Int,
        transformation: Transformation = CircleCropTransformation(),
        contentDescription: String?,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.Center,
        contentScale: ContentScale = ContentScale.Fit,
        alpha: Float = DefaultAlpha,
        colorFilter: ColorFilter? = null
    ){
        Image(
            painter = rememberImagePainter(
                url,
                builder = {
                    crossfade(true)
                    placeholder(placeHolder)
                    transformations(transformation)
                }
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    }

    @Composable
    fun BoxScope.addMemberButton() {
        FloatingActionButton(
            onClick = {
                goToAddMember()
            },
            modifier = Modifier.align(Alignment.BottomEnd),
            backgroundColor = colorResource(id = R.color.red)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                tint = Color.White,
                contentDescription = "Add Member"
            )
        }
    }

    private fun goToAddMember() {
        mode.value = MyMode.ADD
        profileImageUrl.value = ""
    }

    @Composable
    fun BoxScope.backButton() {
        IconButton(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            onClick = {
                when(mode.value){
                    MyMode.LIST -> finish()
                    MyMode.ADD,
                    MyMode.EDIT -> {
                        currentImage = null
                        mode.value = MyMode.LIST
                        uploading.value = false
                        profileBitmap.value = null
                        askSourceOpen.value = false
                        calendarOpenState.value = false
                        dobStringState.value = ""
                        ageGenderDialog.value = false
                        genderState.value = -1
                        ageStringState.value = ""
                        ageGenderDialogDoneAllowed.value = false
                        canUpdate.value = false
                        nameState.value = ""
                        emailState.value = ""
                        mobileState.value = ""
                        mode.value = MyMode.LIST
                    }
                }

            }) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Filled.ChevronLeft,
                tint = Color.Black,
                contentDescription = "Back"
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview2() {
        HelloMyDoc2Theme {
            Content()
        }
    }
}

