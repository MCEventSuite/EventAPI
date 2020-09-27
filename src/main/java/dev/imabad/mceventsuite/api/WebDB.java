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
        try {
            connection = DriverManager.getConnection("jdbc:mysql://159.65.35.255:3306/cubed_sql?user=boothuser&password=PeziWapEWA8iy67u1EgI8O114Ot5JI");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public NewBoothData getBoothApplication(String uuid){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `owner_uuid`, `members`, `org` FROM booths_applications WHERE owner_uuid = ? AND year = 2020 AND status = 'accepted';");
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
