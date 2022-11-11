package com.hellomydoc

object Constants {
    const val DataPayload = "DATA_PAYLOAD"
    const val consultationFragArg = "ARGUMENT"
    const val MEDICAL_RECORD = "MEDICAL_RECORD"
    const val APPOINTMENT = "APPOINTMENT"
    const val APPOINTMENT_ID = "APPOINTMENT_ID"
    const val PATIENT_SOURCE = "PATIENT_SOURCE"
    const val PATIENT_ID = "PATIENT_ID"
    const val MY_ID = "MY_ID"
    const val PEER_ID_KEY = "PEER_ID_KEY"
    const val SPLASH_DURATION = 2000L
    val MAX_PAYMENT_DONE_CHECK_COUNT = 5
    val PAYMENT_DONE_CONTINUE_CHECK_TIME_DELAY = 2000L
    val OTHER = "OTHER"
    val FEMALE = "FEMALE"
    val MALE = "MALE"
    val SPACE = " "
    val BASE_URL: String = "https://hellomydoc.com/api/v1/welcome/"
    val MAX_CONTENT_LENGHT: Long = 250000L
    val APPLICATON_JSON = "application/json"

    const val AGE_AND_GENDER_OK = "AGE_AND_GENDER_OK"
    const val LAST_APPOINTMENT_DONE_ID = "LAST_APPOINTMENT_DONE_ID"
    const val APPOINTMENT_PRICE_KEY = "APPOINTMENT_PRICE_KEY"
    const val APPOINTMENT_SPECIALITY_KEY = "APPOINTMENT_SPECIALITY_KEY"
    const val DOCTOR_KEY = "DOCTOR_KEY"
    const val PATIENT_SYMPTOMS_KEY = "PATIENT_SYMPTOMS_KEY"
    const val PATIENT_GENDER_KEY = "PATIENT_GENDER_KEY"
    const val PATIENT_AGE_KEY = "PATIENT_AGE_KEY"
    const val PATIENT_NAME_KEY = "PATIENT_NAME_KEY"
    const val SELECTED_SLOT_DATA_KEY = "SELECTED_SLOT_DATA_KEY"
    const val APPOINTMENT_TYPE_KEY = "APPOINTMENT_TYPE_KEY"
    const val INTRO_SEEN_KEY = "INTRO_SEEN_KEY"
    const val MOBILE_KEY = "MOBILE_KEY"

    const val USER_UID = "USER_UID"
    const val FCM_SYNCED = "fcm_synced"
    const val FCM_TOKEN = "fcm_token"
    const val DOCTOR_NAME_KEY = "DOCTOR_NAME_KEY"
    const val DOCTOR_ID_KEY = "DOCTOR_ID_KEY"

    const val LOGIN_DONE = "LOGIN_DONE"
    const val MOBILE_REGEX = "^\\d{10}\$"
    const val EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"
    const val PERSON_REGEX = "^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*\$"
    const val PASSWORD_REGEX = "(?=^.{8,}\$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*\$"
    /////
    const val CHAT_PROJECT_ID_KEY = "chat_project_id"
    const val CHAT_APPLICATION_ID_KEY = "chat_application_id"
    const val CHAT_API_KEY_KEY = "chat_api_key"
    const val CHAT_FIREBASE_APP_NAME = "chat_firebase_app_name"
    const val CHAT_DATABASE_URL = "chat_database_url"
}
