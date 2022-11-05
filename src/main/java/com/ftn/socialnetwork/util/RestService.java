package com.ftn.socialnetwork.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@PropertySource("classpath:places.properties")
public class RestService {

    private final RestTemplate restTemplate;
    @Value("${auth.token}")
    private String AUTH_TOKEN_FOR_MAPS_SERVICES;
    @Value("${api.key}")
    private String API_KEY;

    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ResponseEntity<Object> getCityList(String cityName) {
        String url = "https://autocomplete.search.hereapi.com/v1/autocomplete";

        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // set custom header
        headers.set("x-request-source", "desktop");
        headers.set("Authorization", "Bearer " + AUTH_TOKEN_FOR_MAPS_SERVICES);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("q", "{q}")
                .queryParam("types", "{types}")
                .queryParam("apiKey", "{apiKey}")
                .encode()
                .toUriString();

        // create a map for post parameters
        Map<String, String> params = new HashMap<>();
        params.put("q", cityName);
        params.put("types", "city");
        params.put("apiKey", this.API_KEY);

        // build the request
        //HttpEntity<Map<String, ?>> entity = new HttpEntity<>(map, headers);

        try {
            return this.restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, Object.class, params);
        } catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if (HttpStatus.UNAUTHORIZED.equals(httpClientOrServerExc.getStatusCode())) {
                try {
                    String newToken = this.renewValidationTokenWebClient();
                    this.AUTH_TOKEN_FOR_MAPS_SERVICES = newToken;

                    // setting new header and sending request
                    headers.set("Authorization", "Bearer " + newToken);
                    return this.restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, Object.class, params);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

    }

    public String renewValidationTokenWebClient() throws URISyntaxException {
        String randomString = RandGeneratedStr(11);
        String grant_type = "grant_type=client_credentials"; // stays the same
        String scope = "&scope=hrn:here:authorization::org165052888:project/1664718349256";
        String oauth_consumer_key = "&oauth_consumer_key=bh0AOy7-Soe7hvNemS7sNQ";
        String oauth_nonce = "&oauth_nonce="+randomString; // signature string that must be unique for every request - generating random
        String oauth_signature_method = "&oauth_signature_method=HMAC-SHA256"; // stays the same

        // The number of seconds since the Unix epoch at the point the request is generated.
        // The HERE platform rejects requests created too distant in the past or future.
        LocalDateTime time = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
        long epoch = time.atZone(zoneId).toEpochSecond();
        String oauth_timestamp = "&oauth_timestamp="+String.valueOf(epoch);
        String oauth_version = "&oauth_version=1.0"; // stays the same

        String parameter_string =
                grant_type+
                        //scope+
                oauth_consumer_key+
                oauth_nonce+
                oauth_signature_method+
                oauth_timestamp+
                oauth_version;
        System.out.println(parameter_string);


        String url_encoded_base_string = "POST&".concat(URLEncoder.encode("https://account.api.here.com/oauth2/token", StandardCharsets.UTF_8)).concat("&").concat(URLEncoder.encode(parameter_string, StandardCharsets.UTF_8));
        String signing_key = "hlZ-ula4cPrLJsH-PmX0_sbphZV67MUiaODV2YuyzRvq8w7PhRyQC1MhEw9jlmlpUoef8mMqvb5AazpkxGzFQQ"; // here.access.key.secret from credentials
        String url_encoded_signing_key =  URLEncoder.encode( signing_key, StandardCharsets.UTF_8).concat("&"); // adding & to the end is required

        System.out.println(url_encoded_signing_key);
        System.out.println(url_encoded_base_string);


        Mac sha256_HMAC = null;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(url_encoded_signing_key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            //HashCode signatureHmac = Hashing.hmacSha256(url_encoded_signing_key.getBytes(StandardCharsets.UTF_8)).hashString(url_encoded_base_string, StandardCharsets.UTF_8);
            //String signature = Base64.encodeBase64String(signatureHmac.asBytes());

            //String signature = Base64.encodeBase64String(sha256_HMAC.doFinal(url_encoded_base_string.getBytes(StandardCharsets.UTF_8)));
            String signature = new String(java.util.Base64.getEncoder().encode(sha256_HMAC.doFinal(url_encoded_base_string.getBytes(StandardCharsets.UTF_8))));
            //System.out.println("encodedBytes " + new String(encodedBytes));
            String signatureUrlEncoded = URLEncoder.encode( signature, StandardCharsets.UTF_8);

            System.out.println(signature);

            String url = "https://account.api.here.com/oauth2/token";


            String client_id = "bh0AOy7-Soe7hvNemS7sNQ";
            String client_secret = "hlZ-ula4cPrLJsH-PmX0_sbphZV67MUiaODV2YuyzRvq8w7PhRyQC1MhEw9jlmlpUoef8mMqvb5AazpkxGzFQQ";

            //WebClient client = WebClient.create();
            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(
                            HttpClient.create().wiretap(true)
                    ))
                    .build();

            MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
            bodyValues.add("grant_type", "client_credentials");
//            bodyValues.add("client_id", client_id);
//            bodyValues.add("client_secret", client_secret);
            //bodyValues.add("scope", "hrn:here:authorization::org165052888:project/1664718349256");

            //"    $headers[]='Authoradization: OAuth oauth_consumer_key=\"xxxx_xxxxxx-xxxxxxxxxx\",oauth_nonce=\"'.$nonce.'\",oauth_signature=\"'.$signature.'\",oauth_signature_method=\"HMAC-SHA256\",oauth_timestamp=\"'.time().'\",oauth_version=\"1.0\"';\n"

            String consumer_key_encoded = URLEncoder.encode( "bh0AOy7-Soe7hvNemS7sNQ", StandardCharsets.UTF_8);
            String signature_method_encoded = URLEncoder.encode( "HMAC-SHA256", StandardCharsets.UTF_8);
            String timestamp_encoded = URLEncoder.encode( String.valueOf(epoch), StandardCharsets.UTF_8);
            String nonce_encoded = URLEncoder.encode( randomString, StandardCharsets.UTF_8);
            String version_encoded = URLEncoder.encode( "1.0", StandardCharsets.UTF_8);

            String authorizationHeaderEncoded = "OAuth oauth_consumer_key=\""+consumer_key_encoded+
                    "\",oauth_signature_method=\""+signature_method_encoded+
                    "\",oauth_timestamp=\""+timestamp_encoded+
                    "\",oauth_nonce=\""+nonce_encoded+
                    "\",oauth_version=\""+version_encoded+
                    "\",oauth_signature=\""+signatureUrlEncoded+"\"";

            //String ah = "OAuth oauth_consumer_key=\"bh0AOy7-Soe7hvNemS7sNQ\",oauth_nonce=\""+randomString+"\",oauth_signature=\""+signature+"\",oauth_signature_method=\"HMAC-SHA256\",oauth_timestamp=\""+epoch+"\",oauth_version=\"1.0\"";
            String authorizationHeader = "OAuth oauth_consumer_key=\"bh0AOy7-Soe7hvNemS7sNQ\",oauth_signature_method=\"HMAC-SHA256\",oauth_timestamp=\""+epoch+"\",oauth_nonce=\""+randomString+"\",oauth_version=\"1.0\",oauth_signature=\""+signatureUrlEncoded+"\"";
            //String authHeaderPostman = "OAuth oauth_consumer_key=\"bh0AOy7-Soe7hvNemS7sNQ\",oauth_signature_method=\"HMAC-SHA256\",oauth_timestamp=\"1665846159\",oauth_nonce=\"efve2ymi0gJ\",oauth_version=\"1.0\",oauth_signature=\"4jvG7h5qPquk3zb1KzQewR4dos7GNzHAk4byQ9Zha9M%3D\"";
            System.out.println(authorizationHeaderEncoded);

            String response = client.post()
                    .uri(new URI("https://account.api.here.com/oauth2/token"))
//                    .header("oauth_consumer_key", "bh0AOy7-Soe7hvNemS7sNQ")
//                    .header("oauth_nonce", randomString)
//                    .header("oauth_signature", signature)
//                    .header("oauth_signature_method", "HMAC-SHA256")
//                    .header("oauth_timestamp", String.valueOf(epoch))
//                    .header("oauth_version", "1.0")
                    .header("Authorization", authorizationHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromFormData(bodyValues))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println(response);
//            Gson gson = new Gson();
//            Map<String,String> responseData = gson.fromJson(gson.toJson(response),Map.class);
//            System.out.println(responseData);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String newToken = jsonObject.get("access_token").toString();

                // saving new token in places.properties file
                Properties prop = new Properties();
                InputStream in = getClass().getClassLoader().getResourceAsStream("places.properties");
                prop.load(in);
                prop.setProperty("auth.token", newToken);
                prop.store(new FileOutputStream("src/main/resources/places.properties"), null);

                return newToken;
            } catch (JSONException | IOException err){
                throw new RuntimeException(err);
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

//    public void renewValidationToken(){
//        String grant_type = "grant_type=client_credentials"; // stays the same
//        String scope = "&scope=hrn:here:authorization::org165052888:project/1664718349256";
//        String oauth_consumer_key = "&oauth_consumer_key=bh0AOy7-Soe7hvNemS7sNQ";
//        String oauth_nonce = "&oauth_nonce="+RandGeneratedStr(10); // signature string that must be unique for every request - generating random
//        String oauth_signature_method = "&oauth_signature_method=HMAC-SHA256"; // stays the same
//
//        // The number of seconds since the Unix epoch at the point the request is generated.
//        // The HERE platform rejects requests created too distant in the past or future.
//        LocalDateTime time = LocalDateTime.now();
//        ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
//        long epoch = time.atZone(zoneId).toEpochSecond();
//        String oauth_timestamp = "&oauth_timestamp="+String.valueOf(epoch);
//        String oauth_version = "&oauth_version=1.0"; // stays the same
//
//        String parameter_string =
//                grant_type+
//                        scope+
//                        oauth_consumer_key+
//                        oauth_nonce+
//                        oauth_signature_method+
//                        oauth_timestamp+
//                        oauth_version;
//        System.out.println(parameter_string);
//
//        String url_encoded_base_string = "POST&"+ URLEncoder.encode("https://account.api.here.com/oauth2/token", StandardCharsets.UTF_8) +"&"+ URLEncoder.encode(parameter_string, StandardCharsets.UTF_8);
//        String signing_key = "hlZ-ula4cPrLJsH-PmX0_sbphZV67MUiaODV2YuyzRvq8w7PhRyQC1MhEw9jlmlpUoef8mMqvb5AazpkxGzFQQ"; // here.access.key.secret from credentials
//        String url_encoded_signing_key =  URLEncoder.encode( signing_key, StandardCharsets.UTF_8) + "&"; // adding & to the end is required
//
//        System.out.println(url_encoded_signing_key);
//        System.out.println(url_encoded_base_string);
//
//
//        Mac sha256_HMAC = null;
//        try {
//            sha256_HMAC = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secret_key = new SecretKeySpec(url_encoded_signing_key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//            sha256_HMAC.init(secret_key);
//            String signature = Base64.encodeBase64String(sha256_HMAC.doFinal(url_encoded_base_string.getBytes(StandardCharsets.UTF_8)));
//
//            System.out.println(signature);
//
//            String url = "https://account.api.here.com/oauth2/token";
//
//            // create headers
//            HttpHeaders headers = new HttpHeaders();
//            // set `accept` header
//            //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            // set custom header
//            headers.set("Content-Type", "application/x-www-form-urlencoded");
//            headers.set("Authorization", "OAuth");
//            headers.set("oauth_consumer_key", "bh0AOy7-Soe7hvNemS7sNQ");
//            headers.set("oauth_nonce", oauth_nonce.split("=")[1]);
//            headers.set("oauth_signature", signature);
//            headers.set("oauth_signature_method", "HMAC-SHA256");
//            headers.set("oauth_timestamp", String.valueOf(epoch));
//            headers.set("oauth_version", "1.0");
//
//            //HttpEntity<?> entity = new HttpEntity<>(headers);
//
//
//
//            String client_id = "bh0AOy7-Soe7hvNemS7sNQ";
//            String client_secret = "hlZ-ula4cPrLJsH-PmX0_sbphZV67MUiaODV2YuyzRvq8w7PhRyQC1MhEw9jlmlpUoef8mMqvb5AazpkxGzFQQ";
//
////            // create a map for post parameters
////            Map<String, String> params = new HashMap<>();
////            params.put("client_id", client_id);
////            params.put("client_secret", client_secret);
////            params.put("grant_type", "client_credentials");
////            params.put("scope", "hrn:here:authorization::org165052888:project/1664718349256");
//
//            MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
//            form.add("client_id", client_id);
//            form.add("client_secret", client_secret);
//            form.add("grant_type", "client_credentials");
//            form.add("scope", "hrn:here:authorization::org165052888:project/1664718349256");
//
//
////            String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
////                    .queryParam("client_id", "{client_id}")
////                    .queryParam("client_secret", "{client_secret}")
////                    .queryParam("grant_type", "{grant_type}")
////                    .queryParam("scope", "{scope}")
////                    .buildAndExpand(form)
////                    .encode()
////                    .toUriString();
//
//            // build the request
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
//
//            // use `exchange` method for HTTP call
//            ResponseEntity<Void> response = this.restTemplate.exchange(url,HttpMethod.POST, entity,Void.class);
//
//
//            // check response status code
//            if (response.getStatusCode() == HttpStatus.OK){
//                System.out.println(response.getBody());
//            } else {
//                System.out.println(response.getBody());
//            }
//
//        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static String RandGeneratedStr(int l)

    {

        // a list of characters to choose from in form of a string

        String AlphaNumericStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";

        // creating a StringBuffer size of AlphaNumericStr

        StringBuilder s = new StringBuilder(l);

        int i;

        for ( i=0; i<l; i++) {

            //generating a random number using math.random()

            int ch = (int)(AlphaNumericStr.length() * Math.random());

            //adding Random character one by one at the end of s

            s.append(AlphaNumericStr.charAt(ch));

        }

        return s.toString();

    }
}