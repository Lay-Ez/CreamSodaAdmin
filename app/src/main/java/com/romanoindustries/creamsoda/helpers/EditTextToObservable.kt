package com.romanoindustries.creamsoda.helpers

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import io.reactivex.rxjava3.core.Observable

fun EditText.textChanges() : Observable<String> {
    return Observable.create {subscriber ->
        this.addTextChangedListener( object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                subscriber.onNext(s.toString())
            }
        })
    }
}

fun EditText.trimmedText(): String {
    return text.toString().trim()
}