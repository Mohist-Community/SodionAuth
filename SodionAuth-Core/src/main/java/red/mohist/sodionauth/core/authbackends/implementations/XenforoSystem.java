/*
 * Copyright 2020 Mohist-Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.mohist.sodionauth.core.authbackends.implementations;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import red.mohist.sodionauth.core.authbackends.AuthBackendSystem;
import red.mohist.sodionauth.core.enums.ResultType;
import red.mohist.sodionauth.core.modules.AbstractPlayer;
import red.mohist.sodionauth.core.utils.Helper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URLEncoder;

@SuppressWarnings("unused")
public class XenforoSystem implements AuthBackendSystem {

    private final String url;
    private final String key;

    public XenforoSystem(String url, String key) {
        this.url = url;
        this.key = key;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ResultType register(AbstractPlayer player, String password, String email) {
        try {
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200 || status == 400) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else if (status == 401) {
                    Helper.getLogger().warn(
                            Helper.langFile("errors.key", ImmutableMap.of(
                                    "key", key)));
                } else if (status == 404) {
                    Helper.getLogger().warn(
                            Helper.langFile("errors.url", ImmutableMap.of(
                                    "url", url)));
                }
                return null;
            };

            String result = Request.Post(url + "/users")
                    .bodyForm(Form.form().add("username", player.getName())
                            .add("password", password)
                            .add("email", email).build())
                    .addHeader("XF-Api-Key", key)
                    .addHeader("XF-Api-User", "1")
                    .execute().handleResponse(responseHandler);

            if (result == null) {
                return ResultType.SERVER_ERROR;
            }
            JsonParser parse = new JsonParser();
            JsonObject json;
            try {
                json = parse.parse(result).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                Helper.getLogger().warn(result);
                e.printStackTrace();
                return ResultType.SERVER_ERROR;
            }
            if (json == null) {
                return ResultType.SERVER_ERROR;
            }
            if (json.get("success") != null && json.get("success").getAsBoolean()) {
                return ResultType.OK;
            } else {
                JsonArray errors = json.get("errors").getAsJsonArray();
                if (errors.size() > 0) {
                    switch (errors.get(0).getAsJsonObject().get("code").getAsString()) {
                        case "usernames_must_be_unique":
                            return ResultType.USER_EXIST;
                        case "please_enter_valid_email":
                            return ResultType.EMAIL_WRONG;
                        case "email_addresses_must_be_unique":
                            return ResultType.EMAIL_EXIST;
                        default:
                            return ResultType.UNKNOWN.inheritedObject(ImmutableMap.of(
                                    "code", errors.get(0).getAsJsonObject().get("code").getAsString(),
                                    "message", errors.get(0).getAsJsonObject().get("message").getAsString()));
                    }
                } else {
                    return ResultType.SERVER_ERROR;
                }
            }
        } catch (Exception e) {
            Helper.getLogger().warn(
                    "Error while register player " + player.getName() + " data", e);
            return ResultType.SERVER_ERROR;
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ResultType login(AbstractPlayer player, String password) {
        try {
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200 || status == 400) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else if (status == 403) {
                    Helper.getLogger().warn(
                            Helper.langFile("errors.key", ImmutableMap.of(
                                    "key", key)));
                } else if (status == 404) {
                    Helper.getLogger().warn(
                            Helper.langFile("errors.url", ImmutableMap.of(
                                    "url", url)));
                }
                return null;
            };

            String result = Request.Post(url + "/auth")
                    .bodyForm(Form.form().add("login", player.getName())
                            .add("password", password).build())
                    .addHeader("XF-Api-Key", key)
                    .execute().handleResponse(responseHandler);


            if (result == null) {
                return ResultType.SERVER_ERROR;
            }
            JsonParser parse = new JsonParser();
            JsonObject json;
            try {
                json = parse.parse(result).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                Helper.getLogger().warn(result);
                e.printStackTrace();
                return ResultType.SERVER_ERROR;
            }
            if (json == null) {
                return ResultType.SERVER_ERROR;
            }
            if (json.get("success") != null && json.get("success").getAsBoolean()) {
                json.get("user").getAsJsonObject().get("username").getAsString();
                if (json.get("user").getAsJsonObject().get("username").getAsString().equals(player.getName())) {
                    return ResultType.OK;
                } else {
                    return ResultType.ERROR_NAME.inheritedObject(ImmutableMap.of(
                            "correct", json.getAsJsonObject("exact").get("username").getAsString()));
                }
            } else {
                JsonArray errors = json.get("errors").getAsJsonArray();
                if (errors.size() > 0) {
                    switch (errors.get(0).getAsJsonObject().get("code").getAsString()) {
                        case "incorrect_password":
                            return ResultType.PASSWORD_INCORRECT;
                        case "requested_user_x_not_found":
                            return ResultType.NO_USER;
                        default:
                            return ResultType.UNKNOWN.inheritedObject(ImmutableMap.of(
                                    "code", errors.get(0).getAsJsonObject().get("code").getAsString(),
                                    "message", errors.get(0).getAsJsonObject().get("message").getAsString()));
                    }
                } else {
                    return ResultType.SERVER_ERROR;
                }
            }
        } catch (Exception e) {
            Helper.getLogger().warn("Error while checking player " + player.getName() + " data", e);
            return ResultType.SERVER_ERROR;
        }
    }

    @Nonnull
    @Override
    public ResultType join(AbstractPlayer player) {
        return join(player.getName());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ResultType join(String name) {
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else if (status == 401) {
                Helper.getLogger().warn(
                        Helper.langFile("errors.key", ImmutableMap.of(
                                "key", key)));
            } else if (status == 404) {
                Helper.getLogger().warn(
                        Helper.langFile("errors.url", ImmutableMap.of(
                                "url", url)));
            }
            return null;
        };
        String result;
        try {
            result = Request.Get(url + "/users/find-name?username=" +
                    URLEncoder.encode(name, "UTF-8"))
                    .addHeader("XF-Api-Key", key)
                    .execute().handleResponse(responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return ResultType.SERVER_ERROR;
        }
        if (result == null) {
            new ClientProtocolException("Unexpected response: null").printStackTrace();
            return ResultType.SERVER_ERROR;
        }
        JsonParser parse = new JsonParser();
        JsonObject json;
        try {
            json = parse.parse(result).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            Helper.getLogger().warn(result);
            e.printStackTrace();
            return ResultType.SERVER_ERROR;
        }
        if (json == null) {
            new ClientProtocolException("Unexpected json: null").printStackTrace();
            return ResultType.SERVER_ERROR;
        }
        if (json.get("exact").isJsonNull()) {
            return ResultType.NO_USER;
        }
        if (!json.getAsJsonObject("exact").get("username").getAsString().equals(name)) {
            return ResultType.ERROR_NAME.inheritedObject(ImmutableMap.of(
                    "correct", json.getAsJsonObject("exact").get("username").getAsString()));
        }
        return ResultType.OK;
    }
}
