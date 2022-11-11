package com.hellomydoc.data

//import com.pixplicity.easyprefs.library.Prefs
import android.util.Log
import com.hellomydoc.*
import com.hellomydoc.data.appointment_booking_doctor_response.Doctor
import com.hellomydoc.data.appointment_booking_doctor_response.DoctorsForAppointmentResponse
import com.hellomydoc.data.doctorDetails.DoctorDetailsResponse
import com.hellomydoc.data.slots.SlotsResponse
import com.hellomydoc.views.AppointmentTypeView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.lang.reflect.Type

class Repository {
    var introSeen: Boolean
        get() = Prefs.getBoolean(Constants.INTRO_SEEN_KEY)
        set(value) {
            Prefs.putBoolean(Constants.INTRO_SEEN_KEY,value)
        }
    var userUid: String
        get() = Prefs.getString(Constants.USER_UID)?:""
        set(value) {
            Prefs.putString(Constants.USER_UID,value)
            if(value.isNotEmpty&&value.matches("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$".toRegex())){
                App.instance.syncFcmToken()
            }
        }
    var loginDone: Boolean
        get() = Prefs.getBoolean(Constants.LOGIN_DONE)
        set(value) {
            Prefs.putBoolean(Constants.LOGIN_DONE,value)
        }

    suspend fun updateMember(
        f: FileAttachment?,
        memberId: String,
        name: String,
        email: String,
        mobile: String
    ): Response<MemberUpdateResponse> {
        var filePart = if(f!=null) MultipartBody.Part.createFormData(
            "image",
            f.name,
            f.file.asRequestBody(f.mimeType.toMediaTypeOrNull())
        ) else null

        return ApiRepository().updateMember(
            filePart,
            memberId.toPart("member_id"),
            userUid.toPart("user_id"),
            name.toPart("name"),
            email.toPart("email"),
            mobile.toPart("mobile"),
        )
    }

    suspend fun addNewMember(
        f: FileAttachment,
        name: String,
        email: String,
        mobile: String,
        age: String,
        dob: String,
        gender: String
    ): Response<NewMemberAddResponse> {
        var filePart = MultipartBody.Part.createFormData(
            "image",
            f.name,
            f.file.asRequestBody(f.mimeType.toMediaTypeOrNull())
        )

        return ApiRepository().addNewMember(
            filePart,
            userUid.toPart("user_id"),
            name.toPart("name"),
            email.toPart("email"),
            mobile.toPart("mobile"),
            age.toPart("age"),
            dob.toPart("dob"),
            gender.toPart("gender"),
        )
    }

    suspend fun getUser(userId: String, pass: String): Response<UserResponse> {
        return ApiRepository().getUser(userId,pass)
    }

    suspend fun deleteMember(memberId: String): Response<MemberDeletedResponse> {
        return ApiRepository().deleteMember(userUid,memberId)
    }

    suspend fun getMembers(): Response<MembersResponse> {
        return ApiRepository().getMembers(userUid)
    }

    suspend fun bookings(): Response<AppointmentsResponse> {
        return ApiRepository().bookings(userUid)
    }

    suspend fun medicalRecords(): Response<MedicalRecordsResponse> {
        return ApiRepository().medicalRecords(userUid)
    }

    suspend fun notifications(): Response<NotificationsResponse> {
        return ApiRepository().notifications(userUid)
    }

    suspend fun checkPaymentDoneByAppointmentId(appointmentId: String): Response<PaymentDoneStatus> {
        return ApiRepository().checkPaymentDoneByAppointmentId(appointmentId)
    }

    suspend fun appointmentFinalizedAcknowledgement(lastAppointmentFinalizedId: String): Response<AppointmentFinalizedAcknowledgementResponse> {
        return ApiRepository().appointmentFinalizedAcknowledgement(lastAppointmentFinalizedId)
    }

    suspend fun appointmentPrices(): Response<AppointmentPricesResponse> {
        return ApiRepository().appointmentPrices(userUid)
    }

    suspend fun appointmentFinalize(appointmentSummary: AppointmentSummary): Response<AppointmentFinalizedResponse> {
        return ApiRepository().appointmentFinalize(appointmentSummary)
    }

    suspend fun getUser(): Response<UserResponse> {
        return ApiRepository().getUserById(repository.userUid)
    }

    suspend fun addMedicalRecord(
        attachments: List<FileAttachment?>,
        userId: String,
        name: String,
        type: String,
        date: String,
        patient: String
    ): Response<MedicalRecordAddedResponse> {
        val attachmentParts = mutableListOf<MultipartBody.Part>()

        attachments.forEach {
            it?.let {
                attachmentParts.add(
                    MultipartBody.Part.createFormData(
                        "attachments[]",
                        it.name,
                        it.file.asRequestBody(it.mimeType.toMediaTypeOrNull())
                    )
                )
            }
        }
        return ApiRepository().addMedicalRecord(
            attachmentParts,
            userId.toPart("user_id"),
            name.toPart("name"),
            type.toPart("type"),
            date.toPart("date"),
            patient.toPart("patient")
        )
    }

    suspend fun updateProfileImage(attachment: FileAttachment): Response<UpdateProfileResponse> {
        var m = MultipartBody.Part.createFormData(
            "image",
            attachment.name,
            attachment.file.asRequestBody(attachment.mimeType.toMediaTypeOrNull())
        )
        return ApiRepository().updateProfileImage(m,userUid.toPart("user_id"))
    }

    fun String.toPart(name: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(name, this)
    }

//    RequestBody.create(
//    "image/*".toMediaTypeOrNull(),
//    file
//    )

    suspend fun getSlots(
        type: AppointmentTypeView.APPOINTMENT_TYPE,
        speciality: AppointmentDoctorSpeciality
    ): Response<SlotsResponse> {
        Log.d("Texting111", ApiRepository().getSlots(repository.userUid,type,speciality.name).body().toString())
        return ApiRepository().getSlots(repository.userUid,type,speciality.name)
    }

    suspend fun updateAgeAndGender(age: String, dob: String, gender: String): Response<AgeGenderUpdatedResponse> {
        return ApiRepository().updateAgeAndGender(repository.userUid,age,dob,gender)
    }

    suspend fun getDoctorsForAppointmentBooking(
        userId: String,
        type: String,
        speciality: String
    ): Response<DoctorsForAppointmentResponse>{
        return ApiRepository().getDoctorsForAppointmentBooking(
            userId,
            type,
            speciality
        )
    }

    suspend fun paymentMethods(): Response<PaymentMethodsResponse>{
        return ApiRepository().paymentMethods()
    }

    suspend fun googleLogin(
        email: String,
        name: String,
        imageUrl: String,
        id: String
    ): Response<LoginResponse> {
        return ApiRepository().
        getGoogleLoginResponse(
            GoogleLoginData(
                email,
                name,
                imageUrl,
                id
            )
        )
    }

    suspend fun facebookLogin(
        email: String,
        name: String,
        imageUrl: String,
        id: String
    ): Response<LoginResponse> {
        return ApiRepository().
        getFacebookResponse(
            FacebookLoginData(
                email,
                name,
                imageUrl,
                id
            )
        )
    }

    suspend fun requestOTP(mobile: String): Response<OTPResponse> {
        return ApiRepository().sendOtpForNumberValidation(mobile)
    }

    suspend fun setMobileVerified(mobile: String, userId: String): Response<MobileVerifiedSetResponse> {
        return ApiRepository().setMobileVerified(mobile, userId)
    }

    suspend fun register(registrationData: RegistrationData): Response<RegistrationResponse> {
        return ApiRepository().register(registrationData)
    }

    suspend fun appointments(): Response<AppointmentsResponse> {
        return ApiRepository().appointments(userUid)
    }

    suspend fun requestPasswordResetCode(mobile: String): Response<PasswordResetCodeSendResponse> {
        return ApiRepository().requestPasswordResetCode(mobile)
    }

    suspend fun fetchFewUpcomingAppointments(): Response<AppointmentsResponse> {
        return ApiRepository().fetchFewUpcomingAppointments(userUid)
    }

    suspend fun fetchRecentBookings(): Response<AppointmentsResponse> {
        return ApiRepository().fetchRecentBookings(userUid)
    }

    suspend fun checkAgeAndGender(): Response<AgeAndGenderOkResponse> {
        return ApiRepository().checkAgeAndGender(userUid)
    }

    /////////////////////////////////////////////////
    fun setAppointmentType(type: AppointmentTypeView.APPOINTMENT_TYPE) {
        Prefs.putString(Constants.APPOINTMENT_TYPE_KEY,type.name)
    }

    fun getAppointmentType():AppointmentTypeView.APPOINTMENT_TYPE {
        var type = Prefs.getString(Constants.APPOINTMENT_TYPE_KEY,"")
        return AppointmentTypeView.APPOINTMENT_TYPE.fromString(type?:"")
    }

    fun setSelectedSlot(
        selectedSlotData: SelectedDateSlot,
        type: Type
    ) {
        Prefs.putObject(Constants.SELECTED_SLOT_DATA_KEY,selectedSlotData)
    }

    fun getSelectedSlot(): SelectedDateSlot? {
        return Prefs.getObject<SelectedDateSlot>(
            Constants.SELECTED_SLOT_DATA_KEY,
            SelectedDateSlot::class.java
        )
    }

    fun clearAppointmentData() {
        Prefs.remove(Constants.APPOINTMENT_SPECIALITY_KEY)
        Prefs.remove(Constants.APPOINTMENT_TYPE_KEY)
        Prefs.remove(Constants.SELECTED_SLOT_DATA_KEY)
        Prefs.remove(Constants.PATIENT_NAME_KEY)
        Prefs.remove(Constants.PATIENT_AGE_KEY)
        Prefs.remove(Constants.PATIENT_GENDER_KEY)
        Prefs.remove(Constants.PATIENT_SYMPTOMS_KEY)
        Prefs.remove(Constants.DOCTOR_KEY)
        Prefs.remove(Constants.PATIENT_ID)
    }

    fun setPatientName(name: String) {
        Prefs.putString(Constants.PATIENT_NAME_KEY,name)
    }

    fun getPatientName(): String {
        return Prefs.getString(Constants.PATIENT_NAME_KEY,"")?:""
    }

    fun setPatientAge(age: Int) {
        Prefs.putInt(Constants.PATIENT_AGE_KEY,age)
    }

    fun getPatientAge():Int {
        return Prefs.getInt(Constants.PATIENT_AGE_KEY,0)
    }

    fun setPatientGender(gender: String) {
        Prefs.putString(Constants.PATIENT_GENDER_KEY,gender)
    }

    fun getPatientGender(): String {
        return Prefs.getString(Constants.PATIENT_GENDER_KEY,"")?:""
    }

    fun setPatientSymptoms(symptoms: String) {
        Prefs.putString(Constants.PATIENT_SYMPTOMS_KEY,symptoms)
    }

    fun getPatientSymptoms(): String {
        return Prefs.getString(Constants.PATIENT_SYMPTOMS_KEY,"")?:""
    }

    fun setDoctor(doctor: Doctor) {
        Prefs.putObject(Constants.DOCTOR_KEY,doctor)
    }

    fun getDoctor(): Doctor?{
        return Prefs.getObject<Doctor>(Constants.DOCTOR_KEY,Doctor::class.java)
    }

    fun setAppointmentSpeciality(speciality: String) {
        Prefs.putString(Constants.APPOINTMENT_SPECIALITY_KEY,speciality)
    }

    fun getAppointmentSpeciality(): String {
        return Prefs.getString(Constants.APPOINTMENT_SPECIALITY_KEY,"")?:""
    }

    fun setAppointmentPrice(price: Float) {
        Prefs.putFloat(Constants.APPOINTMENT_PRICE_KEY,price)
    }

    fun getAppointmentPrice(): Float {
        return Prefs.getFloat(Constants.APPOINTMENT_PRICE_KEY)
    }

    fun setLastAppointmentFinalizedId(appointmentId: String) {
        Prefs.putString(Constants.LAST_APPOINTMENT_DONE_ID,appointmentId)
    }

    fun getLastAppointmentFinalizedId(): String {
        return Prefs.getString(Constants.LAST_APPOINTMENT_DONE_ID,"")?:""
    }

    fun clearPrefs() {
        Prefs.clear()
    }

    fun setAgeAndGenderOk(b: Boolean) {
        Prefs.putBoolean(Constants.AGE_AND_GENDER_OK,b)
    }

    fun getAgeAndGenderOk(): Boolean {
        return Prefs.getBoolean(Constants.AGE_AND_GENDER_OK,false)
    }

    suspend fun checkVideoCallAllowed(appointmentId: String,userId: String): Response<VideoCallAllowedResponse> {
        return ApiRepository().checkVideoCallAllowed(userId,"CLIENT",appointmentId)
    }

    suspend fun fetchPrescriptions(userId: String = ""): Response<PrescriptionsResponse> {
        return if(userId.isEmpty){
            ApiRepository().fetchPrescriptions(userUid,"MASTER")
        } else{
            ApiRepository().fetchPrescriptions(userId,"PATIENT")
        }
    }

    suspend fun fetchPrescriptionDetails(prescriptionId: String): Response<PrescriptionDetailsResponse> {
        return ApiRepository().fetchPrescriptionDetails(prescriptionId)
    }

    suspend fun fetchMedicalHistory(): Response<MedicalHistoryResponse> {
        return ApiRepository().fetchMedicalHistory(userUid)
    }

    suspend fun fetchMembers(): Response<com.hellomydoc.data.members.MembersResponse> {
        return ApiRepository().fetchMembers(userUid)
    }

    suspend fun fetchDoctorDetails(doctorId: String): Response<DoctorDetailsResponse> {
        return ApiRepository().fetchDoctorDetails(doctorId)
    }

    fun setPatientId(name: String) {
        Prefs.putString(Constants.PATIENT_ID,name)
    }

    fun getPatientId(): String {
        return Prefs.getString(Constants.PATIENT_ID)?:""
    }

    fun setPatientSource(s: String) {
        Prefs.putString(Constants.PATIENT_SOURCE,s)
    }

    fun getPatientSource(): String {
        return Prefs.getString(Constants.PATIENT_SOURCE)?:""
    }


}