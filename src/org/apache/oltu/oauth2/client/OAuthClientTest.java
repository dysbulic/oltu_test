/**
 *       Copyright 2010 Newcastle University
 *
 *          http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oltu.oauth2.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple example that shows how to get OAuth 2.0 access token from Facebook
 * using Oltu OAuth 2.0 library
 */
public class OAuthClientTest {

    public static void main(String[] args) throws OAuthSystemException, IOException {
    	String callback = "http://localhost:8080/";
    	String clientId = "131804060198305";
    	String secret = "3acb294b071c9aec86d60ae3daf32a93";
    	
    	String host = "http://smoke-track.herokuapp.com";
    	host = "http://localhost:3000";
    	String authUri = host + "/oauth/authorize";
    	String tokenUri = host + "/oauth/token";
        String appUri = host + "/habits";

        callback = "http://localhost:8080";
    	
        clientId = "728ad798943fff1afd90e79765e9534ef52a5b166cfd25f055d1c8ff6f3ae7fd";
    	secret = "3728e0449052b616e2465c04d3cbd792f2d37e70ca64075708bfe8b53c28d529";
    	
    	clientId = "e42ae40e269d9a546316f93e42edf52a18934c6a68de035f8b615343c4b81eb0";
    	secret = "3b1a720c311321c29852dc4735517f6ece0c991d4be677539cb430c7ee4d1097";
    	
    	try {
            OAuthClientRequest request = OAuthClientRequest
            	.authorizationLocation(authUri)
                .setClientId(clientId)
                .setRedirectURI(callback)
                .setResponseType("code")
                .buildQueryMessage();

            //in web application you make redirection to uri:
            System.out.println("Visit: " + request.getLocationUri() + "\nand grant permission");

            System.out.print("Now enter the OAuth code you have received in redirect uri ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();

            request = OAuthClientRequest
            	.tokenLocation(tokenUri)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(clientId)
                .setClientSecret(secret)
                .setRedirectURI(callback)
                .setCode(code)
                .buildBodyMessage();
            
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class);

            System.out.println(
                "Access Token: " + oAuthResponse.getAccessToken() + ", Expires in: " + oAuthResponse
                    .getExpiresIn());

            URL appURL = new URL(appUri);
            HttpURLConnection con = (HttpURLConnection) appURL.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");
            con.setRequestProperty("Authorization", "Bearer " + oAuthResponse.getAccessToken());

            JSONObject body = new JSONObject();
            body.put("color", "green");
            body.put("name", "Java Test");
            body.put("description", "Test Habit");
            
            byte[] bodyBytes = body.toString().getBytes();

            con.setRequestProperty("Content-Length", Integer.toString(bodyBytes.length));

            con.setInstanceFollowRedirects(false);
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(bodyBytes);
            wr.flush();
            wr.close();

            //InputStream is = con.getInputStream();
            int status = con.getResponseCode();
            System.out.println("Status: " + status);
    	} catch (OAuthProblemException e) {
            System.out.println("OAuth error: " + e.getError());
            System.out.println("OAuth error description: " + e.getDescription());
        } catch (JSONException e) {
			e.printStackTrace();
		}
    }
}