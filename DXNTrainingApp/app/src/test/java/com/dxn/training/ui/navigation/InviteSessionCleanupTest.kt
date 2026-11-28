package com.dxn.training.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteSessionCleanupTest {
    @Test
    fun clearsInviteWhenSessionEnds() {
        assertTrue(shouldClearPendingLeaderDxnId(previousUserId = "user-1", currentUserId = null))
    }

    @Test
    fun keepsInviteWhileLoggedOutAndWhileSessionSwitchesUser() {
        assertFalse(shouldClearPendingLeaderDxnId(previousUserId = null, currentUserId = null))
        assertFalse(shouldClearPendingLeaderDxnId(previousUserId = "user-1", currentUserId = "user-2"))
    }
}
