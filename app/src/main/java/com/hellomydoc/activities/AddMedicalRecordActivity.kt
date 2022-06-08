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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.anggrayudi.storage.SimpleStorageHelper
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.data.FileAttachment
import com.hellomydoc.data.members.Member
import com.hellomydoc.databinding.ActivityAddMedicalRecordBinding
import com.hellomydoc.views.CustomRoundBottomSheet
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class AddMedicalRecordActivity : AbstractActivity() {
    private val members = mutableStateListOf<Member>()
    private val membersLoading = mutableStateOf(false)
    private val storageHelper = SimpleStorageHelper(this)
    private val canUpload = mutableStateOf(false)
    private var patient = mutableStateOf("")
    private var patientName = mutableStateOf("")
    val recordTypeDrawableIds = listOf(
        R.drawable.ic_report,
        R.drawable.ic_prescription,
        R.drawable.ic_invoice
    )
    val recordTypeTexts = listOf(
        R.string.report.string,
        R.string.prescription.string,
        R.string.invoice.string
    )
    val attachments: MutableList<Any> = mutableStateListOf()
    val recordName: MutableState<String> = mutableStateOf("")
    val recordTypeIndex: MutableState<Int> = mutableStateOf(0)

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2
    val REQUEST_FILE = 3
    private var loading = false
    private lateinit var binding: ActivityAddMedicalRecordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicalRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cvContent.setContent {
            content()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        if(savedInstanceState==null){
            storageHelper.onFileSelected = { requestCode, files ->
                when(requestCode){
                    REQUEST_FILE->{
                        attachments.addAll(files)
                        validateCanUpload()
                    }
                }
            }
            fetchMembers()
        }
    }


    private fun fetchMembers() {
        lifecycleScope.launch {
            membersLoading.value = true
            try {
                val r = repository.fetchMembers().resp
                if(r.isSuccess){
                    val mr = r.body?:return@launch
                    if(mr.success){
                        members.clear()
                        members.addAll(mr.members)
                        patient.value = members.getOrNull(0)?.user_id?: repository.userUid
                        patientName.value = members.getOrNull(0)?.name?:"MySelf"
                        membersLoading.value = false
                    }
                }
            } catch (e: Exception) {
                membersLoading.value = false
                Toast.makeText(this@AddMedicalRecordActivity, R.string.something_went_wrong.string, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val membersVisible = mutableStateOf(false)
    @Composable
    private fun content() {
        val canUploadState = remember { canUpload }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)){
            Column(modifier = Modifier.fillMaxSize()) {
                MemberSelectionUI()
                imagesSelection()
                    Spacer(modifier = Modifier.size(12.dp))
                recordNameField()
                    Spacer(modifier = Modifier.size(33.dp))
                recordTypes()
                    Spacer(modifier = Modifier.size(33.dp))
                recordCreated()
            }
            Button(
                enabled = canUpload.value,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(alignment = Alignment.BottomCenter),
                onClick = {
                    onUploadClick()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.red),
                    contentColor = Color.White
                )) {
                Text(stringResource(R.string.upload_record).uppercase())
            }
        }

        if(membersVisible.value){
            Dialog(
                onDismissRequest = {
                    membersVisible.value = false
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)

                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ){
                        Text(
                            "Members",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Divider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if(membersLoading.value){
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator(
                                    color = Color.Red,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        else{
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ){
                                items(members){
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                onMemberSelect(it)
                                                membersVisible.value = false
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        AsyncImage(
                                            model = it.image,
                                            contentDescription = "Image",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            getMemberDisplayNameValue(it),
                                            color = Color.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MemberSelectionUI() {
        Row(
            modifier = Modifier.fillMaxWidth()
        ){
            if(membersLoading.value){
                Text("Loading members...")
            }
            else{
                if(members.size==0){
                    TextButton(onClick = {
                        fetchMembers()
                    }) {
                        Text(
                            "Failed to load members, Try again",
                            color = Color.Red,
                        )
                    }
                }
                else{
                    TextButton(onClick = {
                        membersVisible.value = true
                    }) {
                        Text(patientName.value)
                    }
                }
            }
        }

    }

    private fun getMemberDisplayNameValue(member: Member): String {
        if(member.user_id== repository.userUid){
            return "MySelf"
        }
        else{
            return "${member.name}(${member.genderShort}-${member.age})"
        }
    }

    private fun onMemberSelect(it: Member) {
        this.patientName.value = it.name
        this.patient.value = it.user_id
    }

    private fun onUploadClick() {
        val fileAttachments = attachments.map {
            fileAttachmentFrom(it)
        }
        val recordTypeText = getRecordTypeText()
        val date = getDateForServer()
        if(members.size==0){
            Toast.makeText(this, "Failed to load members", Toast.LENGTH_SHORT).show()
            fetchMembers()
            return
        }
        wait = true
        lifecycleScope.launch {
            processApi {
                repository.addMedicalRecord(
                    fileAttachments,
                    repository.userUid,
                    recordName.value,
                    recordTypeText,
                    date,
                    patient.value
                ).resp
            }
                .apply {
                    wait = false
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success&&recordAdded){
                                    R.string.record_added_successfully.string.toast(this@AddMedicalRecordActivity)
                                    finish()
                                }
                                else{
                                    message.toast(this@AddMedicalRecordActivity)
                                }
                            }
                        }
                        else->{

                            R.string.something_went_wrong.string.toast(this@AddMedicalRecordActivity)
                        }
                    }
                }
        }
    }

    private fun getDateForServer(): String {
        return Dt.today(Dt.DATE_DB_DATE_TIME)
    }

    private fun getDate(): String {
        return Dt.today("d MMM , yyyy")
    }

    private fun getRecordTypeText(): String {
        return when(recordTypeIndex.value){
            0->"report"
            1->"prescription"
            2->"invoice"
            else->""
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

    private fun fileAttachmentFrom(item: Any): FileAttachment? {
        try {
            if(item is Uri){
                val file = item.toFile()
                val name = item.fileName(this)
                val mimeType = contentResolver.getType(item)?:""
                return FileAttachment(item,file,name, mimeType)
            }else if(item is Bitmap){
                val name = newFileName("png")
                val file = bitmapToFile(item,name)
                val mimeType = "image/png"
                if(file!=null){
                    return FileAttachment(null,file,name, mimeType)
                }
            }else if(item is DocumentFile){
                val name = item.name?:""
                val file = item.file(folderToSave()+File.separator+name,this)
                val mimeType = item.type?:""
                if(file!=null){
                    return FileAttachment(null,file,name, mimeType)
                }
            }
        } catch (e: Exception) {
            Log.d("uri_to_attachment","${e.message}")
        }
        return null
    }

    private fun folderToSave(): String {
        return cacheDir.toString()
    }

    private fun newFileName(ext: String): String {
        val base = System.currentTimeMillis().toString()
        return if(ext.isEmpty()){
            base
        } else{
            "$base.$ext"
        }
    }

    @Composable
    private fun recordCreated() {
        Text(stringResource(R.string.record_created_on))
        Spacer(modifier = Modifier.size(24.dp))
        Text(
            getDate(),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(Color.LightGray))
    }

    @Preview
    @Composable
    private fun contentPreview() {
        content()
    }

    @Composable
    private fun recordTypes() {
        Text(stringResource(R.string.type_of_record))
        Spacer(modifier = Modifier.size(20.dp))
        val recordTypeIndexValue = remember { recordTypeIndex }
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            recordType(0)
            recordType(1)
            recordType(2)
        }
    }

    @Composable
    private fun recordType(i: Int) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                recordTypeIndex.value = i
                validateCanUpload()
            }
        ){
            Icon(
                painter = painterResource(id = recordTypeDrawableIds[i]),
                contentDescription = "",
                tint = recordTypeColorOf(i),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(recordTypeTexts[i], color = recordTypeColorOf(i))
        }
    }

    @Composable
    private fun recordNameField() {
        val red = colorResource(id = R.color.red)
        val recordNameFieldValue = remember { recordName }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = recordNameFieldValue.value,
            onValueChange = {
                recordNameFieldValue.value = it
                validateCanUpload()
            },
            label = { Text(text = getString(R.string.record_name)) },
            placeholder = { Text(text = getString(R.string.enter_record_name)) },
            maxLines = 1,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                cursorColor = red,
                textColor = red,
                focusedLabelColor = red,
                focusedIndicatorColor = red
            )
        )
    }

    @Composable
    private fun imagesSelection() {
        val allImages = remember { attachments }
        LazyRow{
            item{
                addImageButton()
            }
            items(allImages){ t->
                imageHolder(t)
            }
        }
    }

    enum class ThumbnailType{
        NONE,
        PDF,
        IMAGE
    }
    data class Thumbnail(
        val type: ThumbnailType,
        val bitmap: Bitmap?,
        val name: String = "",
        val uri: Uri?=null,
    )

    private fun resolveThumbnail(t: Any): Thumbnail {
        if(t is Bitmap){
            return Thumbnail(ThumbnailType.IMAGE,t)
        }
        else if(t is Uri){
            var type = contentResolver.getType(t)?:""
            if(type.startsWith("image/")){
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    var s = ImageDecoder.createSource(contentResolver, t)
                    Thumbnail(ThumbnailType.IMAGE,ImageDecoder.decodeBitmap(s),"",t)

                } else{
                    Thumbnail(ThumbnailType.IMAGE,MediaStore.Images.Media.getBitmap(contentResolver, t),"",t)
                }
            }
            else if(type=="application/pdf"){
                return Thumbnail(ThumbnailType.PDF,null,t.fileName(this)?:"",t)
            }
        }
        else if(t is DocumentFile){
            var type = t.type?:""
            if(type.startsWith("image/")){
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    var s = ImageDecoder.createSource(contentResolver, t.uri)
                    Thumbnail(ThumbnailType.IMAGE,ImageDecoder.decodeBitmap(s),"",t.uri)

                } else{
                    Thumbnail(ThumbnailType.IMAGE,MediaStore.Images.Media.getBitmap(contentResolver, t.uri),"",t.uri)
                }
            }
            else if(type=="application/pdf"){
                return Thumbnail(ThumbnailType.PDF,null,t.name?:"",t.uri)
            }
        }
        return Thumbnail(ThumbnailType.NONE,null)
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun imageHolder(t: Any) {
        Row(modifier = Modifier.wrapContentSize()){
            Spacer(modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        onImageItemClicked(resolveThumbnail(t))
                    }
            ){
                var b = resolveThumbnail(t)
                if(b.type==ThumbnailType.IMAGE&&b.bitmap!=null){
                    Card(
                        modifier = Modifier
                            .size(114.dp),
                        shape = CircleShape,
                        elevation = 0.dp,
                    ){
                        Image(
                            bitmap = (b.bitmap)!!.asImageBitmap(),
                            contentDescription = "some useful description",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }else if(b.type==ThumbnailType.PDF){
                    Card(
                        modifier = Modifier
                            .size(114.dp)
                            .border(
                                border = BorderStroke(
                                    2.dp,
                                    colorResource(id = R.color.very_light_red)
                                ),
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        elevation = 0.dp,
                    ){
                        Box(modifier = Modifier.fillMaxSize()){
                            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally){
                                Icon(
                                    modifier = Modifier
                                        .size(24.dp),
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "",
                                    tint = colorResource(id = R.color.red),
                                )
                                Text(
                                    b.name,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Icon(
                    modifier = Modifier
                        .clickable {
                            deleteImage(t)
                        }
                        .align(alignment = Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(shape = CircleShape)
                        .background(color = Color.White.copy(alpha = 0.5f)),
                    imageVector = Icons.Filled.Close,
                    contentDescription = "",
                    tint = colorResource(id = R.color.red),
                )
            }
        }
    }

    private fun onImageItemClicked(t: Thumbnail) {
        if(/*t.type==ThumbnailType.PDF&&*/t.uri!=null){
            /*val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(t.uri, "application/pdf")*/

            startActivity(Intent(Intent.ACTION_VIEW, t.uri))
        }
    }

    @Composable
    private fun addImageButton() {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = interactionSource
                ) {
                    addImage()
                }
                .size(114.dp)
                .clip(shape = CircleShape)
                .background(
                    color = colorResource(
                        R.color.very_light_red
                    )
                ),
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "",
                    tint = colorResource(id = R.color.red),
                )
                Text(
                    (if(attachments.size>0)
                        R.string.more_attachments.string
                    else R.string.attach.string),
                    color = colorResource(R.color.red),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun recordTypeColorOf(i: Int): Color {
        return if(i==recordTypeIndex.value){
            colorResource(id = R.color.red)
        } else{
            Color.Gray
        }
    }

    private fun addImage() {
        CustomRoundBottomSheet
            .create(this)
            .layout(R.layout.choose_image_dialog_layout)
            .with {
                findViewById<View>(R.id.llh_photo)?.setOnClickListener {
                    onTakePhotoClicked()
                    dismiss()
                }
                findViewById<View>(R.id.llh_gallery)?.setOnClickListener {
                    onFromGalleryClicked()
                    dismiss()
                }
                findViewById<View>(R.id.llh_file)?.setOnClickListener {
                    onUploadFilesClicked()
                    dismiss()
                }
            }
            .show()
    }

    private fun onUploadFilesClicked() {
        storageHelper.openFilePicker(REQUEST_FILE, true,"application/pdf")
        /*val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.type = "application/pdf";
        try {
            startActivityForResult(intent, REQUEST_FILE)
        } catch (e: Exception) {
        }*/
    }

    private fun onFromGalleryClicked() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
//        intent.type = "image/*";
//        try {
//            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
//        } catch (e: Exception) {
//        }
        storageHelper.openFilePicker(REQUEST_FILE, true,"image/*")
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                onTakePhotoClicked()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun onTakePhotoClicked() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var d = data?.data
        var c = data?.clipData
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            onImageCaptured(imageBitmap)
        }
        if ((requestCode == REQUEST_IMAGE_GALLERY||requestCode == REQUEST_FILE) && resultCode == RESULT_OK) {
            if(d!=null){
                attachments.add(d)
                validateCanUpload()
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
        }
    }

    private fun onImageCaptured(imageBitmap: Bitmap) {
        attachments.add(imageBitmap)
        validateCanUpload()
    }

    private fun validateCanUpload() {
        canUpload.value = recordName.value.isNotEmpty()&&attachments.size>0
    }

    private fun deleteImage(t: Any?) {
        attachments.remove(t)
        validateCanUpload()
    }
}