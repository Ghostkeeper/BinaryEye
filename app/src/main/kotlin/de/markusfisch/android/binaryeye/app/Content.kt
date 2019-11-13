package de.markusfisch.android.binaryeye.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import android.view.View
import de.markusfisch.android.binaryeye.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun setFragment(
	fm: androidx.fragment.app.FragmentManager?,
	fragment: androidx.fragment.app.Fragment
) {
	fm?.let { getTransaction(fm, fragment).commit() }
}

fun addFragment(
	fm: androidx.fragment.app.FragmentManager?,
	fragment: androidx.fragment.app.Fragment
) {
	fm?.let { getTransaction(fm, fragment).addToBackStack(null).commit() }
}

@SuppressLint("CommitTransaction")
private fun getTransaction(
	fm: androidx.fragment.app.FragmentManager,
	fragment: androidx.fragment.app.Fragment
) = fm.beginTransaction().replace(R.id.content_frame, fragment)

suspend inline fun View.useVisibility(
	whileExecuting: Int = View.VISIBLE,
	otherwise: Int = View.GONE,
	crossinline block: suspend () -> Unit
) {
	if (visibility == whileExecuting) {
		return
	}
	withContext(Dispatchers.Main) {
		visibility = whileExecuting
	}
	try {
		block()
	} finally {
		withContext(Dispatchers.Main) {
			visibility = otherwise
		}
	}
}

suspend inline fun <T : Any> alertDialog(
	context: Context,
	crossinline build: AlertDialog.Builder.(resume: (T?) -> Unit) -> Unit
): T? = withContext(Dispatchers.Main) {
	suspendCoroutine<T?> { continuation ->
		AlertDialog.Builder(context).apply {
			setOnCancelListener {
				continuation.resume(null)
			}
			build(continuation::resume)
			show()
		}
	}
}
