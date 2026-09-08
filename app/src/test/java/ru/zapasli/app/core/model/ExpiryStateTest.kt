package ru.zapasli.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpiryStateTest {
    @Test
    fun `negative days means expired`() {
        assertEquals(ExpiryState.Expired, expiryState(-1))
    }

    @Test
    fun `zero days means today`() {
        assertEquals(ExpiryState.Today, expiryState(0))
    }

    @Test
    fun `one to three days means soon`() {
        assertEquals(ExpiryState.Soon, expiryState(1))
        assertEquals(ExpiryState.Soon, expiryState(3))
    }

    @Test
    fun `more than three days means fresh`() {
        assertEquals(ExpiryState.Fresh, expiryState(4))
    }

    @Test
    fun `missing date has its own state`() {
        assertEquals(ExpiryState.NoDate, expiryState(null))
    }
}
