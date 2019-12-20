package za.co.xisystems.itis_rrm.data._commons

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import za.co.xisystems.itis_rrm.R

class Animations {
    @JvmField val bounce: Animation;
    @JvmField val bounce_short: Animation
    @JvmField val bounce_long: Animation
    @JvmField val bounce_soft: Animation
    @JvmField val bounce_250: Animation
    @JvmField val bounce_500: Animation
    @JvmField val bounce_750: Animation
    @JvmField val bounce_1000: Animation

    @JvmField val scale: Animation
    @JvmField val scale_light: Animation

    @JvmField val click: Animation

    @JvmField val shake_delay: Animation
    @JvmField val shake: Animation

    @JvmField val shake_long: Animation

    @JvmField val shake_longer: Animation

    constructor(context: Context) {
        click = AnimationUtils.loadAnimation(context, R.anim.click)
        bounce = AnimationUtils.loadAnimation(context, R.anim.bounce)
        bounce_short = AnimationUtils.loadAnimation(context, R.anim.bounce_short)
        bounce_long = AnimationUtils.loadAnimation(context, R.anim.bounce_long)
        bounce_250 = AnimationUtils.loadAnimation(context, R.anim.bounce_250)
        bounce_500 = AnimationUtils.loadAnimation(context, R.anim.bounce_500)
        bounce_750 = AnimationUtils.loadAnimation(context, R.anim.bounce_750)
        bounce_1000 = AnimationUtils.loadAnimation(context, R.anim.bounce_1000)
        bounce_soft = AnimationUtils.loadAnimation(context, R.anim.bounce_soft)

        shake_delay = AnimationUtils.loadAnimation(context, R.anim.shake_long_delay)
        shake = AnimationUtils.loadAnimation(context, R.anim.shake)
        shake_long = AnimationUtils.loadAnimation(context, R.anim.shake_long)
        shake_longer = AnimationUtils.loadAnimation(context, R.anim.shake_longer)
        scale = AnimationUtils.loadAnimation(context, R.anim.scale)
        scale_light = AnimationUtils.loadAnimation(context, R.anim.scale_light)
    }
}