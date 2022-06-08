package com.hellomydoc

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.hellomydoc.data.*
import com.hellomydoc.data.appointment_booking_doctor_response.DoctorsForAppointmentResponse
import com.hellomydoc.data.doctorDetails.DoctorDetailsResponse
import com.hellomydoc.data.slots.SlotsResponse
import com.hellomydoc.views.AppointmentTypeView
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.Years
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

data class Resp<T>(
    val response: Response<T>
){
    val isError: Boolean
        get(){
            return response.errorBody()!=null
        }
    val errorMessage: String
        get(){
            val inputStream = response.errorBody()?.byteStream()
            val textBuilder = StringBuilder()
            BufferedReader(
                InputStreamReader(inputStream,
                Charset.forName(StandardCharsets.UTF_8.name()))
            ).use { reader ->
                var c = 0
                while (reader.read().also { c = it } != -1) {
                    textBuilder.append(c.toChar())
                }
            }
            return textBuilder.toString()
        }
    val isNotError: Boolean
        get(){
            return response.errorBody()==null
        }
    val isSuccess: Boolean
        get(){
            return response.isSuccessful
        }
    val body: T?
        get() = response.body()
}

val <T> Response<T>.resp: Resp<T>
    get() = Resp(this)

//interface
interface UsersApi {



    @FormUrlEncoded
    @POST("/api/v1/welcome/appointmentFinalizedAcknowledgement")
    suspend fun appointmentFinalizedAcknowledgement(
        @Field("appointmentId") appointmentId: String
    ): Response<AppointmentFinalizedAcknowledgementResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/videoCallAllowed")
    suspend fun checkVideoCallAllowed(
        @Field("userId") userId: String,
        @Field("userType") userType: String,
        @Field("appointmentId") appointmentId: String,
    ): Response<VideoCallAllowedResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/listOfPrescriptions")
    suspend fun fetchPrescriptions(
        @Field("userId") userId: String,
        @Field("userType") userType: String,
    ): Response<PrescriptionsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/prescriptionDetails")
    suspend fun fetchPrescriptionDetails(
        @Field("prescriptionId") userId: String,
    ): Response<PrescriptionDetailsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/medicalHistoryByUserId")
    suspend fun fetchMedicalHistory(
        @Field("userId") userId: String,
    ): Response<MedicalHistoryResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/membersByMasterId")
    suspend fun fetchMembers(
        @Field("userId") userId: String,
    ): Response<com.hellomydoc.data.members.MembersResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/doctorDetails")
    suspend fun fetchDoctorDetails(
        @Field("doctor_id") doctorId: String,
    ): Response<DoctorDetailsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/doctorsForAppointmentBooking")
    suspend fun getDoctorsForAppointmentBooking(
        @Field("user_id") user_id: String,
        @Field("type") type: String,
        @Field("speciality",) speciality: String
    ): Response<DoctorsForAppointmentResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/checkPaymentDoneByAppointmentId")
    suspend fun checkPaymentDoneByAppointmentId(@Field("appointmentId") appointmentId: String): Response<PaymentDoneStatus>

    @POST("/api/v1/welcome/paymentMethods")
    suspend fun paymentMethods(
    ): Response<PaymentMethodsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/slotData")
    suspend fun getSlots(
        @Field("id") user_id: String,
        @Field("type") type: String,
        @Field("speciality",) speciality: String
    ): Response<SlotsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/updateAgeAndGender")
    suspend fun updateAgeAndGender(
        @Field("user_id") user_id: String,
        @Field("age") age: String,
        @Field("dob") dob: String,
        @Field("gender") gender: String,
    ): Response<AgeGenderUpdatedResponse>

    @FormUrlEncoded
    @POST("users/getById")
    suspend fun getUserById(@Field("id") user_id: String): Response<UserResponse>

    @FormUrlEncoded
    @POST("users/getUser")
    suspend fun getUser(@Field("user_id") user_id: String, @Field("pass") pass: String): Response<UserResponse>

    @FormUrlEncoded
    @POST("members")
    suspend fun getMembers(@Field("user_id") user_id: String): Response<MembersResponse>

    @FormUrlEncoded
    @POST("deleteMember")
    suspend fun deleteMember(
        @Field("user_id") user_id: String,
        @Field("member_id") member_id: String
    ): Response<MemberDeletedResponse>

    @FormUrlEncoded
    @POST("appointments/bookings")
    suspend fun bookings(@Field("user_id") user_id: String): Response<AppointmentsResponse>

    @FormUrlEncoded
    @POST("medicalRecords")
    suspend fun medicalRecords(@Field("user_id") user_id: String): Response<MedicalRecordsResponse>

    @FormUrlEncoded
    @POST("clientNotifications")
    suspend fun notifications(@Field("user_id") user_id: String): Response<NotificationsResponse>

    @FormUrlEncoded
    @POST("/api/v1/welcome/appointmentPrices")
    suspend fun appointmentPrices(@Field("user_id") user_id: String): Response<AppointmentPricesResponse>


    @POST("/api/v1/welcome/appointmentFinalize")
    suspend fun appointmentFinalize(@Body appointmentSummary: AppointmentSummary): Response<AppointmentFinalizedResponse>

    @FormUrlEncoded
    @POST("appointments/fewUpcoming")
    suspend fun fetchFewUpcomingAppointments(@Field("user_id") user_id: String): Response<AppointmentsResponse>


    @FormUrlEncoded
    @POST("bookings/recents")
    suspend fun fetchRecentBookings(@Field("user_id") user_id: String): Response<AppointmentsResponse>

    @FormUrlEncoded
    @POST("ageAndGenderOk")
    suspend fun checkAgeAndGender(@Field("user_id") user_id: String): Response<AgeAndGenderOkResponse>

    @FormUrlEncoded
    @POST("setMobileVerified")
    suspend fun setMobileVerified(
        @Field("mobile") mobile: String,
        @Field("user_id") user_id: String,
    ): Response<MobileVerifiedSetResponse>

    @FormUrlEncoded
    @POST("sendOtpForNumberValidation")
    suspend fun sendOtpForNumberValidation(@Field("mobile") mobile: String): Response<OTPResponse>

    @FormUrlEncoded
    @POST("appointments/list")
    suspend fun appointments(@Field("user_id") userId: String): Response<AppointmentsResponse>

    @POST("googleLogin")
    suspend fun getGoogleLoginResponse(
        @Header("Content-Type") str: String,
        @Body loginData: GoogleLoginData,
    ):Response<LoginResponse>

    @POST("facebookLogin")
    suspend fun getFacebookLoginResponse(
        @Header("Content-Type") str: String,
        @Body loginData: FacebookLoginData,
    ):Response<LoginResponse>

    @POST("registration")
    suspend fun register(
        @Header("Content-Type") str: String,
        @Body registrationData: RegistrationData,
    ):Response<RegistrationResponse>

    @FormUrlEncoded
    @POST("requestPasswordResetCode")
    suspend fun requestPasswordResetCode(@Field("mobile") mobile: String): Response<PasswordResetCodeSendResponse>

    @Multipart
    @POST("addMedicalRecord")
    suspend fun addMedicalRecord(
        @Part image_list: List<MultipartBody.Part>,
        @Part user_id: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part type: MultipartBody.Part,
        @Part date: MultipartBody.Part,
        @Part patient: MultipartBody.Part,
    ): Response<MedicalRecordAddedResponse>

    @Multipart
    @POST("updateProfileImage")
    suspend fun updateProfileImage(
        @Part image: MultipartBody.Part,
        @Part user_id: MultipartBody.Part
    ): Response<UpdateProfileResponse>

    @Multipart
    @POST("addMember")
    suspend fun addNewMember(
        @Part image: MultipartBody.Part,
        @Part user_id: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part mobile: MultipartBody.Part,
        @Part age: MultipartBody.Part,
        @Part dob: MultipartBody.Part,
        @Part gender: MultipartBody.Part,
    ): Response<NewMemberAddResponse>

    @Multipart
    @POST("updateMember")
    suspend fun updateMember(
        @Part image: MultipartBody.Part?,
        @Part member_id: MultipartBody.Part,
        @Part user_id: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part mobile: MultipartBody.Part,
    ): Response<MemberUpdateResponse>
}

//repository
class ApiRepository {
    suspend fun addMedicalRecord(
        image_list: List<MultipartBody.Part>,
        user_id: MultipartBody.Part,
        name: MultipartBody.Part,
        type: MultipartBody.Part,
        date: MultipartBody.Part,
        patient: MultipartBody.Part,
    ) = ApiService.get().addMedicalRecord(
            image_list,
            user_id,
            name,
            type,
            date,
            patient
        )

    suspend fun updateProfileImage(
        image: MultipartBody.Part,
        user_id: MultipartBody.Part
    ) = ApiService.get().updateProfileImage(
        image,
        user_id
    )

    suspend fun addNewMember(
        image: MultipartBody.Part,
        user_id: MultipartBody.Part,
        name: MultipartBody.Part,
        email: MultipartBody.Part,
        mobile: MultipartBody.Part,
        age: MultipartBody.Part,
        dob: MultipartBody.Part,
        gender: MultipartBody.Part,
    ) = ApiService.get().addNewMember(
        image,
        user_id,
        name,
        email,
        mobile,
        age,
        dob,
        gender
    )

    suspend fun updateMember(
        image: MultipartBody.Part?,
        member_id: MultipartBody.Part,
        user_id: MultipartBody.Part,
        name: MultipartBody.Part,
        email: MultipartBody.Part,
        mobile: MultipartBody.Part,
    ) = ApiService.get().updateMember(
        image,
        member_id,
        user_id,
        name,
        email,
        mobile,
    )


    suspend fun appointmentFinalizedAcknowledgement(lastAppointmentFinalizedId: String) = ApiService.get().appointmentFinalizedAcknowledgement(lastAppointmentFinalizedId)
    suspend fun checkPaymentDoneByAppointmentId(appointmentId: String) = ApiService.get().checkPaymentDoneByAppointmentId(appointmentId)
    suspend fun getUser(user_id: String, pass: String) = ApiService.get().getUser(user_id,pass)
    suspend fun deleteMember(user_id: String, member_id: String) = ApiService.get().deleteMember(user_id,member_id)
    suspend fun getMembers(user_id: String) =
        ApiService.get().getMembers(user_id)
    suspend fun bookings(user_id: String) = ApiService.get().bookings(user_id)
    suspend fun medicalRecords(user_id: String) = ApiService.get().medicalRecords(user_id)
    suspend fun notifications(user_id: String) = ApiService.get().notifications(user_id)
    suspend fun appointmentPrices(user_id: String) = ApiService.get().appointmentPrices(user_id)
    suspend fun appointmentFinalize(appointmentSummary: AppointmentSummary) = ApiService.get().appointmentFinalize(appointmentSummary)
    suspend fun getFacebookResponse(facebookLoginData: FacebookLoginData) = ApiService.get().getFacebookLoginResponse(Constants.APPLICATON_JSON, facebookLoginData)
    suspend fun getGoogleLoginResponse(googleLoginData: GoogleLoginData) = ApiService.get().getGoogleLoginResponse(Constants.APPLICATON_JSON, googleLoginData)
    suspend fun sendOtpForNumberValidation(mobile: String) = ApiService.get().sendOtpForNumberValidation(mobile)
    suspend fun setMobileVerified(mobile: String, user_id: String) = ApiService.get().setMobileVerified(mobile, user_id)
    suspend fun register(registrationData: RegistrationData) = ApiService.get().register(Constants.APPLICATON_JSON,registrationData)
    suspend fun appointments(userId: String) = ApiService.get().appointments(userId)
    suspend fun requestPasswordResetCode(mobile: String): Response<PasswordResetCodeSendResponse> = ApiService.get().requestPasswordResetCode(mobile)
    suspend fun fetchFewUpcomingAppointments(userUid: String): Response<AppointmentsResponse> = ApiService.get().fetchFewUpcomingAppointments(userUid)
    suspend fun fetchRecentBookings(userUid: String): Response<AppointmentsResponse> = ApiService.get().fetchRecentBookings(userUid)
    suspend fun checkAgeAndGender(userUid: String): Response<AgeAndGenderOkResponse> =
        ApiService.get().checkAgeAndGender(userUid)
    suspend fun getUserById(id: String): Response<UserResponse> = ApiService.get().getUserById(id)
    suspend fun getSlots(id: String, type: AppointmentTypeView.APPOINTMENT_TYPE, speciality: String): Response<SlotsResponse> = ApiService.get().getSlots(id,type.name, speciality)
    suspend fun updateAgeAndGender(id: String, age: String, dob: String, gender: String): Response<AgeGenderUpdatedResponse> =
        ApiService.get().updateAgeAndGender(id,age,dob,gender)
    suspend fun getDoctorsForAppointmentBooking(
        user_id: String, type: String, speciality: String): Response<DoctorsForAppointmentResponse>
    = ApiService.get().getDoctorsForAppointmentBooking(user_id,type,speciality)

    suspend fun paymentMethods(): Response<PaymentMethodsResponse> = ApiService.get().paymentMethods()

    suspend fun checkVideoCallAllowed(userId: String,userType: String,appointmentId: String) =
        ApiService.get().checkVideoCallAllowed(userId,userType,appointmentId)

    suspend fun fetchPrescriptions(userId: String,userType: String) =
        ApiService.get().fetchPrescriptions(userId,userType)

    suspend fun fetchPrescriptionDetails(prescriptionId: String) =
        ApiService.get().fetchPrescriptionDetails(prescriptionId)

    suspend fun fetchMedicalHistory(userId: String) =
        ApiService.get().fetchMedicalHistory(userId)

    suspend fun fetchMembers(userId: String) =
        ApiService.get().fetchMembers(userId)

    suspend fun fetchDoctorDetails(doctorId: String) =
        ApiService.get().fetchDoctorDetails(doctorId)
}
private fun httpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(
            ChuckerInterceptor.Builder(App.instance as Context)
                .collector(ChuckerCollector(App.instance as Context))
                .maxContentLength(Constants.MAX_CONTENT_LENGHT)
                .redactHeaders(emptySet())
                .alwaysReadResponseBody(false)
                .build()
        )
        .build()
}

//service provider
object ApiService{
    fun get(): UsersApi {
        val gson = GsonBuilder()
            .create()
        val BASE_URL = Constants.BASE_URL
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient())
            .addConverterFactory(
                GsonConverterFactory
                    .create(gson)
            )
            .build()
        val apiService = retrofit.create(UsersApi::class.java)
        return apiService
    }
}

//data model
data class User(
    val email: String?,
    val id: String?,
    var image: String?,
    val master: String?,
    val mobile: String?,
    val mobile_verified: String?,
    val name: String?,
    val organization: String?,
    val password: String?,
    val provided_user_id: String?,
    val status: String?,
    val timestamp: String?,
    val type: String?,
    val user_id: String?,
    val age_on_date: String? = "",
    val date_of_age: String? = "",
    val dob: String? = "",
    val gender: String? = ""
){
    val ageAndGender: String
    get(){
        var age = getCurrentAge(age_on_date,date_of_age,dob)
        var ageString = if(age>0) "$age Year(s)" else "Age Not set"
        var genderString = if(gender!=null&&gender.isNotEmpty) gender.uppercase() else "Gender Not set"
        return "$ageString • $genderString"
    }

    private fun getCurrentAge(ageOnDate: String?, dateOfAge: String?, dob: String?): Int {
        return if(dob!=null&&dob!="0000-00-00"&&dob.isNotEmpty){
            ageAsDob(dob)
        } else if(ageOnDate!=null&&ageOnDate.isNotEmpty&&dateOfAge!=null&&dateOfAge.isNotEmpty){
            ageAsAgeOnDate(ageOnDate,dateOfAge)
        } else{
            0
        }
    }

    private fun ageAsAgeOnDate(ageOnDate: String, dateOfAge: String): Int {
        return try {
            val today = DateTime()
            val date = DateTime(dateOfAge)
            val ageToday = ageOnDate.toInt() + Years.yearsBetween(date,today).years
            ageToday
        } catch (e: Exception) {
            0
        }
    }

    private fun ageAsDob(dobString: String): Int {
        return try {
            val today = DateTime()
            val dob = DateTime(dobString)
            val age = Years.yearsBetween(dob,today).years
            age
        } catch (e: Exception) {
            0
        }
    }

    companion object{
        val LoadingTime = User(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        )
    }
}

data class UserResponse(
    val success: Boolean,
    val message: String,
    val user: User
)
data class LoginResponse (
    val success: Boolean,
    val message: String,
    val user_id:String,
    val mobile:String,
    val mobile_verified: String,
)
data class RegistrationResponse (
    val success: Boolean,
    val message: String,
    val user_id:String
)
data class GoogleLoginData(
    val email: String,
    val name: String,
    val imageUrl: String,
    val id: String,
)
data class FacebookLoginData(
    val email: String,
    val name: String,
    val imageUrl: String,
    val id: String,
)

