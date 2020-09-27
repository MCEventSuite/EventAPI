package dev.imabad.mceventsuite.api.objects;

public class AuthResponse {

    private boolean success;
    private String token;
    private String error;

    public AuthResponse(boolean success, String token, String error) {
        this.success = success;
        this.token = token;
        this.error = error;
    }

    public AuthResponse(boolean success, String token) {
        this.success = success;
        this.token = token;
    }

    public AuthResponse(String error){
        this.success = false;
        this.error = error;
    }

}
