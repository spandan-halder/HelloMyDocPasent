package com.hellomydoc

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.openInputStream
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.hellomydoc.data.Repository
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest

/*fun Composable.wrap(){
    Box(){
        this@wrap
    }
}*/

fun MutableState<Boolean>.check(){
    this.value = true
}

fun MutableState<Boolean>.uncheck(){
    this.value = false
}

fun MutableState<Boolean>.toggle(){
    this.value = !this.value
}


val Long.utcToCurrentTimeZoneMillis: Long
    get() {
        return try {
            DateTime(this, DateTimeZone.UTC).toDateTime(DateTimeZone.getDefault()).millis
        } catch (e: Exception) {
            0
        }
    }

val String.md5: String
    get(){
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
    }

fun View.hide() {
    visibility = View.GONE
}
fun View.show() {
    visibility = View.VISIBLE
}

fun String.log(tag: String){
    Log.d(tag,this)
}

fun String.toast(context: Context){
    Toast.makeText(context,this,Toast.LENGTH_SHORT).show()
}

fun String.toastLong(context: Context){
    Toast.makeText(context,this,Toast.LENGTH_LONG).show()
}

val screenWidth:Int
    get() = Resources.getSystem().displayMetrics.widthPixels

val screenHeight:Int
    get() = Resources.getSystem().displayMetrics.heightPixels

val repository: Repository
    get() = Repository()

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


/**extensions for globally accessible resources by id**/
val Int.string: String
    get() = App.instance.stringResource(this)

val Editable.string: String
    get() = this.toString()

fun Int.drawable(): Drawable?
{
    return App.instance.drawableResource(this)
}

fun Int.dimension(): Float
{
    return try {
        App.instance.dimensionResource(this)!!
    } catch (e: Exception) {
        0f
    }
}

val Int.color: Int
    get() = App.instance.colorResource(this)

enum class ApiDispositionStatus{
    EXCEPTION,
    ERROR,
    FAILED,
    BODY_NULL,
    UNKNOWN,
    RESPONSE
}

data class ApiDisposition<T>(
    val status: ApiDispositionStatus,
    val exception: java.lang.Exception? = null,
    val error: String? = null,
    val failed: Any? = null,
    val response: T? = null,
)

suspend inline fun <reified T> processApi(api: suspend ()-> Resp<T>): ApiDisposition<T> {
    try {
        val response = api()
        when{
            response.isError->return ApiDisposition(
                ApiDispositionStatus.ERROR,
                error = response.errorMessage
            )
            else->{
                when{
                    response.isSuccess->{
                        val registrationResponse = response.body
                        when{
                            registrationResponse!=null->{
                                return ApiDisposition(
                                    ApiDispositionStatus.RESPONSE,
                                    response = registrationResponse
                                )
                            }
                            else->{
                                return ApiDisposition(
                                    ApiDispositionStatus.BODY_NULL
                                )
                            }
                        }
                    }
                    else->{
                        return ApiDisposition(
                            ApiDispositionStatus.FAILED
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        return ApiDisposition(
            ApiDispositionStatus.EXCEPTION,
            exception = e
        )
    }
}

/**
 * Consumer Key: UX5FVQgfJeCmbp7mAchqDCjT4Weuerfi
 * Consumer Secret: C4iIOFAQ07QvtG5g
 */

fun ImageView.setImage(url: String?,@DrawableRes default: Int = 0) {
    if(url==null || url.isEmpty){
        return
    }
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    Glide
        .with(App.instance)
        .load(url)
        .apply {
            if(default!=0){
                placeholder(default.drawable())
            }
        }
        //.transition(withCrossFade(factory))
        .into(this)
}

fun ImageView.setImage(url: String?) {
    if(url==null){
        return
    }
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    Glide
        .with(App.instance)
        .load(url)
        .listener(object: RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d("glide_debug",e?.message?:"")
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d("glide_debug","ready")
                return false
            }

        })
        //.transition(withCrossFade(factory))
        .into(this)
}

fun View.heightAnim(to: Float,duration:Long,listener: ()->Unit){
    val va = ValueAnimator.ofFloat(0f, 1f)
    va.duration = duration
    val mFromHeight = height
    val mToHeight = to
    va.addUpdateListener { animation ->
        val newHeight: Int
        newHeight = (mFromHeight + (mToHeight - mFromHeight) * (animation.animatedValue as Float)).toInt()
        Log.d("height_animation","height=$newHeight")
        layoutParams.height = newHeight
        requestLayout()
    }
    va.addListener(object: Animator.AnimatorListener{
        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            listener()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    })
    va.start()
}

fun Float.map(sa:Float,sb:Float,ta:Float,tb:Float): Float{
    val sd = sb - sa
    val rd = this - sa
    var p = rd/sd

    val td = tb - ta
    val tv = td*p
    var t = ta + tv
    return t
}

fun DocumentFile.file(destination: String, context: Context): File?{
    val f = File(destination)
    var success = false
    var input = openInputStream(context)
    input?.let {
        try {
            FileOutputStream(f).use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
                success = true
            }
        } finally {
        }
    }
    input?.close()
    return if(success){
        f
    }
    else{
        null
    }
}

fun Uri.fileName(context: Context): String{
    var result: String? = null
    if (scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun Long?.dateTimeFormat(format: String): String {
    return try {
        val df = DateTimeFormat.forPattern(format)
        val dt = DateTime(this, DateTimeZone.UTC)
        df.print(dt)
    } catch (e: Exception) {
        ""
    }
}

fun <T> T?.placeValue(default: T, blank: T? = null):T{
    if(this==null){
        return default
    }
    if(blank==null){
        return this
    }
    if(this==blank){
        return default
    }
    return this
}