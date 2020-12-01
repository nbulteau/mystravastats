package me.nicolas.stravastats.infrastructure.dao

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TokenTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `read Token with username = null`() {
        val json = """
        {
            "token_type":"Bearer",
            "expires_at":1606766292,
            "expires_in":21157,
            "refresh_token":"bd9f499b207c278f60558ecd9fa8f73a31f27146",
            "access_token":"b614b0a03997c322f8d1de27ad6e83c04f137e8d",
            "athlete":{
                "id":11791949,
                "username":null,
                "resource_state":2,
                "firstname":"Fabrice",
                "lastname":"Depaulis",
                "city":"Rennes",
                "state":"Bretagne",
                "country":"France",
                "sex":"M",
                "premium":false,
                "summit":false,
                "created_at":"2015-10-17T12:26:49Z",
                "updated_at":"2020-11-19T16:16:56Z",
                "badge_type_id":0,
                "profile_medium":"https://lh3.googleusercontent.com/...",
                "profile": "https://lh3.googleusercontent.com/...",
                "friend": null,
                "follower": null
            }
        }
        """.trimIndent()

        val token = mapper.readValue(json, Token::class.java)
        Assertions.assertNull(token.athlete.username)
    }

    fun `read Token`() {
        val json = """
        {
            "token_type": "Bearer",
            "expires_at": 1606792265,
            "expires_in": 21548,
            "refresh_token": "0f77aa570889c881531c746160403f452331f1b2",
            "access_token": "fa71ba5e8a69ae19a2c12596eeae7efbf6ed95cd",
            "athlete": {
                "id": 23710236,
                "username": "nbulteau",
                "resource_state": 2,
                "firstname": "Nicolas",
                "lastname": "BULTEAU",
                "city": "Rennes",
                "state": "Bretagne",
                "country": "France",
                "sex": "M",
                "premium": false,
                "summit": false,
                "created_at": "2017-07-26T15:12:22Z",
                "updated_at": "2020-07-21T21:37:47Z",
                "badge_type_id": 0,
                "profile_medium": "https://lh3.googleusercontent.com/a-/AOh14GgwTnA_a2Rn9mKKH4DRHi7GdI0qIbU2yf06zcdHDQ=s96-c",
                "profile": "https://lh3.googleusercontent.com/a-/AOh14GgwTnA_a2Rn9mKKH4DRHi7GdI0qIbU2yf06zcdHDQ=s96-c",
                "friend": null,
                "follower": null
            }
        }
        """.trimIndent()

        val token = mapper.readValue(json, Token::class.java)
        Assertions.assertEquals("nbulteau", token.athlete.username)
    }
}