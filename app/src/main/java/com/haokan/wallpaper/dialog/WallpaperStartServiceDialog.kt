package com.haokan.wallpaper.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.text.style.LineHeightSpan
import android.view.*
import android.widget.TextView
import com.haokan.mytestimageview.R

/**
 * 确认打开wallpaper service 弹窗
 */
class WallpaperStartServiceDialog(context: Context, themeId: Int) : Dialog(context, themeId) {
    class Builder(private val context: Context) {

        private var title: String? = null
        private var message: String? = null
        private var positiveButtonContent: String? = null
        private var negativeButtonContent: String? = null
        private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
        private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
        private var mContentView: View? = null
        private var color: Int = 0
        private var widthOffSize: Float = 0.toFloat()
        private var heightOffSize: Float = 0.toFloat()
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setPositiveButton(text: String, listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonContent = text
            this.positiveButtonClickListener = listener
            return this
        }

        fun setNegativeButton(text: String, listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonContent = text
            this.negativeButtonClickListener = listener
            return this
        }

        fun setPositiveTextColor(color: Int): Builder {
            this.color = color
            return this
        }

        fun setWidthOffset(widthoffSet: Float): Builder {
            this.widthOffSize = widthoffSet
            return this
        }

        fun setHeightOffset(heightSpan: Float): Builder {
            this.heightOffSize = heightSpan
            return this
        }

        fun setContentView(v: View): Builder {
            this.mContentView = v
            return this
        }


        fun onCreate(): WallpaperStartServiceDialog {
            /**
             * 初始化dialog
             */
            val dialog = WallpaperStartServiceDialog(context, R.style.MyDialog)
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogLayoutView = inflater.inflate(R.layout.dialog_start_service_layout, null)
            dialog.addContentView(dialogLayoutView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT))
            if (!TextUtils.isEmpty(title)) {
                (dialogLayoutView.findViewById<View>(R.id.tv_title) as TextView).text = title
                (dialogLayoutView.findViewById<View>(R.id.tv_title) as TextView).visibility =
                    View.VISIBLE
            } else {
                (dialogLayoutView.findViewById<View>(R.id.tv_title) as TextView).visibility =
                    View.GONE
            }
            if (!TextUtils.isEmpty(message)) {
                (dialogLayoutView.findViewById<View>(R.id.tv_message) as TextView).text = message
            }
            if (color != 0) {
                val viewById = dialogLayoutView.findViewById<View>(R.id.tv_ok) as TextView
                viewById.setTextColor(color)

            }
            if (!TextUtils.isEmpty(positiveButtonContent)) {
                val viewById = dialogLayoutView.findViewById<View>(R.id.tv_ok) as TextView
                viewById.text = positiveButtonContent
                if (positiveButtonClickListener != null) {
                    viewById.setOnClickListener {
                        positiveButtonClickListener!!.onClick(dialog, -1)
                    }
                }
            } else {
                (dialogLayoutView.findViewById<View>(R.id.tv_ok) as TextView).visibility = View.GONE
            }
            if (!TextUtils.isEmpty(negativeButtonContent)) {
                val viewNegative = dialogLayoutView.findViewById<View>(R.id.tv_cancel) as TextView
                viewNegative.text = negativeButtonContent
                if (negativeButtonClickListener != null) {
                    viewNegative.setOnClickListener {
                        negativeButtonClickListener!!.onClick(dialog, -2)
                    }
                }
            } else {
                (dialogLayoutView.findViewById<View>(R.id.tv_cancel) as TextView).visibility =
                    View.GONE
            }
            //将初始化完整的 布局添加到dialog中
            dialog.setContentView(dialogLayoutView)
            dialog.setCanceledOnTouchOutside(true)
            val window = dialog.window

            val params: WindowManager.LayoutParams? = window?.attributes
            params?.gravity = Gravity.CENTER
            window?.setAttributes(params)
            return dialog
        }

    }
}