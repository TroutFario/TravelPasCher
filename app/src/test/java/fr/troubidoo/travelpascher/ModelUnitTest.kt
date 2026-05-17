package fr.troubidoo.travelpascher

import fr.troubidoo.travelpascher.ui.screen.MainUiState
import fr.troubidoo.travelpascher.ui.screen.SettingsUiState
import fr.troubidoo.travelpascher.viewmodel.UiPost
import fr.troubidoo.travelpascher.viewmodel.UiUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelUnitTest {

    @Test
    fun uiUser_initialization_isCorrect() {
        val user = UiUser(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            bio = "Hello world",
            profileImageUrl = "http://example.com/image.jpg"
        )

        assertEquals("123", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("Hello world", user.bio)
        assertEquals("http://example.com/image.jpg", user.profileImageUrl)
    }

    @Test
    fun uiUser_defaultValues_areCorrect() {
        val user = UiUser(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        assertEquals("", user.bio)
        assertEquals("", user.profileImageUrl)
    }

    @Test
    fun uiPost_initialization_isCorrect() {
        val now = System.currentTimeMillis()
        val post = UiPost(
            id = "post1",
            userId = "user1",
            username = "Traveler",
            location = "Paris",
            imageUrl = "http://image.com",
            createdAt = now
        )

        assertEquals("post1", post.id)
        assertEquals("Paris", post.location)
        assertEquals(now, post.createdAt)
    }

    @Test
    fun mainUiState_copy_worksAsExpected() {
        val initialState = MainUiState()
        assertEquals(0, initialState.currentTab)
        assertEquals(false, initialState.isSettingsVisible)

        val updatedState = initialState.copy(currentTab = 1, isSettingsVisible = true)
        assertEquals(1, updatedState.currentTab)
        assertEquals(true, updatedState.isSettingsVisible)
    }

    @Test
    fun settingsUiState_update_works() {
        val state = SettingsUiState(firstName = "Alice")
        val newState = state.copy(lastName = "Smith", isLoading = true)

        assertEquals("Alice", newState.firstName)
        assertEquals("Smith", newState.lastName)
        assertTrue(newState.isLoading)
    }
}
