package com.hellomydoc.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.anggrayudi.storage.SimpleStorageHelper
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.activities.HomeActivity
import com.hellomydoc.data.FileAttachment
import com.hellomydoc.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Years
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {
    private lateinit var childCallback: HomeActivity.ChildCallback
    private val askLogoutOpen = mutableStateOf(false)
    private var currentImage: Any? = null
    private val profileBitmap = mutableStateOf<Bitmap?>(null)
    private val canSave = mutableStateOf(false)
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_FILE = 3
    private val saving = mutableStateOf(false)
    private val storageHelper = SimpleStorageHelper(this)
    private val user = mutableStateOf(User.LoadingTime)
    private val profileEdit = mutableStateOf(false)
    private val askSourceOpen = mutableStateOf(false)
    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStorageHelperCallback()
        fetchUser()
        setupContent()
    }

    private fun setupContent() {
        binding.cvContent.setContent {
            val userState = remember { user }
            val profileEditState = remember { profileEdit }
            if (profileEditState.value) {
                profileEditContent(userState, profileEditState)
            } else {
                profileContent(userState, profileEditState)
            }
        }
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

    private fun saveProfileImage() {
        val f = fileAttachmentFrom(currentImage)
        if (f != null) {
            lifecycleScope.launch {
                saving.value = true
                processApi {
                    repository.updateProfileImage(f).resp
                }
                    .apply {

                        when (status) {
                            ApiDispositionStatus.RESPONSE -> {
                                response?.apply {
                                    if (success) {
                                        currentImage = null
                                        canSave.value = false
                                        profileEdit.value = false
                                        val img = image
                                        user.value = user.value.apply {
                                            image = img
                                        }
                                        saving.value = false
                                        Toast.makeText(
                                            requireContext(),
                                            message,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    } else {
                                        message.toast(requireContext())
                                    }
                                }
                            }
                            else -> {

                                R.string.something_went_wrong.string.toast(requireContext())
                            }
                        }
                    }
            }
        }

    }

    private fun fromGallery() {
        storageHelper.openFilePicker(REQUEST_FILE, false, "image/*")
    }
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                fromCamera()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
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
                requireContext(),
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

    private fun onFileSelected(files: List<DocumentFile>) {
        val t = files.first()
        currentImage = t
        canSave.value = true
        var contentResolver = activity?.contentResolver
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
    }

    private fun onImageCaptured(imageBitmap: Bitmap) {
        currentImage = imageBitmap
        canSave.value = true
        onImageObtained(imageBitmap)
    }

    private fun fetchUser() {
        lifecycleScope.launch {
            processApi {
                repository.getUser().resp
            }.apply {
                when (status) {
                    ApiDispositionStatus.RESPONSE -> {
                        response?.apply {
                            if (success) {
                                this@ProfileFragment.user.value = this.user
                                currentImage = this.user.image
                                canSave.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(childCallback: HomeActivity.ChildCallback) = ProfileFragment().apply {
            this.childCallback = childCallback
        }
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
        var ct = activity ?: return null
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(ct.getExternalFilesDir(null).toString() + File.separator + fileNameToSave)
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

    private fun folderToSave(): String {
        return activity?.cacheDir.toString()
    }

    private fun fileAttachmentFrom(item: Any?): FileAttachment? {
        if (item == null) {
            return null
        }
        if (item is String) {
            return null
        }
        var contentResolver = activity?.contentResolver ?: return null
        var ct = activity ?: return null
        try {
            if (item is Uri) {
                val file = item.toFile()
                val name = item.fileName(ct)
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
                val file = item.file(folderToSave() + File.separator + name, ct)
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

    /********composables*******************/
    @Composable
    private fun profileContent(
        userState: MutableState<User>,
        profileEditState: MutableState<Boolean>
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Profile",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                profileSection()
                Spacer(modifier = Modifier.height(40.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()){
                    item {
                        membersRow()
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        medicalHistoryRow()
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        notificationsRow()
                    }
                    item{
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        prescriptionsRow()
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        logoutRow()
                    }
                }
            }
        }
        confirmLogoutDialog()
    }

    @Composable
    private fun membersRow() {
        Column(modifier = Modifier.fillMaxWidth().clickable {
            childCallback.goToPage(HomeActivity.PAGE.MEMBERS,null)
        }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Members",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
                IconButton(onClick = {
                    childCallback.goToPage(HomeActivity.PAGE.MEMBERS,null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        tint = Color.Black,
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }
    }

    @Composable
    private fun confirmLogoutDialog() {
        if(askLogoutOpen.value) {
            Dialog(onDismissRequest = {
                askLogoutOpen.value = false
            }){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ){
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)){
                        Text(
                            "Logout",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Are you sure to delete?",
                            style = TextStyle(
                                fontSize = 16.sp,

                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth()){
                            Button(onClick = {
                                askLogoutOpen.value = false
                                doLogout()
                            },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.White,
                                    contentColor = colorResource(id = R.color.red)
                                )
                            ){
                                Text("Yes")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(onClick = {
                                askLogoutOpen.value = false
                            },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = colorResource(id = R.color.red),
                                    contentColor = Color.White
                                )
                            ){
                                Text("No")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun doLogout() {
        repository.clearPrefs()
        childCallback.goToPage(HomeActivity.PAGE.SPLASH,null)
    }

    @Composable
    private fun profileSection() {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (user.value.image == "") {
                profileImageFromSelectedReadOnly()
            } else {
                profileImageFromNetworkReadOnly()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                profileNameReadOnly()
                Spacer(modifier = Modifier.height(4.dp))
                profileEmailReadOnly()
                profileEditButton()
            }
        }
    }

    @Composable
    private fun profileEditButton() {
        if (user.value.id?.isNotEmpty==true) {
            Button(
                modifier = Modifier.wrapContentWidth(),
                onClick = { profileEdit.value = true },
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color(0xFFD32424)
                )
            ) {
                Text(
                    "Edit Profile",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    private fun profileEmailReadOnly() {
        if (user.value.email?.isEmpty==true) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 1.dp,
                color = colorResource(id = R.color.red)
            )
        } else {
            Text(
                user.value.email?:"Email",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )
        }
    }

    @Composable
    private fun profileNameReadOnly() {
        if (user.value.name?.isEmpty==true) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 1.dp,
                color = colorResource(id = R.color.red)
            )
        } else {
            Text(
                text = user.value.name?:"Name",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            )
        }
    }

    @Composable
    private fun profileImageFromSelectedReadOnly() {
        Image(
            painter = painterResource(id = R.drawable.ic_user_svgrepo_com),
            contentDescription = null,
            modifier = Modifier
                .size(91.dp)
                .clip(CircleShape)
        )
    }

    @Composable
    private fun profileImageFromNetworkReadOnly() {
        Image(
            painter = rememberImagePainter(
                user.value.image,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.ic_user_svgrepo_com)
                    transformations(CircleCropTransformation())
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(91.dp)
                .clip(CircleShape)
        )
    }

    @Composable
    private fun medicalHistoryRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    childCallback.goToPage(HomeActivity.PAGE.MEDICAL_HISTORY,null)
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Medical History",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
                IconButton(onClick = {
                    childCallback.goToPage(HomeActivity.PAGE.MEDICAL_HISTORY,null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        tint = Color.Black,
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }
    }

    @Composable
    private fun notificationsRow() {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                childCallback.goToPage(HomeActivity.PAGE.NOTIFICATIONS,null)
            }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Notifications",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
                IconButton(onClick = {
                    childCallback.goToPage(HomeActivity.PAGE.NOTIFICATIONS,null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        tint = Color.Black,
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }
    }

    @Composable
    private fun prescriptionsRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openPrescriptions()
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Prescriptions",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
                IconButton(onClick = {
                    openPrescriptions()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        tint = Color.Black,
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }
    }

    private fun openPrescriptions() {
        childCallback.goToPage(HomeActivity.PAGE.PRESCRIPTIONS,null)
    }

    @Composable
    private fun logoutRow() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    confirmLogout()
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Logout",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )
                IconButton(onClick = { confirmLogout() }) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        tint = Color(0xFFD32424),
                        contentDescription = ""
                    )
                }

            }
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
        }
    }

    private fun confirmLogout() {
        askLogoutOpen.value = true
    }

    @Composable
    private fun profileEditContent(
        userState: MutableState<User>,
        profileEditState: MutableState<Boolean>
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            backButton(profileEditState)
            profileEditingSection()
            saveButton()
        }
        askDialog()
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
    private fun BoxScope.saveButton() {
        Button(
            onClick = {
                saveProfileImage()
            },
            enabled = if (saving.value) false else canSave.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(
                    id = R.color.red
                ),
                contentColor = Color.White
            )
        ) {
            Row(modifier = Modifier.wrapContentSize()) {
                Text(
                    if (!saving.value) "SAVE" else "SAVING"
                )
                if (saving.value) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.red),
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 1.dp
                    )
                }
            }
        }
    }

    @Composable
    private fun profileEditingSection() {
        Column(modifier = Modifier.fillMaxSize()) {
            profileEditHeading()
            Spacer(modifier = Modifier.height(12.dp))
            profileImage()
            Spacer(modifier = Modifier.height(36.dp))
            editName()
            Spacer(modifier = Modifier.height(36.dp))
            editEmail()
            Spacer(modifier = Modifier.height(36.dp))
            editMobile()
            Spacer(modifier = Modifier.height(36.dp))
            ageAndGender()
        }
    }

    @Composable
    private fun ageAndGender() {
        val age = getCurrentAge(user.value.age_on_date,user.value.date_of_age,user.value.dob)
        Text(
            age+" Years " + user.value.gender?.uppercase(),
            style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
    }

    private fun getCurrentAge(ageOnDate: String?, dateOfAge: String?, dob: String?): String {
        return if(dob!=null&&(dob!="0000-00-00"&&dob.isNotEmpty)){
            ageAsDob(dob)
        } else if(ageOnDate!=null&&ageOnDate.isNotEmpty&&dateOfAge!=null&&dateOfAge.isNotEmpty){
            ageAsAgeOnDate(ageOnDate,dateOfAge)
        } else{
            ""
        }
    }

    private fun ageAsAgeOnDate(ageOnDate: String, dateOfAge: String): String {
        return try {
            val today = DateTime()
            val date = DateTime(dateOfAge)
            val ageToday = ageOnDate.toInt() + Years.yearsBetween(date,today).years
            ageToday.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun ageAsDob(dobString: String): String {
        return try {
            val today = DateTime()
            val dob = DateTime(dobString)
            val age = Years.yearsBetween(dob,today).years
            age.toString()
        } catch (e: Exception) {
            ""
        }
    }

    @Composable
    private fun editMobile() {
        Text(
            user.value.mobile?:"Mobile",
            style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
    }

    @Composable
    private fun editEmail() {
        Text(
            user.value.email?:"Email",
            style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
    }

    @Composable
    private fun editName() {
        Text(
            user.value.name?:"Name",
            style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider()
    }

    @Composable
    private fun ColumnScope.profileImage() {
        Box(modifier = Modifier
            .wrapContentSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                askSourceOpen.value = true
            }
            .align(Alignment.CenterHorizontally)) {
            if (profileBitmap.value == null) {
                profileImageFromNetwork()
            } else {
                profileImageFromSelected()
            }
            profileEditBadge()
        }
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
    private fun profileImageFromNetwork() {
        Image(
            painter = rememberImagePainter(
                user.value.image,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.ic_user_svgrepo_com)
                    transformations(CircleCropTransformation())
                }
            ),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .size(135.dp)
                .clip(CircleShape)
        )
    }

    @Composable
    private fun ColumnScope.profileEditHeading() {
        Text(
            R.string.profile.string,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontSize = 16.sp
            )
        )
    }

    @Composable
    private fun backButton(profileEditState: MutableState<Boolean>) {
        IconButton(onClick = { profileEditState.value = false }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_left),
                tint = Color.Black,
                contentDescription = "Back"
            )
        }
    }
}

