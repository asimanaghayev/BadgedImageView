package com.example.badgedimageview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * This Image View helps to show badge over view. <br></br>
 * By default badge will be shown right top position. <br></br>
 * It is possible to show badge with count and without count.
 */
class BadgedImageView : AppCompatImageView {
    private val DEFAULT_MAX_BADGE_COUNT = 9

    private lateinit var badgeIcon: Drawable
    private var badgeRadius = 0
    private var badgeBorderWidth = 0
    private var badgeText: String? = null
    private var badgeEnabled = false

    @DrawableRes
    private var customBadgeIcon = 0
    private lateinit var badgePaint: Paint
    private lateinit var badgeBounds: Rect
    private var badgeOutlinePaint: Paint? = null

    /**
     * Badge positions
     */
    private var badgeTextPositionX = 0
    private var badgeTextPositionY = 0

    /**
     * Padding for Badge
     */
    private var padding = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttributes(attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle) {
        applyAttributes(attrs)
        init()
    }

    private fun applyAttributes(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val typedArray: TypedArray = context.theme.obtainStyledAttributes(attrs,
            R.styleable.BadgedImageView, 0, 0)
        try {
            setBadgeEnabled(typedArray.getBoolean(
                R.styleable.BadgedImageView_badgeEnabled,
                false
            ))
            setBadgeRadius(typedArray.getDimensionPixelSize(
                R.styleable.BadgedImageView_badgeRadius,
                R.dimen.badge_icon_size
            ))
            setBadgeBorderWidth(typedArray.getDimensionPixelSize(
                R.styleable.BadgedImageView_badgeBorderWidth,
                R.dimen.badge_default_border
            ))
            setCustomBadgeIcon(typedArray.getResourceId(
                R.styleable.BadgedImageView_badgeCustomIcon,
                0
            ))
        } finally {
            typedArray.recycle()
        }
    }

    private fun init() {
        badgePaint = Paint()
        badgeBounds = Rect()

        badgeOutlinePaint = Paint()
        badgeOutlinePaint?.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            strokeWidth = badgeBorderWidth.toFloat()
            style = Paint.Style.STROKE
        }

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (badgeEnabled) {
            badgeIcon = ContextCompat.getDrawable(context, R.drawable.ic_tab_badge)!!
            padding = ((paddingTop / 1.5).roundToInt())
        }
        if (badgeEnabled && badgeText?.isNotEmpty() == true) {
            badgePaint.color = ContextCompat.getColor(context, android.R.color.white)
            badgePaint.textSize = resources.getDimension(R.dimen.badge_text)
            badgePaint.getTextBounds(badgeText, 0, badgeText!!.length, badgeBounds)
            badgeTextPositionX = padding + badgeRadius / 2 + badgeBounds.width() / 2 + 2
            badgeTextPositionY = padding + badgeRadius / 2 + badgeBounds.height() / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (badgeEnabled) {
            badgeIcon.setBounds(
                width - badgeRadius - padding,
                padding,
                width - padding,
                badgeRadius + padding
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                badgeIcon.setTint(ContextCompat.getColor(context, R.color.main_red))
            } else {
                badgeIcon.mutate().setColorFilter(resources.getColor(R.color.main_red),
                    PorterDuff.Mode.SRC_IN)
            }
            badgeIcon.draw(canvas)
            val badgeRadius = badgeRadius / 2
            badgeOutlinePaint?.let {
                canvas.drawCircle(
                    (width - badgeRadius - padding).toFloat(),
                    (padding + badgeRadius).toFloat(),
                    badgeRadius.toFloat(),
                    it
                )
            }
        }
        if (badgeEnabled && customBadgeIcon != 0) {
            val bitmap: Bitmap = BitmapFactory.decodeResource(resources, customBadgeIcon)
            canvas.drawBitmap(bitmap, (
                    width - badgeRadius - padding).toFloat(),
                padding.toFloat(), null)
        } else if (badgeEnabled) {
            badgeText?.let {
                canvas.drawText(it, (
                        width - badgeTextPositionX).toFloat(),
                    badgeTextPositionY.toFloat(),
                    badgePaint
                )
            }
        }
    }

    /**
     * This method is for setting badge icon size. <br></br>
     * By default radius id is [R.dimen.badge_icon_size].
     *
     * @param dimen the resource id of dimension.
     */
    fun setBadgeRadius(@DimenRes dimen: Int) {
        badgeRadius = resources.getDimension(dimen).roundToInt()
    }

    /**
     * This method is for setting badge border width. <br></br>
     *
     * @param dimen the resource id of dimension.
     */
    fun setBadgeBorderWidth(@DimenRes dimen: Int) {
        badgeBorderWidth = resources.getDimension(dimen).roundToInt()
    }

    /**
     * This method sets the count of badge. <br></br>
     * It normalizes badge count with [getNotificationBadge] method.
     *
     * @param count notification count that needs to be displayed.
     */
    fun setBadgeCount(count: Int) {
        if (count > 0) {
            badgeText = getNotificationBadge(count)
            setBadgeEnabled(true)
            invalidate()
        } else if (badgeEnabled) {
            setBadgeEnabled(false)
            invalidate()
        }
    }

    /**
     * This method displays badge icon to top right side of view. <br></br>
     * By default it only shows icon. To show notification count along with badge icon call [.setBadgeCount] method.
     *
     * @param badgeEnabled true for displaying badge, false for hiding badge icon.
     */
    fun setBadgeEnabled(badgeEnabled: Boolean) {
        this.badgeEnabled = badgeEnabled
    }

    /**
     * This method is for setting custom drawable over badge. <br></br>
     * Count can`t be displayed if icon is already set to badge.
     *
     * @param res The resource id of icon for badge.
     */
    fun setCustomBadgeIcon(@DrawableRes res: Int) {
        customBadgeIcon = res
    }


    /**
     * This method is for normalizing count for notification badge. <br></br>
     *
     * @param count of notification before normalization.
     * @return {@value #DEFAULT_MAX_BADGE_COUNT}+ if count is more than [.DEFAULT_MAX_BADGE_COUNT], count otherwise.
     */
    private fun getNotificationBadge(count: Int): String {
        return if (count > DEFAULT_MAX_BADGE_COUNT) "$DEFAULT_MAX_BADGE_COUNT+" else count.toString()
    }
}