package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import mylib.DBUtils;
import DTO.Package;

public class PackageDAO {

    public List<Package> getAllPackage() {
        List<Package> list = new ArrayList<>();
        String sql = "SELECT Package_ID, Name, Description, Price FROM Package";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Package(
                        rs.getInt("Package_ID"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getDouble("Price")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public Package getPackage_ID(int id){
        
        String query = "select Package_ID, Name, Description, Price from Package\n" +
                        "where Package_ID = ?";
        try(Connection connect = DBUtils.getConnection();
                PreparedStatement ps = connect.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs!= null && rs.next()){
                Package pkg = new Package();
                    pkg.setPackageId(rs.getInt("Package_ID"));
                    pkg.setName(rs.getString("Name"));
                    pkg.setDescription(rs.getString("Description"));
                    pkg.setPrice(rs.getDouble("Price"));
                    return pkg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
