package com.example.cloudstorage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.cloudstorage.dto.SupabaseSignedUrlResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.bucket.public}")
    private boolean publicBucket;

    @Value("${supabase.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateUploadPath(Long userId, String filename) {
        String safeFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return userId + "/" + UUID.randomUUID() + "_" + safeFilename;
    }

    public String getUploadEndpoint(String path) {
        return supabaseUrl +
                "/storage/v1/object/" +
                bucket +
                "/" +
                path;
    }

    public String getPublicReadUrl(String path) {
        return supabaseUrl +
                "/storage/v1/object/public/" +
                bucket +
                "/" +
                path;
    }

    public String getSignedReadUrl(String path) {

        if (publicBucket) {
            return getPublicReadUrl(path);
        }

        return generateSignedUrl(path, "preview");
    }

    public String getSignedDownloadUrl(String path) {

        if (publicBucket) {
            return getPublicReadUrl(path);
        }

        return generateSignedUrl(path, "download");
    }

    private String generateSignedUrl(String path, String purpose) {

        String url = supabaseUrl + "/storage/v1/object/sign/" + bucket;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "paths", List.of(path),
                "expiresIn", 600
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<SupabaseSignedUrlResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        SupabaseSignedUrlResponse.class
                );

        SupabaseSignedUrlResponse res = response.getBody();

        if (res == null || res.getSignedUrls() == null || res.getSignedUrls().isEmpty()) {
            throw new RuntimeException("Failed to generate signed " + purpose + " URL");
        }

        String signedUrl = res.getSignedUrls().get(0).getSignedURL();

        return signedUrl.startsWith("http")
                ? signedUrl
                : supabaseUrl + signedUrl;
    }

    public long fetchFileSize(String path) {

        String url = supabaseUrl +
                "/storage/v1/object/info/" +
                bucket +
                "/" +
                path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map body = response.getBody();

        if (body == null || !body.containsKey("size")) {
            return 0;
        }

        return ((Number) body.get("size")).longValue();
    }

    public void delete(String path) {

        String url = supabaseUrl +
                "/storage/v1/object/" +
                bucket +
                "/" +
                path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.set("apikey", serviceKey);

        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
    }
}
