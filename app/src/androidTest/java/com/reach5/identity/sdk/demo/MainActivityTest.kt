package com.reach5.identity.sdk.demo

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.reach5.identity.sdk.core.ReachFive
import com.reach5.identity.sdk.core.utils.Failure
import com.reach5.identity.sdk.core.utils.Success
import io.github.cdimascio.dotenv.dotenv
import junit.framework.TestCase.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.ExpectedException
import com.nhaarman.mockitokotlin2.*
import com.reach5.identity.sdk.core.models.*
import org.junit.Ignore
import java.lang.Error
import java.lang.Exception
import java.lang.Thread.sleep
import java.util.*
import kotlin.random.Random

/**
 * These tests use an account with:
 * - the SMS feature enabled
 * - the country set to "France"
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env"
    }
    private val DOMAIN = dotenv["DOMAIN"] ?: ""
    private val CLIENT_ID = dotenv["CLIENT_ID"] ?: ""

    private val TEST_SHOULD_NOT_FAIL = "This test should not have failed because the data are correct."
    private val TEST_SHOULD_FAIL_SCOPE_MISSING = "This test should have failed because the 'full_write' scope is missing."
    private val NO_ID_TOKEN = "No id_token returned, verify that you have the `openid` scope configured in your API Client Settings."

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @get:Rule
    var exceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun testSuccessfulReachFiveClientInstantiation() {
        assertNotNull(instantiateReachFiveClient())
    }

    @Test
    fun testFailedReachFiveClientInstantiation() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Invalid URL host: \"\"")

        instantiateReachFiveClient("", CLIENT_ID)
    }

    private fun aProfile() =
        Profile(
            givenName = "John",
            familyName = "Doe",
            gender = "male",
            email = UUID.randomUUID().let { uuid -> "$uuid@testaccount.io" },
            password = "hjk90wxc"
        )

    // TODO
//    @Test
//    fun testClientUsesConfiguredScopes() {
//        val client = ReachFive(
//            activity = activityRule.activity,
//            sdkConfig = SdkConfig(domain = DOMAIN, clientId = CLIENT_ID),
//            providersCreators = listOf()
//        )
//        val expectedScopes = setOf(
//            "email",
//            "full_write",
//            "openid",
//            "phone",
//            "profile"
//        )
//
//        assertEquals("Pre-init scopes should be empty", emptySet<String>(), client.scope)
//
//        client.initialize(
//            success = {
//                assertEquals("Post-init scopes should correspond to Client Settings", expectedScopes, client.scopes)
//            },
//            failure = { fail(TEST_SHOULD_NOT_FAIL) }
//        )
//
//
//
//        // TODO: replace the `sleep` method by a callback mock
//        sleep(1000)
//    }

    @Test
    fun testSuccessfulSignupWithEmail() {
        val client = instantiateReachFiveClient()

        client.signup(
            aProfile(),
            success = { authToken -> assertNotNull(authToken) },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has received a verification email
    }

    @Test
    fun testFailedSignupWithAlreadyUsedEmail() {
        val client = instantiateReachFiveClient()

        val email = "test_sylvie.lamour@gmail.com"
        val password = "trcnjrn89"

        client.signup(
            Profile(
                givenName = "Sylvie",
                familyName = "Lamour",
                gender = "female",
                addresses = listOf(ProfileAddress(country = "France")),
                email = email,
                password = password
            ),
            success = { authToken -> run {
                // Check that the returned authentication token is not null
                assertNotNull(authToken)

                client.signup(
                    Profile(email = email, password = password),
                    success = { fail("This test should have failed because the email is already used.") },
                    failure = { error ->
                        run {
                            assertEquals(error.message, "Bad Request")
                            assertEquals(error.data?.error, "email_already_exists")
                            assertEquals(error.data?.errorDescription, "Email already in use")
                        }
                    }
                )
            } },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has not received a verification email
    }

    @Test
    fun testFailedSignupWithEmptyEmail() {
        val client = instantiateReachFiveClient()

//        val mocked = object {
//            var onSuccess: Success<AuthToken> = mock()
//        }

//        whenever(mocked.onSuccess(any())).thenThrow(Exception("This test should have failed because the email is empty."))

        client.signup(
            Profile(email = "", password = "jdhkzkzk"),
//            { a -> mocked.onSuccess(a) },
            //mock.onFailure
            success = { fail("This test should have failed because the email is empty.") },
            failure = { error -> run {
                assertEquals(error.message, "Bad Request")
                assertEquals(error.data?.error, "invalid_request")
                assertEquals(error.data?.errorDescription, "Validation failed")
                assertEquals(error.data?.errorDetails?.get(0)?.field, "data.email")
                assertEquals(error.data?.errorDetails?.get(0)?.message, "Must be a valid email")
            } }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        //val verify = verify(mock, timeout(1000).times(1))

        //verify.onSuccess(check { fail("This test should have failed because the email is empty.") })
        //verify.onFailure(ReachFiveError(message = "Bad Request"))

        // TODO: check that the profile has not received a verification email
    }

    @Test
    fun testSuccessfulSignupWithPhoneNumber() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(
                givenName = "Alita",
                familyName = "Sylvain",
                gender = "female",
                phoneNumber = "+33656244150",
                password = "hjk90wxc"
            ),
            success = { authToken -> assertNotNull(authToken) },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has received a verification SMS
    }

    @Test
    fun testSuccessfulSignupWithLocalPhoneNumber() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(
                givenName = "Belda",
                familyName = "Fortier",
                gender = "female",
                phoneNumber = "0750253354",
                password = "hjk00exc"
            ),
            success = { authToken -> assertNotNull(authToken) },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has received a verification SMS
    }

    @Test
    fun testFailedSignupWeakPassword() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(email = "test_marshall.babin@gmail.fr", password = "toto"),
            success = { fail("This test should have failed because the password is too weak.") },
            failure = { error -> run {
                assertEquals(error.message, "Bad Request")
                assertEquals(error.data?.errorDescription, "Validation failed")
                assertEquals(error.data?.errorDetails?.get(0)?.message, "Password too weak")
            } }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has not received a verification email
    }

    @Test
    fun testFailedSignupAuthTokenRetrievalWithMissingScope() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(
                name = "Jeanette Hachee",
                email = "test_jeanette.hachee@gmail.com",
                password = "jdhkzkzk"
            ),
            listOf(),
            {},
            { error -> assertEquals(error.message, NO_ID_TOKEN) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has not received a verification email
    }

    @Test
    fun testSuccessfulLoginWithEmail() {
        val client = instantiateReachFiveClient()

        val email = "test_chad.morrison@gmail.com"
        val password = "frkjfkrnf"

        client.signup(
            Profile(
                givenName = "Chad",
                familyName = "Morrison",
                gender = "male",
                email = email,
                password = password
            ),
            success = {
                client.loginWithPassword(
                    email,
                    password,
                    success = { authToken -> assertNotNull(authToken) },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) },
//            scopes =
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulLoginWithPhoneNumber() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33782234140"
        val password = "jfk7!fckook"

        client.signup(
            Profile(
                givenName = "Lucas",
                familyName = "Girard",
                gender = "male",
                phoneNumber = phoneNumber,
                password = password
            ),
            success = {
                client.loginWithPassword(
                    phoneNumber,
                    password,
                    success = { authToken -> assertNotNull(authToken) },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedLoginWithNonExistingIdentifier() {
        val client = instantiateReachFiveClient()

        client.loginWithPassword(
            "test_audric.louis@gmail.com",
            "kfjrifjr",
            success = { fail("This test should have failed because the profile is not registered.") },
            failure = { error -> run {
                assertEquals(error.message, "Bad Request")
                assertEquals(error.data?.error, "invalid_grant")
                assertEquals(error.data?.errorDescription, "Invalid email or password")
            } }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedLoginWithWrongPassword() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33682234940"

        client.signup(
            Profile(
                givenName = "Florus",
                familyName = "Lejeune",
                gender = "male",
                phoneNumber = phoneNumber,
                password = "UCrcF4RH"
            ),
            success = {
                client.loginWithPassword(
                    phoneNumber,
                    "6sPePvkY",
                    success = { fail("This test should have failed because the password is incorrect.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Bad Request")
                        assertEquals(error.data?.error, "invalid_grant")
                        assertEquals(error.data?.errorDescription, "Invalid phone number or password")
                    } }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedLoginAuthTokenRetrievalWithMissingScope() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33754234152"
        val password = "9fmHmFWm"

        client.signup(
            Profile(
                givenName = "Clarimunda",
                familyName = "Devoe",
                gender = "other",
                phoneNumber = phoneNumber,
                password = password
            ),
            success = {
                client.loginWithPassword(
                    phoneNumber,
                    password,
                    listOf(),
                    {},
                    { error -> assertEquals(error.message, NO_ID_TOKEN) }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Ignore
    @Test
    fun testSuccessVerifyPhoneNumber() {
        // TODO : write this test once we can get the SMS list from Twilio
    }

    @Test
    fun testFailedVerifyPhoneNumberWithWrongCode() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33771221392"

        client.signup(
            Profile(
                givenName = "Damien",
                familyName = "Cannon",
                gender = "other",
                phoneNumber = phoneNumber,
                password = "9fmHmFWm"
            ),
            client.defaultScope.plus("full_write"),
            success = { authToken ->
                client.verifyPhoneNumber(
                    authToken,
                    phoneNumber,
                    "500",
                    { fail("This test should have failed because the verification code is incorrect.") },
                    { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "invalid_grant")
                        assertEquals(error.data?.errorDescription, "Invalid verification code")
                    } }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulEmailUpdate() {
        val client = instantiateReachFiveClient()

        val updatedEmail = "test_merci.blais@gmail.com"

        client.signup(
            Profile(
                givenName = "Merci",
                familyName = "Blais",
                gender = "female",
                email = "test_merssi.blais@gmail.com",
                password = "5HXuhmhu"
            ),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updateEmail(
                    authToken,
                    updatedEmail,
                    success = { updatedProfile -> run {
                        assertNotNull(updatedProfile)
                        assertEquals(updatedProfile.email, updatedEmail)
                    } },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedEmailUpdateWithSameEmail() {
        val client = instantiateReachFiveClient()

        val email = "test_adrien.hernandez@gmail.com"

        client.signup(
            Profile(email = email, password = "2mmtyiQb"),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updateEmail(
                    authToken,
                    email,
                    success = { fail("This test should have failed because the email has not changed.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Bad Request")
                        assertEquals(error.data?.error, "email_already_exists")
                        assertEquals(error.data?.errorDescription, "Email already in use")
                    } }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedEmailUpdateWithMissingScope() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(givenName = "Holly", gender = "female", email = "test_holy.camacho@gmail.com", password = "KnpP8G95"),
            success = { authToken ->
                client.updateEmail(
                    authToken,
                    "test_holly.camacho@gmail.com",
                    success = { fail(TEST_SHOULD_FAIL_SCOPE_MISSING) },
                    failure =  { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "insufficient_scope")
                        assertEquals(error.data?.errorDescription, "The token does not contain the required scope: full_write")
                    } }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulPhoneNumberUpdate() {
        val client = instantiateReachFiveClient()

        val updatedPhoneNumber = "+33762342563"

        client.signup(
            Profile(givenName = "Tony", familyName = "Howard", gender = "male", phoneNumber = "+33765342563", password = "2m8WrJQf"),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePhoneNumber(
                    authToken,
                    updatedPhoneNumber,
                    success = { updatedProfile -> run {
                        assertNotNull(updatedProfile)
                        assertEquals(updatedProfile.phoneNumber, updatedPhoneNumber)
                    } },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulPhoneNumberUpdateWithSameNumber() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33772342563"

        client.signup(
            Profile(phoneNumber = phoneNumber, password = "2m8WrJQf"),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePhoneNumber(
                    authToken,
                    phoneNumber,
                    success = { updatedProfile -> run {
                        assertNotNull(updatedProfile)
                        assertEquals(updatedProfile.phoneNumber, phoneNumber)
                    } },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedPhoneNumberUpdateWithMissingScope() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(givenName = "Tom", gender = "male", phoneNumber = "+33771312563", password = "f2923kSN"),
            success = { authToken ->
                client.updatePhoneNumber(
                    authToken,
                    "+33771312564",
                    success = { fail(TEST_SHOULD_FAIL_SCOPE_MISSING) },
                    failure =  { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "insufficient_scope")
                        assertEquals(error.data?.errorDescription, "The token does not contain the required scope: full_write")
                    } }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulProfileUpdate() {
        val client = instantiateReachFiveClient()

        val email = "test_christabel.couet@gmail.com"
        val password = "n8URZzWf"
        val updatedGivenName = "Christelle"
        val updatedFamilyName = "Couet"

        client.signup(
            Profile(givenName = "Christabel", familyName = "Coue", gender = "female", email = email, password = password),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client
                .updateProfile(
                    authToken,
                    Profile(givenName = updatedGivenName, familyName = updatedFamilyName),
                    { updatedProfile ->
                        run {
                            assertNotNull(updatedProfile)
                            assertEquals(updatedProfile.email, email)
                            assertEquals(updatedProfile.givenName, updatedGivenName)
                            assertEquals(updatedProfile.familyName, updatedFamilyName)
                            assertEquals(updatedProfile.gender, "female")
                        }
                    },
                    { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedProfileUpdateWithMissingScope() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(givenName = "Petter", gender = "male", email = "test_petter.desimone@gmail.com", password = "ZhVaJP2v"),
            success = { authToken ->
                client
                    .updateProfile(
                        authToken,
                        Profile(givenName = "Peter"),
                        { fail(TEST_SHOULD_FAIL_SCOPE_MISSING) },
                        { error -> run {
                            assertEquals(error.message, "Technical Error")
                            assertEquals(error.data?.error, "insufficient_scope")
                            assertEquals(error.data?.errorDescription, "The token does not contain the required scope: full_write")
                        } }
                    )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulPasswordUpdateWithFreshAccessToken() {
        val client = instantiateReachFiveClient()

        val email = "test_marquis.jones@gmail.com"
        val oldPassword = "gVc7piBn"
        val newPassword = "ZPf7LFtc"

        client.signup(
            Profile(givenName = "Marquis", familyName = "Jones", gender = "male", email = email, password = oldPassword),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePassword(
                    authToken,
                    UpdatePasswordRequest.FreshAccessTokenParams(newPassword),
                    successWithNoContent = {
                        client.loginWithPassword(
                            email,
                            newPassword,
                            success = { authToken -> assertNotNull(authToken) },
                            failure = { fail(TEST_SHOULD_NOT_FAIL) }
                        )
                    },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(2000)
    }

    @Test
    fun testSuccessfulPasswordUpdateWithAccessToken() {
        val client = instantiateReachFiveClient()

        val email = "test_francoise.leveille@gmail.com"
        val oldPassword = "f7SM4ryE"
        val newPassword = "XLpYXz7z"

        client.signup(
            Profile(givenName = "Françoise", familyName = "Léveillé", gender = "female", email = email, password = oldPassword),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePassword(
                    authToken,
                    UpdatePasswordRequest.AccessTokenParams(oldPassword, newPassword),
                    successWithNoContent = {
                        client.loginWithPassword(
                            email,
                            newPassword,
                            success = { authToken -> assertNotNull(authToken) },
                            failure = { fail(TEST_SHOULD_NOT_FAIL) }
                        )
                    },
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(2000)
    }

    @Test
    fun testFailedPasswordUpdateWithAccessTokenWithSamePassword() {
        val client = instantiateReachFiveClient()

        val email = "test_jeanette.laliberte@gmail.com"
        val oldPassword = "d9qtNk75"

        client.signup(
            Profile(givenName = "Jeanette", familyName = "Laliberté", gender = "female", email = email, password = oldPassword),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePassword(
                    authToken,
                    UpdatePasswordRequest.AccessTokenParams(oldPassword, oldPassword),
                    successWithNoContent = { fail("This test should have failed because the password has not changed.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Bad Request")
                        assertEquals(error.data?.error, "invalid_request")
                        assertEquals(error.data?.errorDescription, "New password should be different from the old password")
                    } }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedPasswordUpdateWithEmailAndWrongCode() {
        val client = instantiateReachFiveClient()

        val email = "test_dalmace.legault@gmail.com"

        client.signup(
            Profile(givenName = "Dalmace", familyName = "Legault", gender = "female", email = email, password = "AkRxUS2C"),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePassword(
                    authToken,
                    UpdatePasswordRequest.EmailParams(email, "234", "DoTJR39D"),
                    successWithNoContent = { fail("This test should have failed because the verification code is incorrect.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "invalid_grant")
                        assertEquals(error.data?.errorDescription, "Invalid verification code")
                    } }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testFailedPasswordUpdateWithPhoneNumberAndWrongCode() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33643890990"

        client.signup(
            Profile(givenName = "Royce", familyName = "Tardif", gender = "male", phoneNumber = phoneNumber, password = "w2saTZPn"),
            client.defaultScope.plus("full_write"),
            { authToken ->
                client.updatePassword(
                    authToken,
                    UpdatePasswordRequest.SmsParams(phoneNumber, "908", "qdnsgRc3"),
                    successWithNoContent = { fail("This test should have failed because the verification code is incorrect.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "invalid_grant")
                        assertEquals(error.data?.errorDescription, "Invalid verification code")
                    } }
                )
            },
            { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulRequestPasswordResetWithEmail() {
        val client = instantiateReachFiveClient()

        val email = "test_sidney.stanley@gmail.com"

        client.signup(
            Profile(givenName = "Sidney", familyName = "Stanley", gender = "male", email = email, password = "AZE9pX7U"),
            success = { authToken ->
                client.requestPasswordReset(
                    authToken,
                    email = email,
                    successWithNoContent = {},
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has received an email
    }

    @Test
    fun testSuccessfulRequestPasswordResetWithPhoneNumber() {
        val client = instantiateReachFiveClient()

        val phoneNumber = "+33789345263"

        client.signup(
            Profile(givenName = "Maria", familyName = "Tynan", gender = "female", phoneNumber = phoneNumber, password = "FHEq5mw5"),
            success = { authToken ->
                client.requestPasswordReset(
                    authToken,
                    phoneNumber = phoneNumber,
                    successWithNoContent = {},
                    failure = { fail(TEST_SHOULD_NOT_FAIL) }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)

        // TODO: check that the profile has received an SMS
    }

    @Test
    fun testFailedRequestPasswordResetWithNoIdentifier() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(phoneNumber = "+33780345263", password = "5mCFFhKt"),
            success = { authToken ->
                client.requestPasswordReset(
                    authToken,
                    successWithNoContent = { fail("This test should have failed because neither the email or the phone number are provided.") },
                    failure = { error -> run {
                        assertEquals(error.message, "Technical Error")
                        assertEquals(error.data?.error, "invalid_grant")
                        assertEquals(error.data?.errorDescription, "Invalid credentials")
                    } }
                )
            },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    @Test
    fun testSuccessfulLogout() {
        val client = instantiateReachFiveClient()

        client.signup(
            Profile(
                givenName = "Audric",
                familyName = "Francoeur",
                gender = "male",
                email = "test_audric.francoeur@gmail.com",
                password = "ZL7czYWw"
            ),
            success = { client.logout(successWithNoContent = {}, failure = { fail(TEST_SHOULD_NOT_FAIL) }) },
            failure = { fail(TEST_SHOULD_NOT_FAIL) }
        )

        // TODO: replace the `sleep` method by a callback mock
        sleep(1000)
    }

    private fun instantiateReachFiveClient(domain: String = DOMAIN, clientId: String = CLIENT_ID): ReachFive {
        val sdkConfig = SdkConfig(domain = domain, clientId = clientId)

        return ReachFive(
            activity = activityRule.activity,
            sdkConfig = sdkConfig,
            providersCreators = listOf()
        ).initialize()
    }

}

