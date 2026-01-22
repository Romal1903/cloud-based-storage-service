package com.example.cloudstorage.dto;

import java.util.List;

public class SupabaseSignedUrlResponse {

    private List<SignedUrl> signedUrls;

    public List<SignedUrl> getSignedUrls() {
        return signedUrls;
    }

    public void setSignedUrls(List<SignedUrl> signedUrls) {
        this.signedUrls = signedUrls;
    }

    public static class SignedUrl {
        private String path;
        private String signedURL;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSignedURL() {
            return signedURL;
        }

        public void setSignedURL(String signedURL) {
            this.signedURL = signedURL;
        }
    }
}
