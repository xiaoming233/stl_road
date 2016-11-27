package stlRoad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.DBHelper;
import common.LngLat;


public class stlMapRoad {
	/**
	 *  |p1 p2| X |p1 p|
	 * @param p1 基点1 
	 * @param p2 基点2
	 * @param p 判断点
	 * @return
	 */
	public static double getCross(LngLat p1,LngLat p2,LngLat p) {
		return (p2.getLng() - p1.getLng()) * (p.getLat() - p1.getLat()) -(p.getLng() - p1.getLng()) * (p2.getLat() - p1.getLat());
	}
/**
 * 
 * @param a_lng 经度
 * @param a_lat 纬度
 * @param points 4个基点
 * @return 
 */
	public static boolean isPoinInPoly(LngLat p ,LngLat [] points) {

		return getCross(points[0],points[1],p) * getCross(points[2],points[3],p) >= 0 && getCross(points[1],points[2],p) * getCross(points[3],points[0],p) >= 0; 
	}

/**
 * 
 * @param str 字符串形式的经纬度
 * @return 返回LngLat 的经度纬度
 */	
	public static LngLat split(String str) {
		String [] strings=str.split(",");
		LngLat lngLat=new LngLat();
		lngLat.setLat(Double.valueOf(strings[0]));
		lngLat.setLng(Double.valueOf(strings[1]));
		return lngLat;
	}
	/**
	 * 
	 * @param lat stl的纬度
	 * @param lng stl的经度
	 * @return LngLat
	 */
	public static LngLat toLngLat (String lat,String lng) {
		LngLat lngLat=new LngLat();
		StringBuilder sb_lat=new StringBuilder(lat);
		StringBuilder sb_lng=new StringBuilder(lng);
		sb_lat.insert(2, '.');
		sb_lng.insert(3, '.');
		lngLat.setLat(Double.valueOf(sb_lat.toString()));
		lngLat.setLng(Double.valueOf(sb_lng.toString()));
		return lngLat;
	}
	/**
	 * 
	 * @param str 经度或纬度字符串
	 * @param i 需要插入的位置
	 * @return 
	 */
	private static String latLngStingAddPiont(String str,int i) {
		StringBuilder sb=new StringBuilder(str);
		sb.insert(i, ".");
		return sb.toString();
	}
	public static void getPiontInPoly(String str) {
		
		String sql1="SELECT rid,A,B,C,D FROM road";
		String sql2="select COUNT(UniqueID) AS icount from stl_p1_d"+str;
		StringBuilder values=new StringBuilder();;
		StringBuilder sql4=null;
		StringBuilder sql5=null;
		int  icount=0;
		int rid;
		DBHelper db=new DBHelper();
		DBHelper db2=new DBHelper();
		DBHelper db3=new DBHelper();
		try {
			db2.conn.setAutoCommit(false);
			ResultSet stlSet=null;
			ResultSet count=db.pst.executeQuery(sql2);
			if (count.next()) {
				icount=count.getInt(1);
			}
			ResultSet roadSet=db.pst.executeQuery(sql1);
			while(roadSet.next()){
				
			rid=roadSet.getInt("rid");	
			if (rid==1) {
				continue;
			}
			LngLat A,B,C,D;
			A=split(roadSet.getString("A"));
			B=split(roadSet.getString("B"));
			C=split(roadSet.getString("C"));
			D=split(roadSet.getString("D"));

			System.out.println("Now, it is map road "+rid);
			LngLat [] points={A,B,C,D};
			int start=1,end=500000;
			   while (end<=icount) {
				sql5=new StringBuilder("SELECT UniqueID,DeviceID,Speed,Longitude,Latitude FROM stl_p1_d"+str+" WHERE id BETWEEN ");
				sql5.append(String.valueOf(start)+" AND "+String.valueOf(end));
				System.out.println(sql5);
				stlSet=db3.pst.executeQuery(sql5.toString());
				boolean flag=false;
				while (stlSet.next()) {
					if (isPoinInPoly(toLngLat(stlSet.getString("Latitude"),stlSet.getString("Longitude")), points)) {
						
						values.append("( '"+stlSet.getString(1)+"' ,");
						values.append(String.valueOf(rid)+",");
						values.append("'"+stlSet.getString(2)+"',");
						values.append(stlSet.getString(3)+",");
						values.append(latLngStingAddPiont(stlSet.getString(5), 2)+",");
						values.append(latLngStingAddPiont(stlSet.getString(4), 3)+"),");
						flag=true;
					} 
					
				}
				if (flag) {
					sql4=new StringBuilder("insert into road_stl_"+str+" (uid,rid,deviceid,speed,lat,lng) values ");
					sql4.append(values.deleteCharAt(values.length()-1));
//					System.out.println(sql4);
					values=new StringBuilder();
					db2.pst.addBatch(sql4.toString());
					db2.pst.executeBatch();
					db2.conn.commit();
				}				
				start=end+1;
				end+=500000;

			  }   
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			db.close();
			db2.close();
			db3.close();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getPiontInPoly("03");
//		for(int i=10;i<=31;i++)
//		{
//		getPiontInPoly(String.valueOf(i));
//		}
		System.out.println("finish");

	}

}
