package com.hellomydoc.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.hellomydoc.*
import com.hellomydoc.data.AppointmentSummary
import com.hellomydoc.data.PaymentMethod
import com.hellomydoc.views.PaymentMethodSelectionView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddPaymentDetails : AbstractActivity() {
    private var appointmentId = ""
    private var waitForPaymentdone = false
    private var alv_payment_methods: AnyListView? = null
    private var v_bar: View? = null
    private var bt_continue: Button? = null
    private var paymentMethods: List<PaymentMethod> = listOf()
    var prevSelectedItemPos = -1
    var paymentMethod: PaymentMethod? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment_details)

        bt_continue = findViewById(R.id.bt_continue)

        bt_continue?.setOnClickListener {
            if(paymentMethod==null){
                R.string.please_select_payment_method.string.toast(this)
                return@setOnClickListener
            }
            val userId = repository.userUid
            val speciality = repository.getAppointmentSpeciality()
            val appointmentType = repository.getAppointmentType()
            val price = repository.getAppointmentPrice()
            val slot = repository.getSelectedSlot()
            val patientName = repository.getPatientName()
            val patientAge = repository.getPatientAge()
            val gender = repository.getPatientGender()
            val symptoms = repository.getPatientSymptoms()
            val doctor = repository.getDoctor()

            val appointmentSummary = AppointmentSummary(
                userId = userId,
                speciality = speciality,
                type = appointmentType.name,
                price = price,
                slot = slot,
                patientName = patientName,
                patientAge = patientAge,
                gender = gender,
                symptoms = symptoms,
                doctor = doctor?.id?:"",
                paymentMethod = paymentMethod?.id?:"",
                patientId = repository.getPatientId()
            )


            lifecycleScope.launch {
                wait = true
                processApi {
                    repository.appointmentFinalize(appointmentSummary).resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    this@AddPaymentDetails.appointmentId = this.appointmentId
                                    this@AddPaymentDetails.waitForPaymentdone = this.waitForPaymentDone
                                    onPaymentFinalizeResponse()
                                }
                                else{
                                    message.toast(this@AddPaymentDetails)
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(this@AddPaymentDetails)
                        }
                    }
                }
                wait = false
            }
        }

        alv_payment_methods = findViewById(R.id.alv_payment_methods)
        v_bar = findViewById(R.id.v_bar)
        if(savedInstanceState==null){
            lifecycleScope.launch {
                wait = true
                processApi {
                    repository.paymentMethods().resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    this@AddPaymentDetails.paymentMethods = paymentMethods
                                    showPaymentMethods()
                                }
                                else{
                                    message.toast(this@AddPaymentDetails)
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(this@AddPaymentDetails)
                        }
                    }
                }
                wait = false
            }
        }
    }

    private fun onPaymentFinalizeResponse() {
        if(waitForPaymentdone){
            checkForPaymentDone()
        }
    }

    private var check_payment_done_count = 0
    private fun checkForPaymentDone() {
        lifecycleScope.launch {
            wait = true
            processApi {
                repository.checkPaymentDoneByAppointmentId(appointmentId).resp
            }.apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                if(paymentDone){
                                    onPaymentDone()
                                }
                                else{
                                    ++check_payment_done_count
                                    if(check_payment_done_count>Constants.MAX_PAYMENT_DONE_CHECK_COUNT){
                                        onMaxCheckDone()
                                    }
                                    else{
                                        delay(Constants.PAYMENT_DONE_CONTINUE_CHECK_TIME_DELAY)
                                        checkForPaymentDone()
                                    }
                                }
                            }
                            else{
                                message.toast(this@AddPaymentDetails)
                            }
                        }
                    }
                    else->{
                        wait = false
                        R.string.something_went_wrong.string.toast(this@AddPaymentDetails)
                    }
                }
            }
            wait = false
        }
    }

    private fun onMaxCheckDone() {
        alert(R.string.please_check_sometime_later.string)
    }

    private fun onPaymentDone() {
        repository.setLastAppointmentFinalizedId(appointmentId)
        navi().target(HomeActivity::class.java).back()
    }

    private fun showPaymentMethods() {
        alv_payment_methods?.configure(
            AnyListView.Configurator(
                onScrollPx = {dx,dy->
                    val a = dy.toFloat().coerceAtMost(17f).map(0f,17f,0f,1f)
                    v_bar?.alpha = a
                },
                itemType = {pos->pos},
                state = AnyListView.STATE.DATA,
                itemCount = {paymentMethods?.size?:0},
                onView = {pos,view->
                    ((view as? ViewGroup)?.findViewWithTag("item_view") as? PaymentMethodSelectionView)?.apply {
                        setData(paymentMethods.getOrNull(pos))
                        onCheckedChanged = {
                            Log.d("selection_bug","$pos $it");
                            if(it){
                                if(pos!=prevSelectedItemPos&&prevSelectedItemPos!=-1){
                                    paymentMethods?.getOrNull(prevSelectedItemPos)?.selected = false
                                    alv_payment_methods?.notifyItemChanged(prevSelectedItemPos)
                                }
                                paymentMethods?.getOrNull(pos)?.selected = it
                            }
                            paymentMethod = if(it){
                                prevSelectedItemPos = pos
                                paymentMethods?.getOrNull(pos)
                            } else{
                                //prevSelectedDoctorPos = -1
                                null
                            }
                        }
                    }
                },
                itemView = {pos->
                    PaymentMethodSelectionView(this).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            setMargins(0,20,0,30)
                            tag = "item_view"
                        }
                    }
                }
            )
        )
    }
}