package dev.imabad.mceventsuite.api.objects;

public class BasicResponse {

    public static BasicResponse SUCCESS = new BasicResponse(true);
    public static BasicResponse UNSUCCESSFUL = new BasicResponse(false);

    public static BasicResponse error(String error){
        return new BasicResponse(error);
    }

    private boolean success;
    private String error;

    private BasicResponse(boolean success){
        this.success = success;
    }

    private BasicResponse(String error){
        this.success = false;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}
