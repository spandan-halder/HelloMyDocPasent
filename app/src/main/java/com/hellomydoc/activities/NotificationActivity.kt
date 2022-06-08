package com.hellomydoc.activities

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.data.Notification
import com.hellomydoc.databinding.ActivityNotificationBinding
import kotlinx.coroutines.launch

class NotificationActivity : AbstractActivity() {
    private var loading = false
    private lateinit var binding: ActivityNotificationBinding
    private var notifications: List<Notification> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            navi().back()
        }

        if(savedInstanceState==null){
            fetchNotifications()
        }
    }

    private fun fetchNotifications() {
        loading = true
        showNotifications()
        lifecycleScope.launch {
            processApi {
                repository.notifications().resp
            }
                .apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    this@NotificationActivity.notifications = notifications?: listOf()
                                }
                            }
                        }
                        else->{
                            this@NotificationActivity.notifications = listOf()
                            R.string.something_went_wrong.string.toast(this@NotificationActivity)
                        }
                    }
                    loading = false
                    showNotifications()
                }
        }
    }

    private fun showNotifications() {
        if(loading){
            binding.cvContent.setContent {
                Box(contentAlignment = Alignment.Center){
                    CircularProgressIndicator(modifier = Modifier.size(50.dp),color = colorResource(id = R.color.red))
                }
            }
        }
        else{
            if(notifications.isEmpty()){
                binding.cvContent.setContent{
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                ImageVector.vectorResource(
                                    id = R.drawable.ic_no_notifications
                                ),
                                "Localized description",
                                modifier = Modifier.size(180.dp),
                            )
                            Text(R.string.no_notifications.string, style = MaterialTheme.typography.h6)
                            Text(R.string.you_dont_have_any_notifications.string)
                        }
                    }
                }
            }
            else{
                binding.cvContent.setContent {
                    LazyColumn{
                        items(notifications){d->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)) {
                                    Image(
                                        painter = rememberImagePainter(
                                            d.image,
                                            builder = {
                                                crossfade(true)
                                                placeholder(R.drawable.ic_outline_notifications_none_24)
                                                transformations(CircleCropTransformation())
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.width(24.dp))
                                    Column(modifier = Modifier.fillMaxWidth()){
                                        Text(d.title, style = MaterialTheme.typography.h6)
                                        Text(d.subtitle, style = MaterialTheme.typography.subtitle1)
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}