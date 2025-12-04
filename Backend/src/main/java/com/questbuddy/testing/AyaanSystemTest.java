package com.questbuddy.testing;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * System / end-to-end tests hitting the deployed QuestBuddy backend.
 */
public class AyaanSystemTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://coms-3090-026.class.las.iastate.edu";
        RestAssured.port = 8080;
    }

    /**
     * TEST 1 (non-trivial):
     * - Call /api/v1/auth/signup/batch with 3 entries (2 valid, 1 invalid).
     * - Assert only the 2 valid users are created (length == 2).
     * - Then call /api/v2/users and verify those usernames appear in the admin list.
     */
    @Test
    public void signupBatch_createsOnlyValidUsers_andVisibleInAdminList() throws JSONException {
        long now = System.currentTimeMillis();

        JSONArray signupArray = new JSONArray();

        // Valid user 1
        JSONObject u1 = new JSONObject();
        String u1Username = "ayaan_sys1_" + now;
        u1.put("email", u1Username + "@example.com");
        u1.put("username", u1Username);
        u1.put("password", "Password123!");
        u1.put("firstName", "Ayaan");
        u1.put("lastName", "One");
        signupArray.put(u1);

        // Valid user 2
        JSONObject u2 = new JSONObject();
        String u2Username = "ayaan_sys2_" + now;
        u2.put("email", u2Username + "@example.com");
        u2.put("username", u2Username);
        u2.put("password", "Password123!");
        u2.put("firstName", "Ayaan");
        u2.put("lastName", "Two");
        signupArray.put(u2);

        // Invalid user (missing username) -> should be skipped by controller
        JSONObject bad = new JSONObject();
        bad.put("email", "bad_" + now + "@example.com");
        bad.put("password", "Password123!");
        bad.put("firstName", "Bad");
        bad.put("lastName", "User");
        signupArray.put(bad);

        // Call batch signup
        Response batchResp = RestAssured
                .given()
                .contentType("application/json")
                .body(signupArray.toString())
                .when()
                .post("/api/v1/auth/signup/batch");

        assertEquals(201, batchResp.getStatusCode());

        JSONArray created = new JSONArray(batchResp.asString());
        // Only the two valid entries should be created
        assertEquals(2, created.length());

        String createdU1 = created.getJSONObject(0).getString("username");
        String createdU2 = created.getJSONObject(1).getString("username");
        assertTrue(createdU1.contains("ayaan_sys1_"));
        assertTrue(createdU2.contains("ayaan_sys2_"));

        // Now verify these users appear in admin list /api/v2/users
        Response adminListResp = RestAssured
                .given()
                .when()
                .get("/api/v2/users");

        assertEquals(200, adminListResp.getStatusCode());

        JSONArray allUsers = new JSONArray(adminListResp.asString());
        boolean found1 = false;
        boolean found2 = false;

        for (int i = 0; i < allUsers.length(); i++) {
            JSONObject row = allUsers.getJSONObject(i);
            String username = row.getString("username");
            if (username.equals(createdU1)) {
                found1 = true;
            } else if (username.equals(createdU2)) {
                found2 = true;
            }
        }

        assertTrue("First signed-up user should be in admin list", found1);
        assertTrue("Second signed-up user should be in admin list", found2);
    }

    /**
     * TEST 2 (non-trivial):
     * - Create a user with signupBatch.
     * - Try PUT /api/v1/users/{id} with:
     *   * no X-User-Id header -> expect 401
     *   * wrong X-User-Id -> expect 403
     *   * correct X-User-Id -> expect 200 and fields updated
     */
    @Test
    public void updateProfile_pathVersion_enforcesAuthAndUpdates() throws JSONException {
        long now = System.currentTimeMillis();

        // Create one user via batch signup (array of 1 object)
        JSONArray signupArray = new JSONArray();
        JSONObject u = new JSONObject();
        String baseUsername = "ayaan_update_" + now;
        u.put("email", baseUsername + "@example.com");
        u.put("username", baseUsername);
        u.put("password", "Password123!");
        u.put("firstName", "OldFirst");
        u.put("lastName", "OldLast");
        signupArray.put(u);

        Response batchResp = RestAssured
                .given()
                .contentType("application/json")
                .body(signupArray.toString())
                .when()
                .post("/api/v1/auth/signup/batch");

        assertEquals(201, batchResp.getStatusCode());
        JSONArray created = new JSONArray(batchResp.asString());
        long userId = created.getJSONObject(0).getLong("id");

        // Prepare update body
        JSONObject update = new JSONObject();
        String newEmail = baseUsername + "+updated@example.com";
        String newUsername = baseUsername + "_new";
        update.put("email", newEmail);
        update.put("username", newUsername);
        update.put("firstName", "NewFirst");
        update.put("lastName", "NewLast");
        update.put("avatarUrl", "http://example.com/avatar.png");
        update.put("newPassword", "NewPassword123!"); // controller ignores but record has this field

        // 1) No header -> 401 missing_user
        Response noHeaderResp = RestAssured
                .given()
                .contentType("application/json")
                .body(update.toString())
                .when()
                .put("/api/v1/users/" + userId);

        assertEquals(401, noHeaderResp.getStatusCode());

        // 2) Wrong header -> 403 forbidden
        Response wrongHeaderResp = RestAssured
                .given()
                .contentType("application/json")
                .header("X-User-Id", userId + 1) // wrong user
                .body(update.toString())
                .when()
                .put("/api/v1/users/" + userId);

        assertEquals(403, wrongHeaderResp.getStatusCode());

        // 3) Correct header -> 200 and updated profile
        Response okResp = RestAssured
                .given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(update.toString())
                .when()
                .put("/api/v1/users/" + userId);

        assertEquals(200, okResp.getStatusCode());

        JSONObject updatedJson = new JSONObject(okResp.asString());
        assertEquals(userId, updatedJson.getLong("id"));
        assertEquals(newEmail, updatedJson.getString("email"));
        assertEquals(newUsername, updatedJson.getString("username"));
        assertEquals("NewFirst", updatedJson.getString("firstName"));
        assertEquals("NewLast", updatedJson.getString("lastName"));
    }

    /**
     * TEST 3 (non-trivial):
     * - Create a user.
     * - PUT /api/v1/users/me with X-User-Id to update profile.
     * - Then GET /api/v1/users/me with same header and verify fields match.
     * - Also check that GET /api/v1/users/me without header gives 401.
     */
    @Test
    public void updateMe_andMeRoundTrip() throws JSONException {
        long now = System.currentTimeMillis();

        // Create a user
        JSONArray signupArray = new JSONArray();
        JSONObject u = new JSONObject();
        String baseUsername = "ayaan_me_" + now;
        u.put("email", baseUsername + "@example.com");
        u.put("username", baseUsername);
        u.put("password", "Password123!");
        u.put("firstName", "OrigFirst");
        u.put("lastName", "OrigLast");
        signupArray.put(u);

        Response batchResp = RestAssured
                .given()
                .contentType("application/json")
                .body(signupArray.toString())
                .when()
                .post("/api/v1/auth/signup/batch");

        assertEquals(201, batchResp.getStatusCode());
        JSONArray created = new JSONArray(batchResp.asString());
        long userId = created.getJSONObject(0).getLong("id");

        // New profile data
        JSONObject update = new JSONObject();
        String newEmail = baseUsername + "+me@example.com";
        String newUsername = baseUsername + "_me";
        update.put("email", newEmail);
        update.put("username", newUsername);
        update.put("firstName", "MeFirst");
        update.put("lastName", "MeLast");
        update.put("avatarUrl", "http://example.com/me.png");
        update.put("newPassword", "DoesNotMatter");

        // PUT /users/me with correct header
        Response updateResp = RestAssured
                .given()
                .contentType("application/json")
                .header("X-User-Id", userId)
                .body(update.toString())
                .when()
                .put("/api/v1/users/me");

        assertEquals(200, updateResp.getStatusCode());
        JSONObject updatedJson = new JSONObject(updateResp.asString());
        assertEquals(newEmail, updatedJson.getString("email"));
        assertEquals(newUsername, updatedJson.getString("username"));

        // GET /users/me without header -> 401 missing_user
        Response noHeaderMe = RestAssured
                .given()
                .when()
                .get("/api/v1/users/me");

        assertEquals(401, noHeaderMe.getStatusCode());

        // GET /users/me with header -> data matches what we just updated
        Response meResp = RestAssured
                .given()
                .header("X-User-Id", userId)
                .when()
                .get("/api/v1/users/me");

        assertEquals(200, meResp.getStatusCode());
        JSONObject meJson = new JSONObject(meResp.asString());
        assertEquals(newEmail, meJson.getString("email"));
        assertEquals(newUsername, meJson.getString("username"));
        assertEquals("MeFirst", meJson.getString("firstName"));
        assertEquals("MeLast", meJson.getString("lastName"));
    }

    /**
     * TEST 4 (non-trivial):
     * - Create a user.
     * - Confirm GET /api/v1/users/{id} returns 200.
     * - DELETE /api/v2/users/{id} via admin controller.
     * - Then call GET /api/v1/users/me with header X-User-Id = id and expect 404.
     *   (User no longer exists, so profile endpoint should report not found.)
     */
    @Test
    public void deleteUser_thenCurrentProfileReturns404() throws JSONException {
        long now = System.currentTimeMillis();

        // Create the user
        JSONArray signupArray = new JSONArray();
        JSONObject u = new JSONObject();
        String baseUsername = "ayaan_delete_" + now;
        u.put("email", baseUsername + "@example.com");
        u.put("username", baseUsername);
        u.put("password", "Password123!");
        u.put("firstName", "DeleteFirst");
        u.put("lastName", "DeleteLast");
        signupArray.put(u);

        Response batchResp = RestAssured
                .given()
                .contentType("application/json")
                .body(signupArray.toString())
                .when()
                .post("/api/v1/auth/signup/batch");

        assertEquals(201, batchResp.getStatusCode());
        JSONArray created = new JSONArray(batchResp.asString());
        long userId = created.getJSONObject(0).getLong("id");

        // Sanity check: GET /api/v1/users/{id} returns 200 now
        Response getBeforeDelete = RestAssured
                .given()
                .when()
                .get("/api/v1/users/" + userId);

        assertEquals(200, getBeforeDelete.getStatusCode());

        // DELETE via admin controller
        Response deleteResp = RestAssured
                .given()
                .when()
                .delete("/api/v2/users/" + userId);

        // Controller returns 200 on successful delete
        assertEquals(200, deleteResp.getStatusCode());

        // Now current profile endpoint should not find this user anymore
        Response meResp = RestAssured
                .given()
                .header("X-User-Id", userId)
                .when()
                .get("/api/v1/users/me");

        assertEquals(404, meResp.getStatusCode());
    }
}
