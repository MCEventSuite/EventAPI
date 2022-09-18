package dev.imabad.mceventsuite.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.imabad.mceventsuite.api.objects.booths.NewBoothData;
import dev.imabad.mceventsuite.core.util.GsonUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class WebDB {

    private Connection connection;


    public void connect(){
        String ipAddress = EventAPI.getInstance().getConfig().getWebIP();
        String password = EventAPI.getInstance().getConfig().getWebPassword();
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ipAddress + ":3306/cubed_sql?user=boothuser&password=" + password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public NewBoothData getBoothApplication(String uuid){
        try {
            if(connection == null || connection.isClosed()) {
                connect();
            }
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `owner_uuid`, `members`, `org` FROM booths_applications WHERE owner_uuid = ? AND year = 2022 AND status = 'accepted';");
            preparedStatement.setString(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.first()){
                return new NewBoothData(resultSet.getString("owner_uuid"), resultSet.getString("org"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

}
