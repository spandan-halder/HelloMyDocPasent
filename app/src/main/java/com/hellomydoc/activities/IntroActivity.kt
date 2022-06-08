package com.hellomydoc.activities

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.hellomydoc.*
import com.hellomydoc.databinding.ActivityIntroBinding

class IntroActivity : AbstractActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        binding.viewpager.adapter = IntroPageAdapter(this)
        binding.tablayout.setupWithViewPager(binding.viewpager)
        binding.viewpager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {

            }

            override fun onPageSelected(position: Int) {
                if(position==2){
                    binding.introNextBtn.text = getString(R.string.get_started)
                    binding.skipTextView.hide()
                }
                else{
                    binding.introNextBtn.text = getString(R.string.next)
                    binding.skipTextView.show()
                }

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        binding.skipTextView.setOnClickListener{
            onIntroDone()
        }
        binding.introNextBtn.setOnClickListener{
            val currentItem = binding.viewpager.currentItem
            currentItem.let {
                val nextPage = currentItem+1
                if(nextPage==3){
                    onIntroDone()
                }
                else{
                    goToIntroPage(nextPage)
                }
            }
        }
    }

    private fun onIntroDone() {
        repository.introSeen = true
        goToLogin()
    }

    private fun goToLogin() {
        navi()
            .target(LoginActivity::class.java)
            .go()
    }
    private fun goToIntroPage(nextPage: Int) {
        binding.viewpager.setCurrentItem(nextPage,true)
    }
}

