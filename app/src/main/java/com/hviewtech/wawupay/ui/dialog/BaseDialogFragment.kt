package com.hviewtech.wawupay.ui.dialog

import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatDialogFragment
import android.view.*
import com.hviewtech.wawupay.R
import com.hviewtech.wawupay.common.ext.toast
import com.hviewtech.wawupay.common.utils.DialogUtils
import com.hviewtech.wawupay.contract.Contract
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import pub.devrel.easypermissions.EasyPermissions

/**
 * @author Eric
 * @version V1.0
 * @Description:
 * @date
 */
abstract class BaseDialogFragment : AppCompatDialogFragment(), Contract.View {
  protected val provider: LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)

  protected var mContext: Context? = null


  protected open fun getStyle(): Int {
    return R.style.BottomDialog
  }

  protected open fun getAnimation(): Int {
    return R.style.AnimRight
  }

  abstract fun getLayoutId(): Int

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    mContext = context
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
    val dialog = Dialog(mContext!!, getStyle())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 设置Content前设定

    //        跳转newActivity 键盘弹出
    //        mEt_info.setFocusable(true);
    //        mEt_info.setFocusableInTouchMode(true);
    //        mEt_info.requestFocus();

    dialog.setCanceledOnTouchOutside(false) // 外部点击取消
    val window = dialog.window
    if (window != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      }

      val animation = getAnimation()
      if (animation != 0) {
        window.setWindowAnimations(animation)
      }
      if (needKeyboardAutoPush()) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
      }
    }

    return dialog
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

    return inflater.inflate(getLayoutId(), container, false)

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mContext = context
    init()
  }

  override fun onStart() {
    super.onStart()

    // 设置宽度为屏宽, 靠近屏幕底部。
    val window = dialog.window
    //        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    val lp = window!!.attributes
    lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
    lp.height = WindowManager.LayoutParams.MATCH_PARENT
    lp.gravity = Gravity.TOP

    //        lp.dimAmount = 0;
    window.attributes = lp
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }

  // TODO: 2018/4/17 待验证
  override fun show(manager: FragmentManager?, tag: String) {
    manager ?: return
    val fragment = manager.findFragmentByTag(tag)
    if (fragment != null) {
      manager.beginTransaction().remove(fragment).commitAllowingStateLoss()
    }
    super.show(manager, tag)
  }


  protected open fun needKeyboardAutoPush(): Boolean {
    return false
  }

  abstract fun init()


  override fun showloading() {
    mContext ?: return
    DialogUtils.showProgressLoading(mContext!!, provider.bindToLifecycle())
  }

  override fun dismissloading(error: Boolean, handler: () -> Unit) {
    if (error) {
      DialogUtils.dismissErrorLoading(provider.bindToLifecycle(), handler)
    } else {
      DialogUtils.dismissProgressLoading(provider.bindToLifecycle(), handler)
    }
  }

  override fun showError(msg: String?) {
    toast(msg)
  }

}
