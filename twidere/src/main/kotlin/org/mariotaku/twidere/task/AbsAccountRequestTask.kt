/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import android.support.v4.util.ArraySet
import android.widget.Toast
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.delete
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.insert
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.promise.UpdateStatusPromise
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts

abstract class AbsAccountRequestTask<Params, Result, Callback>(context: Context, val accountKey: UserKey?) :
        ExceptionHandlingAbstractTask<Params, Result, MicroBlogException, Callback>(context) {
    final override val exceptionClass = MicroBlogException::class.java

    final override fun onExecute(params: Params): Result {
        if (accountKey == null) throw AccountNotFoundException()
        val account = AccountManager.get(context).getDetailsOrThrow(accountKey, true)
        val draft = createDraft()
        var draftId = -1L
        if (draft != null) {
            val uri = context.contentResolver.insert(Drafts.CONTENT_URI, draft)
            draftId = uri?.lastPathSegment.toLongOr(-1)
        }
        if (draftId != -1L) {
            UpdateStatusPromise.addSendingDraftId(context.contentResolver, draftId)
        }
        try {
            val result = onExecute(account, params)
            onCleanup(account, params, result, null)
            if (draftId != -1L) {
                context.contentResolver.delete(Drafts.CONTENT_URI, draftId)
            }
            return result
        } catch (e: MicroBlogException) {
            onCleanup(account, params, null, e)
            if (draftId != 1L && deleteDraftOnException(account, params, e)) {
                context.contentResolver.delete(Drafts.CONTENT_URI, draftId)
            }
            throw e
        } finally {
            if (draftId != -1L) {
                UpdateStatusPromise.removeSendingDraftId(context.contentResolver, draftId)
            }
        }
    }

    protected abstract fun onExecute(account: AccountDetails, params: Params): Result

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result?, exception: MicroBlogException?) {
        if (result != null) {
            onCleanup(account, params, result)
        } else if (exception != null) {
            onCleanup(account, params, exception)
        }
    }

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result) {}
    protected open fun onCleanup(account: AccountDetails, params: Params, exception: MicroBlogException) {}

    protected open fun createDraft(): Draft? = null

    protected open fun deleteDraftOnException(account: AccountDetails, params: Params, exception: MicroBlogException): Boolean = false

    override fun onException(callback: Callback?, exception: MicroBlogException) {
        Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
    }

    abstract class ObjectIdTaskCompanion {
        private val taskIds = ArraySet<ObjectId<String>>()

        fun addTaskId(accountKey: UserKey?, id: String?) {
            if (accountKey == null || id == null) return
            taskIds.add(ObjectId(accountKey, id))
        }

        fun removeTaskId(accountKey: UserKey?, id: String?) {
            if (accountKey == null || id == null) return
            taskIds.removeAll { it.accountKey == accountKey && it.id == id }
        }

        fun isRunning(accountKey: UserKey?, id: String?): Boolean {
            return taskIds.any { it.accountKey == accountKey && it.id == id }
        }
    }
}
