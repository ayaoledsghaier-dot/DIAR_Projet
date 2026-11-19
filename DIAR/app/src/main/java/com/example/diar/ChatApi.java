
package com.example.diar;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

public class ChatApi {

    private static final String BASE_URL =
            "http://192.168.1.22:8083/api/chat/";

    private static RequestQueue queue;

    public interface ObjectCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public interface ArrayCallback {
        void onSuccess(JSONArray response);
        void onError(String error);
    }

    private static RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(
                    context.getApplicationContext()
            );
        }
        return queue;
    }

    // Envoyer un message
    public static void envoyerMessage(
            Context context,
            JSONObject data,
            ObjectCallback callback) {

        Log.d("CHAT_API", "POST send: " + data);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "send",
                data,

                response -> {
                    Log.d("CHAT_API", "Success: " + response);
                    callback.onSuccess(response);
                },

                error -> {
                    String message = getErrorMessage(error);
                    Log.e("CHAT_API", message, error);
                    callback.onError(message);
                }
        );

        request.setShouldCache(false);
        getQueue(context).add(request);
    }

    // Récupérer les conversations
    public static void getConversations(
            Context context,
            String email,
            ArrayCallback callback) {

        try {
            String url = BASE_URL + "conversations?email="
                    + URLEncoder.encode(email, "UTF-8");

            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    callback::onSuccess,
                    error -> callback.onError(getErrorMessage(error))
            );

            request.setShouldCache(false);
            getQueue(context).add(request);

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Récupérer les messages
    public static void getMessages(
            Context context,
            long conversationId,
            ArrayCallback callback) {

        String url = BASE_URL + "messages/" + conversationId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                callback::onSuccess,
                error -> callback.onError(getErrorMessage(error))
        );

        request.setShouldCache(false);
        getQueue(context).add(request);
    }

    // Lire les vraies erreurs du serveur
    private static String getErrorMessage(
            com.android.volley.VolleyError error) {

        if (error.networkResponse != null) {

            int status = error.networkResponse.statusCode;

            String body = "";

            if (error.networkResponse.data != null) {
                body = new String(
                        error.networkResponse.data,
                        StandardCharsets.UTF_8
                );
            }

            return "HTTP " + status + " : " + body;
        }

        return "Erreur réseau : " + error.toString();
    }
}
